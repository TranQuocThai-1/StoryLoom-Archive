package com.storyloom.archive.controller;

import com.storyloom.archive.repository.BookInteractionRepository;
import com.storyloom.archive.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class NavigationController {

    @Autowired
    private BookInteractionRepository interactionRepository;

    @Autowired
    private BookRepository bookRepository; 

    @GetMapping("/")
    public String showHomePage(Model model) {
        Pageable recentPage = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"));
        model.addAttribute("featuredBooks", bookRepository.findAll(recentPage).getContent());

        LocalDateTime lastMonth = LocalDateTime.now().minusDays(30);
        Pageable top10 = PageRequest.of(0, 10);
        model.addAttribute("popularBooks", interactionRepository.findTrendingBooks(lastMonth, top10));

        return "index"; 
    }

    @GetMapping("/about")
    public String showAbout() {
        return "about";
    }

    @GetMapping("/admin")
    public String showAdminDashboard() {
        return "admin"; 
    }

    @GetMapping("/categories")
    public String showCategories(Model model) {
        model.addAttribute("categories", bookRepository.findAllDistinctCategories());
        return "categories";
    }

    @GetMapping("/frequently-downloaded")
    public String frequentlyDownloaded(Model model) { 
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        LocalDateTime lastWeek = LocalDateTime.now().minusDays(7);
        LocalDateTime lastMonth = LocalDateTime.now().minusDays(30);
        
        Pageable top20 = PageRequest.of(0, 20);

        model.addAttribute("topBooksYesterday", interactionRepository.findTrendingBooks(yesterday, top20));
        model.addAttribute("topBooks7Days", interactionRepository.findTrendingBooks(lastWeek, top20));
        model.addAttribute("topBooks30Days", interactionRepository.findTrendingBooks(lastMonth, top20));

        model.addAttribute("topAuthorsYesterday", 
            interactionRepository.findTrendingAuthors(yesterday, top20)
                .stream()
                .map(author -> Map.of("name", author))
                .collect(Collectors.toList()));
                
        model.addAttribute("topAuthors7Days", 
            interactionRepository.findTrendingAuthors(lastWeek, top20)
                .stream()
                .map(author -> Map.of("name", author))
                .collect(Collectors.toList()));
                
        model.addAttribute("topAuthors30Days", 
            interactionRepository.findTrendingAuthors(lastMonth, top20)
                .stream()
                .map(author -> Map.of("name", author))
                .collect(Collectors.toList()));

        return "frequently-downloaded"; 
    }
}