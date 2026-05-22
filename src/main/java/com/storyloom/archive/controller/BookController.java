package com.storyloom.archive.controller;

import com.storyloom.archive.model.Book;
import com.storyloom.archive.model.BookInteraction;
import com.storyloom.archive.model.User;
import com.storyloom.archive.repository.BookRepository;
import com.storyloom.archive.repository.BookInteractionRepository;
import com.storyloom.archive.repository.AnnotationRepository;
import com.storyloom.archive.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class BookController {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookInteractionRepository interactionRepository;

    @Autowired
    private AnnotationRepository annotationRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/book/{id}")
    public String showBookDetails(@PathVariable Long id, Model model, HttpSession session, Authentication authentication) {
        Optional<Book> bookOptional = bookRepository.findById(id);

        if (bookOptional.isEmpty()) {
            return "redirect:/search";
        }

        Book book = bookOptional.get();

        interactionRepository.save(new BookInteraction(book, "VIEW", session.getId()));

        List<Map<String, String>> bookFiles = new ArrayList<>();

        if (book.getEpubFilePath() != null && !book.getEpubFilePath().isEmpty()) {
            Map<String, String> epub = new HashMap<>();
            epub.put("format", "EPUB");
            epub.put("downloadUrl", "http://127.0.0.1:9000/storyloom-archive/" + book.getEpubFilePath());
            epub.put("size", "Standard"); 
            bookFiles.add(epub);
        }

        if (book.getTextFilePath() != null && !book.getTextFilePath().isEmpty()) {
            Map<String, String> text = new HashMap<>();
            text.put("format", "Plain Text UTF-8");
            text.put("downloadUrl", "http://127.0.0.1:9000/storyloom-archive/" + book.getTextFilePath());
            text.put("size", "Standard");
            bookFiles.add(text);
        }

        Pageable top4 = PageRequest.of(0, 4);
        List<Book> alsoDownloaded = interactionRepository.findBooksAlsoDownloadedBySession(book.getId(), top4);

        if (alsoDownloaded.isEmpty()) {
            alsoDownloaded = bookRepository.findByCategoryIgnoreCase(
                book.getCategory(), 
                Sort.by(Sort.Direction.DESC, "downloadCount")
            );
            alsoDownloaded.removeIf(b -> b.getId().equals(book.getId()));
            
            if (alsoDownloaded.size() > 4) {
                alsoDownloaded = alsoDownloaded.subList(0, 4);
            }
        }

        if (authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")) {
            User currentUser = userRepository.findByEmail(authentication.getName()).orElse(null);
            if (currentUser != null) {
                annotationRepository.findByUserAndBook(currentUser, book)
                        .ifPresent(annotation -> model.addAttribute("annotation", annotation));
            }
        }

        model.addAttribute("alsoDownloaded", alsoDownloaded);
        model.addAttribute("book", book);
        model.addAttribute("bookFiles", bookFiles);
        model.addAttribute("totalBooks", bookRepository.count());

        return "book";
    }

    @GetMapping("/read/{id}")
    public String readBookNatively(@PathVariable Long id, Model model, Authentication authentication) {
        Optional<Book> bookOptional = bookRepository.findById(id);

        if (bookOptional.isEmpty()) {
            return "redirect:/search";
        }

        Book book = bookOptional.get();

        if (book.getTextFilePath() == null || book.getTextFilePath().isEmpty()) {
            return "redirect:/book/" + id; 
        }

        if (authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")) {
            User currentUser = userRepository.findByEmail(authentication.getName()).orElse(null);
            if (currentUser != null) {
                annotationRepository.findByUserAndBook(currentUser, book)
                        .ifPresent(annotation -> model.addAttribute("annotation", annotation));
            }
        }

        model.addAttribute("book", book);
        model.addAttribute("textFileUrl", "http://127.0.0.1:9000/storyloom-archive/" + book.getTextFilePath());

        return "read";
    }
}