package com.example.filmservice.service;

import com.example.filmservice.dto.FilmDetailsDto;
import com.example.filmservice.dto.FilmFilterDto;
import com.example.filmservice.dto.FilmListItemDto;
import com.example.filmservice.dto.FilmListRequestDto;
import com.example.filmservice.dto.FilmSaveDto;
import com.example.filmservice.dto.PageDto;
import com.example.filmservice.entity.Director;
import com.example.filmservice.entity.Film;
import com.example.filmservice.exception.BadRequestException;
import com.example.filmservice.exception.NotFoundException;
import com.example.filmservice.repository.DirectorRepository;
import com.example.filmservice.repository.FilmRepository;
import com.example.filmservice.repository.FilmSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.Writer;

@Service
public class FilmService {

    private static final int REPORT_BATCH_SIZE = 1000;
    private static final Sort SORT_BY_ID = Sort.by("id");

    private final FilmRepository filmRepository;
    private final DirectorRepository directorRepository;
    private final FilmMapper filmMapper;

    public FilmService(FilmRepository filmRepository, DirectorRepository directorRepository, FilmMapper filmMapper) {
        this.filmRepository = filmRepository;
        this.directorRepository = directorRepository;
        this.filmMapper = filmMapper;
    }

    @Transactional
    public FilmDetailsDto create(FilmSaveDto dto) {
        Film film = filmRepository.save(filmMapper.toEntity(dto, getReferencedDirector(dto.directorId())));
        return filmMapper.toDetails(film);
    }

    @Transactional(readOnly = true)
    public FilmDetailsDto get(Long id) {
        return filmMapper.toDetails(getFilm(id));
    }

    @Transactional
    public FilmDetailsDto update(Long id, FilmSaveDto dto) {
        Film film = getFilm(id);
        filmMapper.apply(dto, getReferencedDirector(dto.directorId()), film);
        return filmMapper.toDetails(film);
    }

    @Transactional
    public void delete(Long id) {
        filmRepository.delete(getFilm(id));
    }

    @Transactional(readOnly = true)
    public PageDto<FilmListItemDto> list(FilmListRequestDto request) {
        Pageable pageable = PageRequest.of(request.page(), request.size(), SORT_BY_ID);
        Page<Film> page = filmRepository.findAll(FilmSpecifications.byFilter(request.toFilter()), pageable);
        return new PageDto<>(page.map(filmMapper::toListItem).getContent(), page.getTotalPages());
    }

    /**
     * Writes all films matching the filter as CSV. Films are read in batches,
     * so the whole result set is never loaded into memory at once.
     */
    @Transactional(readOnly = true)
    public void writeReport(FilmFilterDto filter, Writer writer) throws IOException {
        FilmCsvWriter csv = new FilmCsvWriter(writer);
        csv.writeHeader();
        Specification<Film> specification = FilmSpecifications.byFilter(filter);
        Pageable pageable = PageRequest.of(0, REPORT_BATCH_SIZE, SORT_BY_ID);
        Page<Film> page;
        do {
            page = filmRepository.findAll(specification, pageable);
            for (Film film : page) {
                csv.writeRow(film);
            }
            pageable = page.nextPageable();
        } while (page.hasNext());
        writer.flush();
    }

    private Film getFilm(Long id) {
        return filmRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Film " + id + " not found"));
    }

    private Director getReferencedDirector(Long directorId) {
        return directorRepository.findById(directorId)
                .orElseThrow(() -> new BadRequestException("Director " + directorId + " does not exist"));
    }
}
