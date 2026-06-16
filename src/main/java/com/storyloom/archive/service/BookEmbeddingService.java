package com.storyloom.archive.service;

import com.storyloom.archive.model.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class BookEmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(BookEmbeddingService.class);
    private final VectorStore vectorStore;

    public BookEmbeddingService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public void embedBookIntoDatabase(Book book, String rawBookContent) {
        log.info("Starting AI embedding process for book: {}", book.getTitle());

        // 1. Chop the massive book text into small, readable chunks (~800 tokens each)
        TokenTextSplitter textSplitter = new TokenTextSplitter();

        // 2. Create a Spring AI Document, tagging it with the Book's database ID and Title
        Document document = new Document(rawBookContent, Map.of(
                "bookId", book.getId(),
                "title", book.getTitle(),
                "author", book.getAuthorName()
        ));

        // 3. Apply the splitter to break it into an array of chunks
        List<Document> chunks = textSplitter.apply(List.of(document));
        
        log.info("Book split into {} chunks. Saving to pgvector...", chunks.size());

        // 4. Save to PostgreSQL (This automatically calls Ollama to translate text to vectors)
        vectorStore.add(chunks);
        
        log.info("Successfully vectorized and saved book: {}", book.getTitle());
    }
}