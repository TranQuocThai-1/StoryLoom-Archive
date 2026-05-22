package com.storyloom.archive.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "book_interactions")
public class BookInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    private String type; 

    private String sessionId;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public BookInteraction() {}

    // UPDATED: Constructor now requires the session ID
    public BookInteraction(Book book, String type, String sessionId) {
        this.book = book;
        this.type = type;
        this.sessionId = sessionId;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}