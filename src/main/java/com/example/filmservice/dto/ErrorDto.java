package com.example.filmservice.dto;

import java.util.Map;

/** Error response body; {@code fieldErrors} is filled for validation errors only. */
public record ErrorDto(String message, Map<String, String> fieldErrors) {

    public ErrorDto(String message) {
        this(message, Map.of());
    }
}
