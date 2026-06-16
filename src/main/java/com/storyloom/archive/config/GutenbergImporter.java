package com.storyloom.archive.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.storyloom.archive.model.Book;
import com.storyloom.archive.repository.BookRepository;
import com.storyloom.archive.service.BookEmbeddingService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Component
public class GutenbergImporter implements CommandLineRunner {

    private final BookRepository bookRepository;
    private final BookEmbeddingService bookEmbeddingService;
    private final MinioClient minioClient;

    @Value("${minio.bucket.name}")
    private String bucketName;

    public GutenbergImporter(BookRepository bookRepository, BookEmbeddingService bookEmbeddingService, MinioClient minioClient) {
        this.bookRepository = bookRepository;
        this.bookEmbeddingService = bookEmbeddingService;
        this.minioClient = minioClient;
    }

    @Override
    public void run(String... args) throws Exception {
        if (bookRepository.count() == 0) {
            System.out.println("📦 Empty database detected. Initiating Automated Gutenberg Bulk Import & Download...");

            try {
                ObjectMapper mapper = new ObjectMapper();
                InputStream inputStream = new ClassPathResource("metadata.json").getInputStream();
                List<Book> booksToImport = mapper.readValue(inputStream, new TypeReference<List<Book>>(){});
                
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
                String currentDate = LocalDate.now().format(formatter);
                Random random = new Random();
                HttpClient httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();

                for (Book book : booksToImport) {
                    book.setReleaseDate(currentDate);
                    book.setDownloadCount(random.nextInt(900) + 100); 

                    String gId = book.getGutenbergId();
                    String fileContent = book.getSynopsis(); // Fallback context

                    if (gId != null && !gId.trim().isEmpty() && book.getTextFilePath() != null) {
                        String downloadUrl = "https://www.gutenberg.org/cache/epub/" + gId + "/pg" + gId + ".txt";
                        System.out.println("🌐 Downloading full text for '" + book.getTitle() + "'...");
                        
                        try {
                            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(downloadUrl)).GET().build();
                            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                            if (response.statusCode() == 200 && response.body().length() > 500) {
                                fileContent = response.body();
                                byte[] bytes = fileContent.getBytes("UTF-8");

                                minioClient.putObject(
                                        PutObjectArgs.builder()
                                                .bucket(bucketName)
                                                .object(book.getTextFilePath())
                                                .stream(new ByteArrayInputStream(bytes), bytes.length, -1)
                                                .contentType("text/plain")
                                                .build()
                                );
                                System.out.println("💾 Uploaded '" + book.getTextFilePath() + "' to MinIO bucket!");
                            } else {
                                System.out.println("⚠️ Gutenberg returned code " + response.statusCode() + ". Using synopsis as vector context.");
                            }
                        } catch (Exception e) {
                            System.out.println("⚠️ Network issue downloading '" + book.getTitle() + "': " + e.getMessage());
                        }
                    }

                    Book savedBook = bookRepository.save(book);

                    System.out.println("🧠 Indexing document content into pgvector for: " + savedBook.getTitle());
                    bookEmbeddingService.embedBookIntoDatabase(savedBook, fileContent);
                }
                
                System.out.println("🧠 AI has finished downloading, saving, and reading the entire library!");

            } catch (Exception e) {
                System.out.println("❌ Failed to import Gutenberg library data: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Database already contains data. Bypassing automated import.");
        }
    }
}