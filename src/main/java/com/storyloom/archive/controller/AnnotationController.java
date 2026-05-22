package com.storyloom.archive.controller;

import com.storyloom.archive.model.Annotation;
import com.storyloom.archive.model.Book;
import com.storyloom.archive.model.User;
import com.storyloom.archive.repository.AnnotationRepository;
import com.storyloom.archive.repository.BookRepository;
import com.storyloom.archive.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequestMapping("/annotations")
public class AnnotationController {

    @Autowired
    private AnnotationRepository annotationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @PostMapping("/save")
    public String saveAnnotation(@RequestParam("bookId") Long bookId, 
                                 @RequestParam("content") String content, 
                                 Authentication authentication,
                                 HttpServletRequest request) { 
        if (authentication == null || !authentication.isAuthenticated()) return "redirect:/login";

        User currentUser = userRepository.findByEmail(authentication.getName()).orElseThrow();
        Book book = bookRepository.findById(bookId).orElseThrow();

        Optional<Annotation> existingNote = annotationRepository.findByUserAndBook(currentUser, book);

        if (existingNote.isPresent()) {
            Annotation note = existingNote.get();
            note.setContent(content);
            annotationRepository.save(note);
        } else {
            annotationRepository.save(new Annotation(currentUser, book, content));
        }

        String referer = request.getHeader("Referer");
        if (referer != null) {
            return "redirect:" + referer;
        }
        return "redirect:/book/" + bookId; 
    }
}