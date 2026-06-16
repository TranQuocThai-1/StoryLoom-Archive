package com.storyloom.archive.controller;

import com.storyloom.archive.model.Book;
import com.storyloom.archive.repository.BookRepository;
import com.storyloom.archive.service.AiAssistantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private AiAssistantService aiAssistantService;

    @Autowired
    private BookRepository bookRepository;

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> globalChatStream(@RequestBody Map<String, String> request) {
        String prompt = request.get("prompt");
        return aiAssistantService.getGlobalRecommendationStream(prompt);
    }

    @PostMapping(value = "/reader-assist", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> readerAssistStream(@RequestBody Map<String, String> request) {
        Long bookId = Long.parseLong(request.get("bookId"));
        String prompt = request.get("prompt");
        String currentText = request.get("currentText");

        Book book = bookRepository.findById(bookId).orElseThrow();
        return aiAssistantService.getReaderAssistanceStream(bookId, book.getTitle(), prompt, currentText);
    }
}