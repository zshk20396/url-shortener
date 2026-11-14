package com.urlshortener.controller;

import java.time.LocalDateTime;

public class StatsResponse {

    private String shortCode;
    private String originalUrl;
    private long hitCount;
    private LocalDateTime createdAt;

    public StatsResponse(String shortCode, String originalUrl, long hitCount, LocalDateTime createdAt) {
        this.shortCode = shortCode;
        this.originalUrl = originalUrl;
        this.hitCount = hitCount;
        this.createdAt = createdAt;
    }

    public String getShortCode() {
        return shortCode;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public long getHitCount() {
        return hitCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}