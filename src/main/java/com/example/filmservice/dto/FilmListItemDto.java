package com.example.filmservice.dto;

/** Short representation of a film used in the paginated list. */
public record FilmListItemDto(Long id, String title, Integer year, String directorName) {
}
