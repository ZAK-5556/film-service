package com.example.filmservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DirectorSaveDto(
        @NotBlank @Size(max = 100) String name) {
}
