package com.example.filmservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Request body for {@code POST /api/film/_list}. {@code page} is zero-based.
 */
public record FilmListRequestDto(
        Long directorId,
        Integer year,
        String country,
        @Min(0) Integer page,
        @Min(1) @Max(100) Integer size) {

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;

    public FilmListRequestDto {
        page = page == null ? DEFAULT_PAGE : page;
        size = size == null ? DEFAULT_SIZE : size;
    }

    public FilmFilterDto toFilter() {
        return new FilmFilterDto(directorId, year, country);
    }
}
