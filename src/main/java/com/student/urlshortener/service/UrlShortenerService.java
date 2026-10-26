package com.student.urlshortener.service;

import com.student.urlshortener.dto.ShortenResponse;
import com.student.urlshortener.model.UrlMapping;
import com.student.urlshortener.repository.UrlMappingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;

@Service
public class UrlShortenerService {

    private static final Logger log = LoggerFactory.getLogger(UrlShortenerService.class);

    private final UrlMappingRepository repository;
    private final StringRedisTemplate redis;
    private final IdGenerator idGenerator;

    @Value("${app.short-code-length:7}")
    private int shortCodeLength;

    @Value("${app.base-url:http://localhost:8080/}")
    private String baseUrl;

    @Value("${app.cache.ttl-seconds:3600}")
    private long cacheTtlSeconds;

    private static final String CACHE_PREFIX = "url:";

    public UrlShortenerService(UrlMappingRepository repository,
                               StringRedisTemplate redis,
                               IdGenerator idGenerator) {
        this.repository = repository;
        this.redis = redis;
        this.idGenerator = idGenerator;
    }

    @Transactional
    public ShortenResponse shorten(String originalUrl) {
        originalUrl = originalUrl.trim();
        if (!originalUrl.startsWith("http://") && !originalUrl.startsWith("https://")) {
            originalUrl = "https://" + originalUrl;
        }

        long id = idGenerator.nextId();
        String shortCode = Base62Encoder.encode(id, shortCodeLength);

        UrlMapping mapping = UrlMapping.of(shortCode, originalUrl);
        repository.save(mapping);

        redis.opsForValue().set(
                CACHE_PREFIX + shortCode,
                originalUrl,
                Duration.ofSeconds(cacheTtlSeconds)
        );

        log.debug("Created short code {} for {}", shortCode, originalUrl);

        return new ShortenResponse(baseUrl + shortCode, shortCode, originalUrl);
    }

    public Optional<String> resolve(String shortCode) {
        String cacheKey = CACHE_PREFIX + shortCode;

        String cached = redis.opsForValue().get(cacheKey);
        if (cached != null) {
            return Optional.of(cached);
        }

        Optional<UrlMapping> fromDb = repository.findById(shortCode);
        if (fromDb.isEmpty()) {
            return Optional.empty();
        }

        String originalUrl = fromDb.get().getOriginalUrl();

        redis.opsForValue().set(
                cacheKey,
                originalUrl,
                Duration.ofSeconds(cacheTtlSeconds)
        );

        try {
            repository.incrementClickCount(shortCode);
        } catch (Exception e) {
            log.warn("Could not increment click count for {}", shortCode);
        }

        return Optional.of(originalUrl);
    }
}
