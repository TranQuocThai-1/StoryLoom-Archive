package com.storyloom.archive.repository;

import com.storyloom.archive.model.BookInteraction;
import com.storyloom.archive.model.Book;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookInteractionRepository extends JpaRepository<BookInteraction, Long> {

    @Query("SELECT i.book FROM BookInteraction i WHERE i.timestamp > :since GROUP BY i.book ORDER BY COUNT(i) DESC")
    List<Book> findTrendingBooks(@Param("since") LocalDateTime since, Pageable pageable);

    @Query(value = "SELECT b.author_name FROM book_interactions i " +
                   "JOIN books b ON i.book_id = b.id " +
                   "WHERE i.timestamp > :since " +
                   "GROUP BY b.author_name " +
                   "ORDER BY COUNT(i.id) DESC", nativeQuery = true)
    List<String> findTrendingAuthors(@Param("since") LocalDateTime since, Pageable pageable);

    @Query("SELECT i2.book FROM BookInteraction i1 JOIN BookInteraction i2 ON i1.sessionId = i2.sessionId " +
           "WHERE i1.book.id = :bookId AND i2.book.id != :bookId AND i1.sessionId IS NOT NULL " +
           "GROUP BY i2.book ORDER BY COUNT(i2) DESC")
    List<Book> findBooksAlsoDownloadedBySession(@Param("bookId") Long bookId, Pageable pageable);
}