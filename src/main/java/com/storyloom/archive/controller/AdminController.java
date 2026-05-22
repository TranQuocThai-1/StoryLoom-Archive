package com.storyloom.archive.controller;

import com.storyloom.archive.model.Book;
import com.storyloom.archive.repository.BookRepository;
import com.storyloom.archive.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class AdminController {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private FileStorageService fileStorageService; 

    @GetMapping("/admin/add-book")
    public String showAddBookForm() {
        return "admin"; 
    }

    @PostMapping("/admin/add-book")
    public String saveNewBook(
            @RequestParam String title,
            @RequestParam String authorName,
            @RequestParam(required = false) Integer publishYear,
            @RequestParam String language,
            @RequestParam(required = false) List<String> category, 
            @RequestParam String synopsis,
            @RequestParam(required = false) String locMainClass, 
            @RequestParam(required = false) String locSubClass,
            @RequestParam(value = "coverImage", required = false) MultipartFile coverImage,
            @RequestParam(value = "textFile", required = false) MultipartFile textFile,
            @RequestParam(value = "epubFile", required = false) MultipartFile epubFile) { 

        Book newBook = new Book();
        newBook.setTitle(title);
        newBook.setAuthorName(authorName);
        newBook.setPublishYear(publishYear);
        newBook.setLanguage(language);
        
        String finalCategories = "Uncategorized";
        if (category != null && !category.isEmpty()) {
            finalCategories = category.stream()
                    .filter(c -> c != null && !c.trim().isEmpty())
                    .collect(Collectors.joining(", "));
            
            if (finalCategories.isEmpty()) {
                finalCategories = "Uncategorized";
            }
        }
        newBook.setCategory(finalCategories);
        newBook.setSynopsis(synopsis);
        

        String finalLocClass = null;
        if (locMainClass != null && !locMainClass.isEmpty()) {
            finalLocClass = locMainClass;
            if (locSubClass != null && !locSubClass.isEmpty()) {
                finalLocClass = locMainClass + " > " + locSubClass;
            }
        }
        newBook.setLocClass(finalLocClass); 
        
        newBook.setDownloadCount(0); 

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        newBook.setReleaseDate(LocalDate.now().format(formatter));

        if (coverImage != null && !coverImage.isEmpty()) {
            newBook.setCoverImagePath(fileStorageService.saveFile(coverImage));
        }
        if (textFile != null && !textFile.isEmpty()) {
            newBook.setTextFilePath(fileStorageService.saveFile(textFile));
        }
        if (epubFile != null && !epubFile.isEmpty()) {
            newBook.setEpubFilePath(fileStorageService.saveFile(epubFile));
        }

        newBook = bookRepository.save(newBook);
        return "redirect:/book/" + newBook.getId(); 
    }
}