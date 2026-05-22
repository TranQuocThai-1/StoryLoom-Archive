package com.storyloom.archive.controller;

import com.storyloom.archive.model.Book;
import com.storyloom.archive.model.Bookmark;
import com.storyloom.archive.model.User;
import com.storyloom.archive.repository.BookRepository;
import com.storyloom.archive.repository.BookmarkRepository;
import com.storyloom.archive.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/bookmarks")
public class BookmarkController {

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    // --- 1. Display the User's Bookmark Page ---
    @GetMapping
    public String showBookmarks(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login"; // Kick them out if not logged in
        }

        // Get the currently logged-in user
        String email = authentication.getName(); 
        User currentUser = userRepository.findByEmail(email).orElseThrow();

        // Fetch their specific bookmarks, newest first
        List<Bookmark> myBookmarks = bookmarkRepository.findByUserOrderByCreatedAtDesc(currentUser);
        
        model.addAttribute("bookmarks", myBookmarks);
        model.addAttribute("username", currentUser.getScreenName());
        
        return "bookmarks"; // We will build this Mustache template next
    }

    // --- 2. Handle the "Add Bookmark" Button Click ---
    @PostMapping("/add")
    public String addBookmark(@RequestParam("bookId") Long bookId, Authentication authentication) {
        if (authentication == null) return "redirect:/login";

        User currentUser = userRepository.findByEmail(authentication.getName()).orElseThrow();
        Book bookToSave = bookRepository.findById(bookId).orElseThrow();

        // Check if it's already bookmarked to prevent duplicates
        Optional<Bookmark> existing = bookmarkRepository.findByUserAndBook(currentUser, bookToSave);
        if (existing.isEmpty()) {
            bookmarkRepository.save(new Bookmark(currentUser, bookToSave));
        }

        // Send them back to the book page they were just looking at
        return "redirect:/book/" + bookId; 
    }

    // --- 3. Handle the "Remove Bookmark" Button Click ---
    @PostMapping("/remove")
    public String removeBookmark(@RequestParam("bookId") Long bookId, Authentication authentication) {
        if (authentication == null) return "redirect:/login";

        User currentUser = userRepository.findByEmail(authentication.getName()).orElseThrow();
        Book bookToRemove = bookRepository.findById(bookId).orElseThrow();

        bookmarkRepository.findByUserAndBook(currentUser, bookToRemove)
                .ifPresent(bookmark -> bookmarkRepository.delete(bookmark));

        return "redirect:/bookmarks"; // Send back to the dashboard after deleting
    }
}