package com.storyloom.archive.model;

import jakarta.persistence.*;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String authorName;
    private Integer publishYear;
    private String language;
    private String category;

    @Column(columnDefinition = "TEXT")
    private String synopsis;

    private String locClass;

    private Integer downloadCount;
    private String releaseDate;

    private String coverImagePath;
    private String textFilePath;
    private String epubFilePath;

    // Empty constructor required by JPA
    public Book() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public Integer getPublishYear() { return publishYear; }
    public void setPublishYear(Integer publishYear) { this.publishYear = publishYear; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSynopsis() { return synopsis; }
    public void setSynopsis(String synopsis) { this.synopsis = synopsis; }

    // --- NEW GETTER/SETTER FOR LOC CLASS ---
    public String getLocClass() { return locClass; }
    public void setLocClass(String locClass) { this.locClass = locClass; }

    public Integer getDownloadCount() { return downloadCount; }
    public void setDownloadCount(Integer downloadCount) { this.downloadCount = downloadCount; }

    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }

    public String getCoverImagePath() { return coverImagePath; }
    public void setCoverImagePath(String coverImagePath) { this.coverImagePath = coverImagePath; }

    public String getTextFilePath() { return textFilePath; }
    public void setTextFilePath(String textFilePath) { this.textFilePath = textFilePath; }

    public String getEpubFilePath() { return epubFilePath; }
    public void setEpubFilePath(String epubFilePath) { this.epubFilePath = epubFilePath; }
}