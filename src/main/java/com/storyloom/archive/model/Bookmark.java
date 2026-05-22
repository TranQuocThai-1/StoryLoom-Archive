package com.storyloom.archive.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookmarks")
public class Bookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The user who saved it
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // The book/lore entry they saved
    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    // Automatically record exactly when they clicked "Bookmark"
    private LocalDateTime createdAt = LocalDateTime.now();

    public Bookmark() {}

    public Bookmark(User user, Book book) {
        this.user = user;
        this.book = book;
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}