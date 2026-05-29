package com.storyloom.archive.controller;

import com.storyloom.archive.model.Book;
import com.storyloom.archive.service.BookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController {

    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);

    @Autowired
    private BookService bookService;

    @GetMapping("/search")
    public String showSearchForm(@RequestParam(value = "q", required = false) String q) {
        if (q != null && !q.trim().isEmpty()) {
            return "redirect:/search/results?title=" + q.trim();
        }
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

        logger.info("Executing search: title='{}', author='{}', subject='{}', language='{}', page={}", 
                    title, author, subject, language, page);

        int pageSize = 25;
        
        Page<Book> bookPage = bookService.searchArchive(title, author, subject, language, page, pageSize);

        long totalResults = bookPage.getTotalElements();
        boolean tooManyResults = totalResults > 1000;
        
        List<Book> results = new ArrayList<>();

        if (tooManyResults) {
            logger.warn("Search yielded too many results: {}", totalResults);
        } else {
            results = bookPage.getContent();
            
            List<Map<String, Object>> pages = new ArrayList<>();
            for (int i = 1; i <= bookPage.getTotalPages(); i++) {
                Map<String, Object> p = new HashMap<>();
                p.put("num", i);
                p.put("isCurrent", i == page);
                pages.add(p);
            }
            model.addAttribute("pages", pages);
            model.addAttribute("hasNext", bookPage.hasNext());
            model.addAttribute("nextPage", page + 1);
        }

        List<String> selections = new ArrayList<>();
        if (!author.isEmpty()) selections.add("Author = " + author);
        if (!title.isEmpty()) selections.add("Title = " + title);
        if (!subject.isEmpty()) selections.add("Subject = " + subject);
        if (!language.isEmpty()) selections.add("Language = " + language);
        
        String selectionText = selections.isEmpty() ? "All Books" : String.join(" and ", selections);

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