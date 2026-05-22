package com.storyloom.archive.controller;

import com.storyloom.archive.model.Book;
import com.storyloom.archive.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private BookRepository bookRepository;

    @GetMapping("/catalog/authors")
    public String browseAuthors(
            @RequestParam(name = "letter", defaultValue = "A") String letter, 
            @RequestParam(name = "page", defaultValue = "1") int page,
            Model model) {
            
        List<Book> allBooks;
        if ("0-9".equals(letter)) {
            allBooks = bookRepository.findByAuthorNameStartingWithNumber();
        } else {
            allBooks = bookRepository.findByAuthorNameStartingWithIgnoreCaseOrderByAuthorNameAsc(letter);
        }

        // Pagination logic (50 per page)
        int pageSize = 50;
        int totalResults = allBooks.size();
        int totalPages = (int) Math.ceil((double) totalResults / pageSize);
        
        if (page < 1) page = 1;
        if (totalPages > 0 && page > totalPages) page = totalPages;
        
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalResults);
        
        List<Book> pagedBooks = new ArrayList<>();
        if (totalResults > 0) {
            pagedBooks = allBooks.subList(startIndex, endIndex);
        }

        model.addAttribute("books", pagedBooks);
        model.addAttribute("letter", letter);
        model.addAttribute("pageTitle", "Authors - " + letter.toUpperCase());
        model.addAttribute("pageHeading", "Authors starting with " + letter.toUpperCase());
        
        // Pass the dynamic page numbers to the template
        model.addAttribute("pages", buildPagination(page, totalPages));
        model.addAttribute("hasNext", page < totalPages);
        model.addAttribute("nextPage", page + 1);

        return "catalog-results";
    }

    @GetMapping("/catalog/titles")
    public String browseTitles(
            @RequestParam(name = "letter", defaultValue = "A") String letter, 
            @RequestParam(name = "page", defaultValue = "1") int page,
            Model model) {
            
        List<Book> allBooks;
        if ("0-9".equals(letter)) {
            allBooks = bookRepository.findByTitleStartingWithNumber();
        } else {
            allBooks = bookRepository.findByTitleStartingWithIgnoreCaseOrderByTitleAsc(letter);
        }

        int pageSize = 50;
        int totalResults = allBooks.size();
        int totalPages = (int) Math.ceil((double) totalResults / pageSize);
        
        if (page < 1) page = 1;
        if (totalPages > 0 && page > totalPages) page = totalPages;
        
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalResults);
        
        List<Book> pagedBooks = new ArrayList<>();
        if (totalResults > 0) {
            pagedBooks = allBooks.subList(startIndex, endIndex);
        }

        model.addAttribute("books", pagedBooks);
        model.addAttribute("letter", letter);
        model.addAttribute("pageTitle", "Titles - " + letter.toUpperCase());
        model.addAttribute("pageHeading", "Titles starting with " + letter.toUpperCase());
        
        model.addAttribute("pages", buildPagination(page, totalPages));
        model.addAttribute("hasNext", page < totalPages);
        model.addAttribute("nextPage", page + 1);

        return "catalog-results";
    }

    @GetMapping("/category")
    public String browseCategory(
            @RequestParam(name = "name") String categoryName, 
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "sort", defaultValue = "popularity") String sortParam,
            Model model) {
            
        Sort sort;
        if ("alphabetical".equals(sortParam)) {
            sort = Sort.by(Sort.Direction.ASC, "title");
        } else if ("date".equals(sortParam)) {
            sort = Sort.by(Sort.Direction.DESC, "publishYear");
        } else {
            sort = Sort.by(Sort.Direction.DESC, "downloadCount"); 
        }
            
        List<Book> allBooks = bookRepository.findByCategoryIgnoreCase(categoryName, sort);
        
        int totalResults = allBooks.size();
        int pageSize = 25; 
        int totalPages = (int) Math.ceil((double) totalResults / pageSize);
        
        if (page < 1) page = 1;
        if (totalPages > 0 && page > totalPages) page = totalPages;
        
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalResults);
        
        List<Book> pagedBooks = new ArrayList<>();
        if (totalResults > 0) {
            pagedBooks = allBooks.subList(startIndex, endIndex);
        }
        
        String encodedCategory = "";
        try {
            encodedCategory = URLEncoder.encode(categoryName, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            encodedCategory = categoryName;
        }

        model.addAttribute("books", pagedBooks);
        model.addAttribute("categoryName", categoryName);
        model.addAttribute("encodedCategory", encodedCategory);
        model.addAttribute("totalResults", totalResults);
        
        model.addAttribute("currentSort", sortParam);
        model.addAttribute("sortPopularity", "popularity".equals(sortParam));
        model.addAttribute("sortAlphabetical", "alphabetical".equals(sortParam));
        model.addAttribute("sortDate", "date".equals(sortParam));
        
        model.addAttribute("displayStart", totalResults == 0 ? 0 : startIndex + 1);
        model.addAttribute("displayEnd", endIndex);
        model.addAttribute("hasPrev", page > 1);
        model.addAttribute("prevPage", page - 1);
        model.addAttribute("hasNext", page < totalPages);
        model.addAttribute("nextPage", page + 1);
        
        return "category"; 
    }

    @GetMapping("/author")
    public String browseAuthor(
            @RequestParam(name = "name") String authorName, 
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "sort", defaultValue = "popularity") String sortParam,
            Model model) {
            
        Sort sort;
        if ("alphabetical".equals(sortParam)) {
            sort = Sort.by(Sort.Direction.ASC, "title");
        } else if ("date".equals(sortParam)) {
            sort = Sort.by(Sort.Direction.DESC, "publishYear");
        } else {
            sort = Sort.by(Sort.Direction.DESC, "downloadCount");
        }
            
        List<Book> allBooks = bookRepository.findByAuthorNameIgnoreCase(authorName, sort);
        
        int totalResults = allBooks.size();
        int pageSize = 25; 
        int totalPages = (int) Math.ceil((double) totalResults / pageSize);
        
        if (page < 1) page = 1;
        if (totalPages > 0 && page > totalPages) page = totalPages;
        
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalResults);
        
        List<Book> pagedBooks = new ArrayList<>();
        if (totalResults > 0) {
            pagedBooks = allBooks.subList(startIndex, endIndex);
        }
        
        String encodedAuthor = "";
        try {
            encodedAuthor = URLEncoder.encode(authorName, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            encodedAuthor = authorName;
        }

        model.addAttribute("books", pagedBooks);
        model.addAttribute("authorName", authorName);
        model.addAttribute("encodedAuthor", encodedAuthor);
        model.addAttribute("totalResults", totalResults);
        
        model.addAttribute("currentSort", sortParam);
        model.addAttribute("sortPopularity", "popularity".equals(sortParam));
        model.addAttribute("sortAlphabetical", "alphabetical".equals(sortParam));
        model.addAttribute("sortDate", "date".equals(sortParam));
        
        model.addAttribute("displayStart", totalResults == 0 ? 0 : startIndex + 1);
        model.addAttribute("displayEnd", endIndex);
        model.addAttribute("hasPrev", page > 1);
        model.addAttribute("prevPage", page - 1);
        model.addAttribute("hasNext", page < totalPages);
        model.addAttribute("nextPage", page + 1);
        
        return "author"; 
    }

    // --- NEW QUICK ACTION ROUTES ---

    @GetMapping("/catalog/popular")
    public String browsePopular(@RequestParam(name = "page", defaultValue = "1") int page, Model model) {
        // Fetch ALL books sorted by highest download count
        List<Book> allBooks = bookRepository.findAll(Sort.by(Sort.Direction.DESC, "downloadCount"));

        int pageSize = 50;
        int totalResults = allBooks.size();
        int totalPages = (int) Math.ceil((double) totalResults / pageSize);
        
        if (page < 1) page = 1;
        if (totalPages > 0 && page > totalPages) page = totalPages;
        
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalResults);
        
        List<Book> pagedBooks = new ArrayList<>();
        if (totalResults > 0) {
            pagedBooks = allBooks.subList(startIndex, endIndex);
        }

        model.addAttribute("books", pagedBooks);
        model.addAttribute("letter", "popular"); // Retains URL parameters
        model.addAttribute("pageTitle", "Most Popular Books");
        model.addAttribute("pageHeading", "Most Popular Books in the Archive");
        
        model.addAttribute("pages", buildPagination(page, totalPages));
        model.addAttribute("hasNext", page < totalPages);
        model.addAttribute("nextPage", page + 1);

        return "catalog-results";
    }

    @GetMapping("/catalog/new")
    public String browseNewReleases(@RequestParam(name = "page", defaultValue = "1") int page, Model model) {
        // Fetch ALL books sorted by newest ID
        List<Book> allBooks = bookRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        int pageSize = 50;
        int totalResults = allBooks.size();
        int totalPages = (int) Math.ceil((double) totalResults / pageSize);
        
        if (page < 1) page = 1;
        if (totalPages > 0 && page > totalPages) page = totalPages;
        
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalResults);
        
        List<Book> pagedBooks = new ArrayList<>();
        if (totalResults > 0) {
            pagedBooks = allBooks.subList(startIndex, endIndex);
        }

        model.addAttribute("books", pagedBooks);
        model.addAttribute("letter", "new"); 
        model.addAttribute("pageTitle", "New Releases");
        model.addAttribute("pageHeading", "Recently Added to the Archive");
        
        model.addAttribute("pages", buildPagination(page, totalPages));
        model.addAttribute("hasNext", page < totalPages);
        model.addAttribute("nextPage", page + 1);

        return "catalog-results";
    }

    @GetMapping("/catalog/random")
    public String browseRandom(Model model) {
        // Fetch exactly 20 random books via native SQL
        List<Book> randomBooks = bookRepository.findRandom20Books();

        model.addAttribute("books", randomBooks);
        model.addAttribute("letter", "random");
        model.addAttribute("pageTitle", "Random Suggestions");
        model.addAttribute("pageHeading", "Random Suggestions (Refresh the page for more!)");
        
        // Pass an empty pagination list so the 1, 2, 3 buttons hide themselves
        model.addAttribute("pages", new ArrayList<>());
        model.addAttribute("hasNext", false);

        return "catalog-results";
    }

    // Helper method to generate the list of page numbers for the Mustache template
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