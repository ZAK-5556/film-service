package com.example.filmservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Request body for creating and updating a film. */
public record FilmSaveDto(
        @NotBlank @Size(max = 200) String title,
        @NotNull Long directorId,
        @NotNull @Min(1888) @Max(2100) Integer year,
        @Size(max = 100) String country,
        @Size(max = 300) String genres) {
}
