package com.storyloom.archive.controller;

import com.storyloom.archive.model.Book;
import com.storyloom.archive.model.Collection;
import com.storyloom.archive.model.User;
import com.storyloom.archive.repository.BookRepository;
import com.storyloom.archive.repository.CollectionRepository;
import com.storyloom.archive.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/shelves")
public class CollectionController {

    @Autowired
    private CollectionRepository collectionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    // --- 1. Display Dashboard & Handle Search ---
    @GetMapping
    public String showShelves(@RequestParam(name = "activeId", required = false) Long activeId,
                              @RequestParam(name = "searchQuery", required = false) String searchQuery,
                              Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) return "redirect:/login";

        User currentUser = userRepository.findByEmail(authentication.getName()).orElseThrow();
        List<Collection> myShelves = collectionRepository.findByUserOrderByNameAsc(currentUser);

        model.addAttribute("shelves", myShelves);

        if (!myShelves.isEmpty()) {
            Collection activeShelf = null;
            if (activeId != null) {
                activeShelf = myShelves.stream().filter(s -> s.getId().equals(activeId)).findFirst().orElse(myShelves.get(0));
            } else {
                activeShelf = myShelves.get(0);
            }
            model.addAttribute("activeShelf", activeShelf);
            model.addAttribute("activeBooks", activeShelf.getBooks());

            // NEW: If the user searched for a book, fetch the results
            if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                List<Book> searchResults = bookRepository.findByTitleContainingIgnoreCase(searchQuery.trim());
                model.addAttribute("searchResults", searchResults);
                model.addAttribute("searchQuery", searchQuery);
            }
        }

        return "shelves"; 
    }

    // --- 2. Create a new empty shelf ---
    @PostMapping("/create")
    public String createShelf(@RequestParam("shelfName") String shelfName, Authentication authentication) {
        if (authentication == null) return "redirect:/login";
        User currentUser = userRepository.findByEmail(authentication.getName()).orElseThrow();
        
        if (shelfName != null && !shelfName.trim().isEmpty()) {
            collectionRepository.save(new Collection(shelfName.trim(), currentUser));
        }
        return "redirect:/shelves";
    }

    // --- 3. NEW: Add a Book to the Active Shelf ---
    @PostMapping("/addBook")
    public String addBookToShelf(@RequestParam("shelfId") Long shelfId, 
                                 @RequestParam("bookId") Long bookId, 
                                 Authentication authentication) {
        if (authentication == null) return "redirect:/login";

        User currentUser = userRepository.findByEmail(authentication.getName()).orElseThrow();
        Collection shelf = collectionRepository.findById(shelfId).orElseThrow();
        
        // Security Check: Make sure the user actually owns this shelf!
        if (shelf.getUser().getId().equals(currentUser.getId())) {
            Book bookToAdd = bookRepository.findById(bookId).orElseThrow();
            
            // Prevent duplicates (don't add if it's already on the shelf)
            if (!shelf.getBooks().contains(bookToAdd)) {
                shelf.getBooks().add(bookToAdd);
                collectionRepository.save(shelf);
            }
        }

        return "redirect:/shelves?activeId=" + shelfId;
    }
}