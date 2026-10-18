package com.example.filmservice.dto;

public record FilmDetailsDto(
        Long id,
        String title,
        Integer year,
        String country,
        String genres,
        DirectorDto director) {
}
