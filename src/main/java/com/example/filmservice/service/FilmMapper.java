package com.example.filmservice.service;

import com.example.filmservice.dto.DirectorDto;
import com.example.filmservice.dto.FilmDetailsDto;
import com.example.filmservice.dto.FilmListItemDto;
import com.example.filmservice.dto.FilmSaveDto;
import com.example.filmservice.entity.Director;
import com.example.filmservice.entity.Film;
import org.springframework.stereotype.Component;

/** Converts between {@link Film} entities and DTOs. */
@Component
public class FilmMapper {

    public FilmDetailsDto toDetails(Film film) {
        return new FilmDetailsDto(
                film.getId(),
                film.getTitle(),
                film.getYear(),
                film.getCountry(),
                film.getGenres(),
                DirectorDto.from(film.getDirector()));
    }

    public FilmListItemDto toListItem(Film film) {
        return new FilmListItemDto(film.getId(), film.getTitle(), film.getYear(), film.getDirector().getName());
    }

    public Film toEntity(FilmSaveDto dto, Director director) {
        Film film = new Film();
        apply(dto, director, film);
        return film;
    }

    public void apply(FilmSaveDto dto, Director director, Film film) {
        film.setTitle(dto.title().trim());
        film.setYear(dto.year());
        film.setCountry(trimToNull(dto.country()));
        film.setGenres(trimToNull(dto.genres()));
        film.setDirector(director);
    }

    private static String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
