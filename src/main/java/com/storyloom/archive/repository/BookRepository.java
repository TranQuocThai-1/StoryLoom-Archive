package com.storyloom.archive.repository;

import com.storyloom.archive.model.Book;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    
    // Existing Advanced Search Method
    List<Book> findByTitleContainingIgnoreCaseAndAuthorNameContainingIgnoreCase(String title, String author);

    // Alphabetical Catalog Methods
    List<Book> findByAuthorNameStartingWithIgnoreCaseOrderByAuthorNameAsc(String letter);
    List<Book> findByTitleStartingWithIgnoreCaseOrderByTitleAsc(String letter);
    
    // UPDATED: Grid Methods with Sorting Support
    List<Book> findByCategoryIgnoreCase(String category, Sort sort);
    List<Book> findByAuthorNameIgnoreCase(String authorName, Sort sort);

    // NEW: For the Homepage Sliders
    List<Book> findTop10ByOrderByIdDesc(); // Fetches the 10 newest additions
    List<Book> findTop10ByOrderByDownloadCountDesc(); // Fetches the 10 highest downloads
    // This is the missing method that VS Code and Spring Boot are complaining about
    @Query("SELECT DISTINCT b.category FROM Book b WHERE b.category IS NOT NULL ORDER BY b.category ASC")
    List<String> findAllDistinctCategories();

    // NEW: Handles the 0-9 catalog queries using PostgreSQL Regex
    @Query(value = "SELECT * FROM books WHERE author_name ~ '^[0-9]' ORDER BY author_name ASC", nativeQuery = true)
    List<Book> findByAuthorNameStartingWithNumber();

    @Query(value = "SELECT * FROM books WHERE title ~ '^[0-9]' ORDER BY title ASC", nativeQuery = true)
    List<Book> findByTitleStartingWithNumber();

    @Query(value = "SELECT * FROM books ORDER BY RANDOM() LIMIT 20", nativeQuery = true)
    List<Book> findRandom20Books();

    List<Book> findByTitleContainingIgnoreCase(String title);

}
