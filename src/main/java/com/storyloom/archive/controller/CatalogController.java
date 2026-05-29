package com.storyloom.archive.controller;

import com.storyloom.archive.model.Book;
import com.storyloom.archive.service.BookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class CatalogController {

    private static final Logger logger = LoggerFactory.getLogger(CatalogController.class);

    @Autowired
    private BookService bookService;

    @GetMapping("/catalog/authors")
    public String browseAuthors(
            @RequestParam(name = "letter", defaultValue = "A") String letter, 
            @RequestParam(name = "page", defaultValue = "1") int page,
            Model model) {
            
        logger.info("Accessing /catalog/authors?letter={}&page={}", letter, page);
        
        int pageSize = 50;
        Page<Book> bookPage = bookService.getBooksByAuthorLetter(letter, page, pageSize);

        model.addAttribute("books", bookPage.getContent());
        model.addAttribute("letter", letter);
        model.addAttribute("pageTitle", "Authors - " + letter.toUpperCase());
        model.addAttribute("pageHeading", "Authors starting with " + letter.toUpperCase());
        
        model.addAttribute("pages", buildPagination(page, bookPage.getTotalPages()));
        model.addAttribute("hasNext", bookPage.hasNext());
        model.addAttribute("nextPage", page + 1);

        return "catalog-results";
    }

    @GetMapping("/catalog/titles")
    public String browseTitles(
            @RequestParam(name = "letter", defaultValue = "A") String letter, 
            @RequestParam(name = "page", defaultValue = "1") int page,
            Model model) {
            
        logger.info("Accessing /catalog/titles?letter={}&page={}", letter, page);
        
        int pageSize = 50;
        Page<Book> bookPage = bookService.getBooksByTitleLetter(letter, page, pageSize);

        model.addAttribute("books", bookPage.getContent());
        model.addAttribute("letter", letter);
        model.addAttribute("pageTitle", "Titles - " + letter.toUpperCase());
        model.addAttribute("pageHeading", "Titles starting with " + letter.toUpperCase());
        
        model.addAttribute("pages", buildPagination(page, bookPage.getTotalPages()));
        model.addAttribute("hasNext", bookPage.hasNext());
        model.addAttribute("nextPage", page + 1);

        return "catalog-results";
    }

    @GetMapping("/category")
    public String browseCategory(
            @RequestParam(name = "name") String categoryName, 
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "sort", defaultValue = "popularity") String sortParam,
            Model model) {
            
        logger.info("Accessing /category?name={}&sort={}&page={}", categoryName, sortParam, page);
            
        Sort sort = determineSort(sortParam);
        int pageSize = 25; 
        
        Page<Book> bookPage = bookService.getBooksByCategory(categoryName, page, pageSize, sort);
        
        String encodedCategory = "";
        try {
            encodedCategory = URLEncoder.encode(categoryName, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            logger.error("Failed to encode category name: {}", categoryName, e);
            encodedCategory = categoryName;
        }

        model.addAttribute("books", bookPage.getContent());
        model.addAttribute("categoryName", categoryName);
        model.addAttribute("encodedCategory", encodedCategory);
        model.addAttribute("totalResults", bookPage.getTotalElements());
        
        model.addAttribute("currentSort", sortParam);
        model.addAttribute("sortPopularity", "popularity".equals(sortParam));
        model.addAttribute("sortAlphabetical", "alphabetical".equals(sortParam));
        model.addAttribute("sortDate", "date".equals(sortParam));
        
        long displayStart = bookPage.getTotalElements() == 0 ? 0 : bookPage.getPageable().getOffset() + 1;
        long displayEnd = Math.min(displayStart + pageSize - 1, bookPage.getTotalElements());
        
        model.addAttribute("displayStart", displayStart);
        model.addAttribute("displayEnd", displayEnd);
        model.addAttribute("hasPrev", bookPage.hasPrevious());
        model.addAttribute("prevPage", page - 1);
        model.addAttribute("hasNext", bookPage.hasNext());
        model.addAttribute("nextPage", page + 1);
        
        return "category"; 
    }

    @GetMapping("/author")
    public String browseAuthor(
            @RequestParam(name = "name") String authorName, 
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "sort", defaultValue = "popularity") String sortParam,
            Model model) {
            
        logger.info("Accessing /author?name={}&sort={}&page={}", authorName, sortParam, page);
            
        Sort sort = determineSort(sortParam);
        int pageSize = 25; 
        
        Page<Book> bookPage = bookService.getBooksBySpecificAuthor(authorName, page, pageSize, sort);
        
        String encodedAuthor = "";
        try {
            encodedAuthor = URLEncoder.encode(authorName, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            logger.error("Failed to encode author name: {}", authorName, e);
            encodedAuthor = authorName;
        }

        model.addAttribute("books", bookPage.getContent());
        model.addAttribute("authorName", authorName);
        model.addAttribute("encodedAuthor", encodedAuthor);
        model.addAttribute("totalResults", bookPage.getTotalElements());
        
        model.addAttribute("currentSort", sortParam);
        model.addAttribute("sortPopularity", "popularity".equals(sortParam));
        model.addAttribute("sortAlphabetical", "alphabetical".equals(sortParam));
        model.addAttribute("sortDate", "date".equals(sortParam));
        
        long displayStart = bookPage.getTotalElements() == 0 ? 0 : bookPage.getPageable().getOffset() + 1;
        long displayEnd = Math.min(displayStart + pageSize - 1, bookPage.getTotalElements());

        model.addAttribute("displayStart", displayStart);
        model.addAttribute("displayEnd", displayEnd);
        model.addAttribute("hasPrev", bookPage.hasPrevious());
        model.addAttribute("prevPage", page - 1);
        model.addAttribute("hasNext", bookPage.hasNext());
        model.addAttribute("nextPage", page + 1);
        
        return "author"; 
    }

    @GetMapping("/catalog/popular")
    public String browsePopular(@RequestParam(name = "page", defaultValue = "1") int page, Model model) {
        logger.info("Accessing /catalog/popular?page={}", page);
        
        int pageSize = 50;
        Page<Book> bookPage = bookService.getAllBooksPaged(page, pageSize, Sort.by(Sort.Direction.DESC, "downloadCount"));

        model.addAttribute("books", bookPage.getContent());
        model.addAttribute("letter", "popular"); 
        model.addAttribute("pageTitle", "Most Popular Books");
        model.addAttribute("pageHeading", "Most Popular Books in the Archive");
        
        model.addAttribute("pages", buildPagination(page, bookPage.getTotalPages()));
        model.addAttribute("hasNext", bookPage.hasNext());
        model.addAttribute("nextPage", page + 1);

        return "catalog-results";
    }

    @GetMapping("/catalog/new")
    public String browseNewReleases(@RequestParam(name = "page", defaultValue = "1") int page, Model model) {
        logger.info("Accessing /catalog/new?page={}", page);
        
        int pageSize = 50;
        Page<Book> bookPage = bookService.getAllBooksPaged(page, pageSize, Sort.by(Sort.Direction.DESC, "id"));

        model.addAttribute("books", bookPage.getContent());
        model.addAttribute("letter", "new"); 
        model.addAttribute("pageTitle", "New Releases");
        model.addAttribute("pageHeading", "Recently Added to the Archive");
        
        model.addAttribute("pages", buildPagination(page, bookPage.getTotalPages()));
        model.addAttribute("hasNext", bookPage.hasNext());
        model.addAttribute("nextPage", page + 1);

        return "catalog-results";
    }

    @GetMapping("/catalog/random")
    public String browseRandom(Model model) {
        logger.info("Accessing /catalog/random");
        
        List<Book> randomBooks = bookService.getRandomBooks();

        model.addAttribute("books", randomBooks);
        model.addAttribute("letter", "random");
        model.addAttribute("pageTitle", "Random Suggestions");
        model.addAttribute("pageHeading", "Random Suggestions (Refresh the page for more!)");
        
        model.addAttribute("pages", new ArrayList<>());
        model.addAttribute("hasNext", false);

        return "catalog-results";
    }

    private Sort determineSort(String sortParam) {
        if ("alphabetical".equals(sortParam)) {
            return Sort.by(Sort.Direction.ASC, "title");
        } else if ("date".equals(sortParam)) {
            return Sort.by(Sort.Direction.DESC, "publishYear");
        } else {
            return Sort.by(Sort.Direction.DESC, "downloadCount");
        }
    }

    private List<Map<String, Object>> buildPagination(int currentPage, int totalPages) {
        List<Map<String, Object>> pages = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            Map<String, Object> pageMap = new HashMap<>();
            pageMap.put("num", i);
            pageMap.put("isCurrent", i == currentPage);
            pages.add(pageMap);
        }
        return pages;
    }
}