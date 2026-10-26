package com.student.urlshortener.model;

import jakarta.persistence.*;
import java.time.Instant;


@Entity
@Table(name = "url_mappings", indexes = {
        @Index(name = "idx_created_at", columnList = "createdAt")
})
public class UrlMapping {

    @Id
    @Column(length = 16)
    private String shortCode;          // "aB3xY9z"

    @Column(nullable = false, length = 2048)
    private String originalUrl;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Long clickCount;

    public UrlMapping() {
    }

    public UrlMapping(String shortCode, String originalUrl) {
        this.shortCode = shortCode;
        this.originalUrl = originalUrl;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (clickCount == null) {
            clickCount = 0L;
        }
    }

    // --- getters & setters ---

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Long getClickCount() {
        return clickCount;
    }

    public void setClickCount(Long clickCount) {
        this.clickCount = clickCount;
    }

    public static UrlMapping of(String shortCode, String originalUrl) {
        UrlMapping m = new UrlMapping();
        m.setShortCode(shortCode);
        m.setOriginalUrl(originalUrl);
        return m;
    }
}
