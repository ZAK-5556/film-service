package com.example.filmservice.controller;

import com.example.filmservice.entity.Director;
import com.example.filmservice.entity.Film;
import com.example.filmservice.repository.DirectorRepository;
import com.example.filmservice.repository.FilmRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Shared setup for controller integration tests: full Spring context, MockMvc,
 * H2 database in PostgreSQL mode (schema created by Liquibase) and a clean DB before each test.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class IntegrationTestBase {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected FilmRepository filmRepository;

    @Autowired
    protected DirectorRepository directorRepository;

    @BeforeEach
    void cleanDatabase() {
        filmRepository.deleteAll();
        directorRepository.deleteAll();
    }

    protected Director saveDirector(String name) {
        return directorRepository.save(new Director(name));
    }

    protected Film saveFilm(String title, Director director, int year, String country, String genres) {
        Film film = new Film();
        film.setTitle(title);
        film.setDirector(director);
        film.setYear(year);
        film.setCountry(country);
        film.setGenres(genres);
        return filmRepository.save(film);
    }

    protected String toJson(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
