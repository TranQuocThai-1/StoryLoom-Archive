package com.storyloom.archive.service;

import com.storyloom.archive.model.Book;
import com.storyloom.archive.model.Bookmark;
import com.storyloom.archive.model.User;
import com.storyloom.archive.repository.BookRepository;
import com.storyloom.archive.repository.BookmarkRepository;
import com.storyloom.archive.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookmarkService {

    private static final Logger log = LoggerFactory.getLogger(BookmarkService.class);

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    public List<Bookmark> getBookmarksForUser(String email) {
        log.info("Fetching bookmarks for user: {}", email);
        User user = userRepository.findByEmail(email).orElseThrow();
        return bookmarkRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public void addBookmark(String email, Long bookId) {
        User user = userRepository.findByEmail(email).orElseThrow();
        Book book = bookRepository.findById(bookId).orElseThrow();

        Optional<Bookmark> existing = bookmarkRepository.findByUserAndBook(user, book);
        if (existing.isEmpty()) {
            bookmarkRepository.save(new Bookmark(user, book));
            log.info("Successfully added bookmark for book ID {} by user {}", bookId, email);
        } else {
            log.info("Bookmark already exists for book ID {} by user {}", bookId, email);
        }
    }

    public void removeBookmark(String email, Long bookId) {
        User user = userRepository.findByEmail(email).orElseThrow();
        Book book = bookRepository.findById(bookId).orElseThrow();

        bookmarkRepository.findByUserAndBook(user, book).ifPresent(bookmark -> {
            bookmarkRepository.delete(bookmark);
            log.info("Successfully removed bookmark for book ID {} by user {}", bookId, email);
        });
    }
}