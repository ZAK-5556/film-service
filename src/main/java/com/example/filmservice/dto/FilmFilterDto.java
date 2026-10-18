package com.example.filmservice.dto;

/** Optional filter criteria; {@code null} or blank values are ignored. */
public record FilmFilterDto(Long directorId, Integer year, String country) {
}
