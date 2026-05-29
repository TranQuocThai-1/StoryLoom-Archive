package com.storyloom.archive.repository;

import org.springframework.data.repository.query.Param;
import com.storyloom.archive.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    
    Page<Book> findByAuthorNameStartingWithIgnoreCaseOrderByAuthorNameAsc(String letter, Pageable pageable);
    
    Page<Book> findByTitleStartingWithIgnoreCaseOrderByTitleAsc(String letter, Pageable pageable);
    
    Page<Book> findByCategoryIgnoreCase(String category, Pageable pageable);
    
    Page<Book> findByAuthorNameIgnoreCase(String authorName, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE SUBSTRING(LOWER(b.authorName), 1, 1) BETWEEN '0' AND '9'")
    Page<Book> findByAuthorNameStartingWithNumber(Pageable pageable);

    @Query("SELECT b FROM Book b WHERE SUBSTRING(LOWER(b.title), 1, 1) BETWEEN '0' AND '9'")
    Page<Book> findByTitleStartingWithNumber(Pageable pageable);

    List<Book> findByTitleContainingIgnoreCaseAndAuthorNameContainingIgnoreCase(String title, String author);
    
    List<Book> findByTitleContainingIgnoreCase(String title);

    List<Book> findByCategoryIgnoreCase(String category, Sort sort);
    List<Book> findByAuthorNameIgnoreCase(String authorName, Sort sort);

    List<Book> findTop10ByOrderByIdDesc(); // Fetches the 10 newest additions
    List<Book> findTop10ByOrderByDownloadCountDesc(); // Fetches the 10 highest downloads
    
    @Query("SELECT DISTINCT b.category FROM Book b WHERE b.category IS NOT NULL ORDER BY b.category ASC")
    List<String> findAllDistinctCategories();

    @Query(value = "SELECT * FROM books ORDER BY RANDOM() LIMIT 20", nativeQuery = true)
    List<Book> findRandom20Books();

    @Query("SELECT b FROM Book b WHERE " +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%')) AND " +
           "LOWER(b.authorName) LIKE LOWER(CONCAT('%', :author, '%')) AND " +
           "(:subject = '' OR LOWER(b.category) = LOWER(:subject)) AND " +
           "(:language = '' OR LOWER(b.language) = LOWER(:language))")
    Page<Book> searchArchive(@Param("title") String title,
                             @Param("author") String author,
                             @Param("subject") String subject,
                             @Param("language") String language,
                             Pageable pageable);
}