package com.storyloom.archive.repository;

import com.storyloom.archive.model.Book;
import com.storyloom.archive.model.Bookmark;
import com.storyloom.archive.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    List<Bookmark> findByUserOrderByCreatedAtDesc(User user);

    // Checks if a user already bookmarked a specific book
    Optional<Bookmark> findByUserAndBook(User user, Book book);
}