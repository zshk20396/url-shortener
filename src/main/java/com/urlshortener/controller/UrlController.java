package com.urlshortener.controller;

import com.urlshortener.model.Url;
import com.urlshortener.service.UrlService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class UrlController {

    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping("/api/shorten")
    public ResponseEntity<ShortenResponse> shorten(@RequestBody ShortenRequest request) {
        if (request.getUrl() == null || request.getUrl().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        Url url = urlService.shortenUrl(request.getUrl());
        String shortUrl = "http://localhost:8080/" + url.getShortCode();

        ShortenResponse response = new ShortenResponse(
                url.getShortCode(),
                shortUrl,
                url.getOriginalUrl()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        Optional<String> originalUrl = urlService.resolveUrl(shortCode);

        if (originalUrl.isPresent()) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", originalUrl.get())
                    .build();
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/api/stats/{shortCode}")
    public ResponseEntity<StatsResponse> stats(@PathVariable String shortCode) {
        Optional<Url> urlOptional = urlService.getStats(shortCode);

        if (urlOptional.isPresent()) {
            Url url = urlOptional.get();
            StatsResponse response = new StatsResponse(
                    url.getShortCode(),
                    url.getOriginalUrl(),
                    url.getHitCount(),
                    url.getCreatedAt()
            );
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.notFound().build();
    }
}