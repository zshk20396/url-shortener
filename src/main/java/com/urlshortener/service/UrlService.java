package com.urlshortener.service;

import com.urlshortener.model.Url;
import com.urlshortener.repository.UrlRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class UrlService {

    private static final Duration CACHE_TTL = Duration.ofHours(24);
    private static final String CACHE_PREFIX = "url:";

    private final UrlRepository urlRepository;
    private final StringRedisTemplate redisTemplate;

    public UrlService(UrlRepository urlRepository, StringRedisTemplate redisTemplate) {
        this.urlRepository = urlRepository;
        this.redisTemplate = redisTemplate;
    }

    public Url shortenUrl(String originalUrl) {
        Url url = new Url(originalUrl);
        url = urlRepository.save(url);

        String shortCode = Base62Encoder.encode(url.getId());
        url.setShortCode(shortCode);
        url = urlRepository.save(url);

        redisTemplate.opsForValue().set(CACHE_PREFIX + shortCode, originalUrl, CACHE_TTL);

        return url;
    }

    public Optional<String> resolveUrl(String shortCode) {
        String cached = redisTemplate.opsForValue().get(CACHE_PREFIX + shortCode);
        if (cached != null) {
            urlRepository.findByShortCode(shortCode).ifPresent(url -> {
                url.setHitCount(url.getHitCount() + 1);
                urlRepository.save(url);
            });
            return Optional.of(cached);
        }

        Optional<Url> urlOptional = urlRepository.findByShortCode(shortCode);
        if (urlOptional.isPresent()) {
            Url url = urlOptional.get();
            url.setHitCount(url.getHitCount() + 1);
            urlRepository.save(url);

            redisTemplate.opsForValue().set(CACHE_PREFIX + shortCode, url.getOriginalUrl(), CACHE_TTL);

            return Optional.of(url.getOriginalUrl());
        }

        return Optional.empty();
    }

    public Optional<Url> getStats(String shortCode) {
        return urlRepository.findByShortCode(shortCode);
    }
}