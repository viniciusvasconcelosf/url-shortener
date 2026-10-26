package com.student.urlshortener.service;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class IdGenerator {

    private final AtomicLong counter = new AtomicLong(1_000_000_000L);

    public long nextId() {
        return counter.getAndIncrement();
    }
}
