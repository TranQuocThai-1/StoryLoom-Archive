package com.storyloom.archive.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitingService {
    
    // Stores a unique bucket for every IP Address
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    // Retrieves an existing bucket for an IP, or creates a new one if it's their first time
    public Bucket resolveBucket(String ip) {
        return cache.computeIfAbsent(ip, this::newBucket);
    }

    // Creates the actual bucket using the modernized Bucket4j API
    private Bucket newBucket(String ip) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(5) // Max 5 tokens in the bucket
                .refillGreedy(5, Duration.ofMinutes(1)) // Refill 5 tokens every 1 minute
                .build();
                
        return Bucket.builder().addLimit(limit).build();
    }
}