package com.example.filmservice.repository;

import com.example.filmservice.dto.FilmFilterDto;
import com.example.filmservice.entity.Film;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a DB-level filter for films. Every criterion matches an indexed column
 * (director_id, release_year, country), so filtering stays fast with pagination.
 */
public final class FilmSpecifications {

    private FilmSpecifications() {
    }

    public static Specification<Film> byFilter(FilmFilterDto filter) {
        List<Specification<Film>> criteria = new ArrayList<>();
        if (filter.directorId() != null) {
            criteria.add((root, query, cb) -> cb.equal(root.get("director").get("id"), filter.directorId()));
        }
        if (filter.year() != null) {
            criteria.add((root, query, cb) -> cb.equal(root.get("year"), filter.year()));
        }
        if (filter.country() != null && !filter.country().isBlank()) {
            criteria.add((root, query, cb) -> cb.equal(root.get("country"), filter.country().trim()));
        }
        return Specification.allOf(criteria);
    }
}
