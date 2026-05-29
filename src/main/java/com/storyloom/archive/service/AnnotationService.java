package com.storyloom.archive.service;

import com.storyloom.archive.model.Annotation;
import com.storyloom.archive.model.Book;
import com.storyloom.archive.model.User;
import com.storyloom.archive.repository.AnnotationRepository;
import com.storyloom.archive.repository.BookRepository;
import com.storyloom.archive.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AnnotationService {

    private static final Logger log = LoggerFactory.getLogger(AnnotationService.class);

    @Autowired
    private AnnotationRepository annotationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    public void saveOrUpdateAnnotation(String email, Long bookId, String content) {
        User user = userRepository.findByEmail(email).orElseThrow();
        Book book = bookRepository.findById(bookId).orElseThrow();

        Optional<Annotation> existingNote = annotationRepository.findByUserAndBook(user, book);

        if (existingNote.isPresent()) {
            Annotation note = existingNote.get();
            note.setContent(content);
            annotationRepository.save(note);
            log.info("Updated existing annotation for book ID {} by user {}", bookId, email);
        } else {
            annotationRepository.save(new Annotation(user, book, content));
            log.info("Created new annotation for book ID {} by user {}", bookId, email);
        }
    }
}