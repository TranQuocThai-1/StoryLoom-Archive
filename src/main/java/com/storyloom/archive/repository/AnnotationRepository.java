package com.storyloom.archive.repository;

import com.storyloom.archive.model.Annotation;
import com.storyloom.archive.model.Book;
import com.storyloom.archive.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnnotationRepository extends JpaRepository<Annotation, Long> {
    Optional<Annotation> findByUserAndBook(User user, Book book);
}