package com.student.urlshortener.service;


public final class Base62Encoder {

    private static final String ALPHABET =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int BASE = ALPHABET.length();

    private Base62Encoder() {}

    public static String encode(long number, int minLength) {
        if (number < 0) {
            throw new IllegalArgumentException("Number must be non-negative");
        }
        if (number == 0) {
            return "0".repeat(Math.max(1, minLength));
        }

        StringBuilder sb = new StringBuilder();
        while (number > 0) {
            int remainder = (int) (number % BASE);
            sb.append(ALPHABET.charAt(remainder));
            number /= BASE;
        }

        String encoded = sb.reverse().toString();

        if (encoded.length() < minLength) {
            encoded = "0".repeat(minLength - encoded.length()) + encoded;
        }
        return encoded;
    }

    public static String encode(long number) {
        return encode(number, 7); 
    }
}
