package com.example.filmservice.dto;

import com.example.filmservice.entity.Director;

public record DirectorDto(Long id, String name) {

    public static DirectorDto from(Director director) {
        return new DirectorDto(director.getId(), director.getName());
    }
}
