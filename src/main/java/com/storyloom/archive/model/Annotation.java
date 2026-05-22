package com.storyloom.archive.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "annotations")
public class Annotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    // We use columnDefinition = "TEXT" because lore notes can get very long!
    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime lastUpdated = LocalDateTime.now();

    public Annotation() {}

    public Annotation(User user, Book book, String content) {
        this.user = user;
        this.book = book;
        this.content = content;
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }
    public String getContent() { return content; }
    public void setContent(String content) { 
        this.content = content; 
        this.lastUpdated = LocalDateTime.now(); // Automatically update timestamp when edited
    }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}