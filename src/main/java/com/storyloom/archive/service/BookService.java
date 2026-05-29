package com.storyloom.archive.service;

import com.storyloom.archive.model.Book;
import com.storyloom.archive.repository.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    private static final Logger log = LoggerFactory.getLogger(BookService.class);

    @Autowired
    private BookRepository bookRepository;

    public Book saveBook(Book book) {
        log.info("Saving new book to the archive: {}", book.getTitle());
        return bookRepository.save(book);
    }

    public Optional<Book> getBookById(Long id) {
        log.info("Fetching book details for ID: {}", id);
        return bookRepository.findById(id);
    }

    public Page<Book> getBooksByTitleLetter(String letter, int pageNumber, int pageSize) {
        log.info("Fetching titles starting with '{}', Page: {}", letter, pageNumber);
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);
        
        if ("0-9".equals(letter)) {
            return bookRepository.findByTitleStartingWithNumber(pageable);
        }
        return bookRepository.findByTitleStartingWithIgnoreCaseOrderByTitleAsc(letter, pageable);
    }

    public Page<Book> getBooksByAuthorLetter(String letter, int pageNumber, int pageSize) {
        log.info("Fetching authors starting with '{}', Page: {}", letter, pageNumber);
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);
        
        if ("0-9".equals(letter)) {
            return bookRepository.findByAuthorNameStartingWithNumber(pageable);
        }
        return bookRepository.findByAuthorNameStartingWithIgnoreCaseOrderByAuthorNameAsc(letter, pageable);
    }
    
    public Page<Book> getBooksByCategory(String category, int pageNumber, int pageSize, Sort sort) {
        log.info("Fetching category '{}', Page: {}", category, pageNumber);
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, sort);
        return bookRepository.findByCategoryIgnoreCase(category, pageable);
    }

    public Page<Book> getBooksBySpecificAuthor(String authorName, int pageNumber, int pageSize, Sort sort) {
        log.info("Fetching books by author '{}', Page: {}", authorName, pageNumber);
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, sort);
        return bookRepository.findByAuthorNameIgnoreCase(authorName, pageable);
    }

    public Page<Book> getAllBooksPaged(int pageNumber, int pageSize, Sort sort) {
        log.info("Fetching all books, Page: {}", pageNumber);
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, sort);
        return bookRepository.findAll(pageable);
    }

    public List<Book> getRandomBooks() {
        log.info("Fetching 20 random books");
        return bookRepository.findRandom20Books();
    }
    
    public long getTotalBookCount() {
        return bookRepository.count();
    }

    public Page<Book> searchArchive(String title, String author, String subject, String language, int pageNumber, int pageSize) {
        log.info("Searching archive - Title: '{}', Author: '{}', Subject: '{}', Language: '{}', Page: {}", 
                 title, author, subject, language, pageNumber);
        
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);
        return bookRepository.searchArchive(title, author, subject, language, pageable);
    }
}