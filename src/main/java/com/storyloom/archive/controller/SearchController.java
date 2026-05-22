package com.storyloom.archive.controller;

import com.storyloom.archive.model.Book;
import com.storyloom.archive.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class SearchController {

    @Autowired
    private BookRepository bookRepository;

    @GetMapping("/search")
    public String showSearchForm() {
        return "search";
    }

    @GetMapping("/search/results")
    public String executeSearch(
            @RequestParam(value = "title", required = false, defaultValue = "") String title,
            @RequestParam(value = "author", required = false, defaultValue = "") String author,
            @RequestParam(value = "subject", required = false, defaultValue = "") String subject,
            @RequestParam(value = "language", required = false, defaultValue = "") String language,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            Model model) {

        List<Book> results = bookRepository.findByTitleContainingIgnoreCaseAndAuthorNameContainingIgnoreCase(title, author);

        if (!subject.isEmpty()) {
            results = results.stream()
                    .filter(book -> subject.equalsIgnoreCase(book.getCategory()))
                    .collect(Collectors.toList());
        }

        if (!language.isEmpty()) {
            results = results.stream()
                    .filter(book -> language.equalsIgnoreCase(book.getLanguage()))
                    .collect(Collectors.toList());
        }

        // Build the "You selected:" summary string
        List<String> selections = new ArrayList<>();
        if (!author.isEmpty()) selections.add("Author = " + author);
        if (!title.isEmpty()) selections.add("Title = " + title);
        if (!subject.isEmpty()) selections.add("Subject = " + subject);
        if (!language.isEmpty()) selections.add("Language = " + language);
        
        String selectionText = selections.isEmpty() ? "All Books" : String.join(" and ", selections);

        int totalResults = results.size();
        boolean tooManyResults = totalResults > 1000;

        if (tooManyResults) {
            results = new ArrayList<>(); // Clear results to force the user to refine
        } else {
            // Pagination Math (25 books per page)
            int pageSize = 25;
            int totalPages = (int) Math.ceil((double) totalResults / pageSize);
            
            if (page < 1) page = 1;
            if (totalPages > 0 && page > totalPages) page = totalPages;
            
            int startIndex = (page - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, totalResults);
            
            if (totalResults > 0) {
                results = results.subList(startIndex, endIndex);
            }

            // Create the page numbers for the template
            List<Map<String, Object>> pages = new ArrayList<>();
            for (int i = 1; i <= totalPages; i++) {
                Map<String, Object> p = new HashMap<>();
                p.put("num", i);
                p.put("isCurrent", i == page);
                pages.add(p);
            }

            model.addAttribute("pages", pages);
            model.addAttribute("hasNext", page < totalPages);
            model.addAttribute("nextPage", page + 1);
        }

        // Pass queries back to HTML so pagination links know what to search for
        model.addAttribute("titleQuery", title);
        model.addAttribute("authorQuery", author);
        model.addAttribute("subjectQuery", subject);
        model.addAttribute("languageQuery", language);

        model.addAttribute("books", results);
        model.addAttribute("totalResults", totalResults);
        model.addAttribute("selectionText", selectionText);
        model.addAttribute("tooManyResults", tooManyResults);

        return "search-results";
    }
}