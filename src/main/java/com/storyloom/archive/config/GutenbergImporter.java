package com.storyloom.archive.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.storyloom.archive.model.Book;
import com.storyloom.archive.repository.BookRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Component
public class GutenbergImporter implements CommandLineRunner {

    private final BookRepository bookRepository;

    public GutenbergImporter(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (bookRepository.count() == 0) {
            System.out.println("📦 Empty database detected. Initiating Gutenberg Bulk Import...");

            try {
                ObjectMapper mapper = new ObjectMapper();
                InputStream inputStream = new ClassPathResource("metadata.json").getInputStream();
                
                List<Book> booksToImport = mapper.readValue(inputStream, new TypeReference<List<Book>>(){});
                
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
                String currentDate = LocalDate.now().format(formatter);
                Random random = new Random();

                for (Book book : booksToImport) {
                    book.setReleaseDate(currentDate);
                    book.setDownloadCount(random.nextInt(900) + 100); 
                    
                    System.out.println("✅ Preparing to save: " + book.getTitle());
                }

                bookRepository.saveAll(booksToImport);
                System.out.println("🚀 Import Complete! " + booksToImport.size() + " real books injected into the archive.");

            } catch (Exception e) {
                System.out.println("❌ Failed to import Gutenberg data: " + e.getMessage());
            }
        } else {
            System.out.println("Database already contains data. Bypassing bulk import.");
        }
    }
}