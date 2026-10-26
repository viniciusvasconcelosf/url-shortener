package com.student.urlshortener.controller;

import com.student.urlshortener.dto.ShortenRequest;
import com.student.urlshortener.dto.ShortenResponse;
import com.student.urlshortener.service.RateLimiterService;
import com.student.urlshortener.service.UrlShortenerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.Map;

@RestController
public class UrlController {

    private final UrlShortenerService shortenerService;
    private final RateLimiterService rateLimiter;

    public UrlController(UrlShortenerService shortenerService, RateLimiterService rateLimiter) {
        this.shortenerService = shortenerService;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping("/api/shorten")
    public ResponseEntity<?> shorten(@Valid @RequestBody ShortenRequest request,
                                     HttpServletRequest httpRequest) {

        String clientIp = extractClientIp(httpRequest);

        if (!rateLimiter.isAllowed(clientIp)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of(
                            "error", "Rate limit exceeded",
                            "message", "Too many requests. Try again later."
                    ));
        }

        ShortenResponse response = shortenerService.shorten(request.getUrl());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        if (shortCode == null || shortCode.length() < 4 || shortCode.length() > 16) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return shortenerService.resolve(shortCode)
                .map(url -> ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create(url))
                        .<Void>build())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Short URL not found"));
    }

    @GetMapping("/api/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "url-shortener");
    }

    private String extractClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
