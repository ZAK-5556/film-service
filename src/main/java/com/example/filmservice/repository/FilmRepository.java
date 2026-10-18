package com.example.filmservice.repository;

import com.example.filmservice.entity.Film;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FilmRepository extends JpaRepository<Film, Long>, JpaSpecificationExecutor<Film> {

    /** Loads the director in the same query to avoid N+1 selects in lists and reports. */
    @Override
    @EntityGraph(attributePaths = "director")
    Page<Film> findAll(Specification<Film> specification, Pageable pageable);

    boolean existsByDirectorId(Long directorId);
}
