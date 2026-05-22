package com.storyloom.archive.config;

import com.storyloom.archive.repository.BookRepository;
import org.springframework.boot.CommandLineRunner;

public class DataSeeder implements CommandLineRunner {

    private final BookRepository bookRepository;

    public DataSeeder(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("DataSeeder is bypassed. No dummy data injected.");
    }
}