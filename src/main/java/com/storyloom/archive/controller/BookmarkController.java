package com.storyloom.archive.controller;

import com.storyloom.archive.model.Bookmark;
import com.storyloom.archive.model.User;
import com.storyloom.archive.repository.UserRepository;
import com.storyloom.archive.service.BookmarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/bookmarks")
public class BookmarkController {

    @Autowired
    private BookmarkService bookmarkService;

    @Autowired
    private UserRepository userRepository; 

    @GetMapping
    public String showBookmarks(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login"; 
        }

        String email = authentication.getName(); 
        User currentUser = userRepository.findByEmail(email).orElseThrow();
        
        // Use the new Service!
        List<Bookmark> myBookmarks = bookmarkService.getBookmarksForUser(email);
        
        model.addAttribute("bookmarks", myBookmarks);
        model.addAttribute("username", currentUser.getScreenName());
        
        return "bookmarks"; 
    }

    @PostMapping("/add")
    public String addBookmark(@RequestParam("bookId") Long bookId, Authentication authentication) {
        if (authentication == null) return "redirect:/login";

        // Use the new Service!
        bookmarkService.addBookmark(authentication.getName(), bookId);

        return "redirect:/book/" + bookId; 
    }

    @PostMapping("/remove")
    public String removeBookmark(@RequestParam("bookId") Long bookId, Authentication authentication) {
        if (authentication == null) return "redirect:/login";

        // Use the new Service!
        bookmarkService.removeBookmark(authentication.getName(), bookId);

        return "redirect:/bookmarks"; 
    }
}