package com.storyloom.archive.controller;

import com.storyloom.archive.model.Book;
import com.storyloom.archive.service.BookService;
import com.storyloom.archive.service.FileStorageService;
import com.storyloom.archive.service.BookEmbeddingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class AdminController {

    @Autowired
    private BookService bookService;

    @Autowired
    private FileStorageService fileStorageService; 

    @Autowired
    private BookEmbeddingService bookEmbeddingService;

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
            @RequestParam(value = "epubFile", required = false) MultipartFile epubFile,
            RedirectAttributes redirectAttributes) { 

        try {
            if (title == null || title.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Upload failed: Title cannot be empty.");
                return "redirect:/admin";
            }
            if (authorName == null || authorName.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Upload failed: Author name cannot be empty.");
                return "redirect:/admin";
            }

            if (coverImage != null && !coverImage.isEmpty()) {
                String filename = coverImage.getOriginalFilename();
                if (filename == null || !(filename.toLowerCase().endsWith(".jpg") || filename.toLowerCase().endsWith(".jpeg") || filename.toLowerCase().endsWith(".png"))) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Upload failed: Cover image must be a JPG or PNG.");
                    return "redirect:/admin";
                }
            }
            
            String rawBookText = "";
            if (textFile != null && !textFile.isEmpty()) {
                String filename = textFile.getOriginalFilename();
                if (filename == null || !filename.toLowerCase().endsWith(".txt")) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Upload failed: Plain text file must be a .txt format.");
                    return "redirect:/admin";
                }
                rawBookText = new String(textFile.getBytes(), StandardCharsets.UTF_8);
            }
            
            if (epubFile != null && !epubFile.isEmpty()) {
                String filename = epubFile.getOriginalFilename();
                if (filename == null || !filename.toLowerCase().endsWith(".epub")) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Upload failed: EPUB file must be a .epub format.");
                    return "redirect:/admin";
                }
            }

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

            newBook = bookService.saveBook(newBook);
            
            // FEED THE AI
            if (!rawBookText.isEmpty()) {
                bookEmbeddingService.embedBookIntoDatabase(newBook, rawBookText);
            } else if (synopsis != null && !synopsis.isEmpty()) {
                bookEmbeddingService.embedBookIntoDatabase(newBook, synopsis);
            }

            redirectAttributes.addFlashAttribute("successMessage", "Successfully uploaded: " + newBook.getTitle());
            return "redirect:/admin"; 

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Upload failed! System Error: " + e.getMessage());
            return "redirect:/admin";
        }
    }
}