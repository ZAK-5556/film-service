package com.example.filmservice.service;

import com.example.filmservice.dto.DirectorDto;
import com.example.filmservice.dto.DirectorSaveDto;
import com.example.filmservice.entity.Director;
import com.example.filmservice.exception.ConflictException;
import com.example.filmservice.exception.NotFoundException;
import com.example.filmservice.repository.DirectorRepository;
import com.example.filmservice.repository.FilmRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DirectorService {

    private final DirectorRepository directorRepository;
    private final FilmRepository filmRepository;

    public DirectorService(DirectorRepository directorRepository, FilmRepository filmRepository) {
        this.directorRepository = directorRepository;
        this.filmRepository = filmRepository;
    }

    @Transactional(readOnly = true)
    public List<DirectorDto> findAll() {
        return directorRepository.findAll(Sort.by("name")).stream()
                .map(DirectorDto::from)
                .toList();
    }

    @Transactional
    public DirectorDto create(DirectorSaveDto dto) {
        String name = dto.name().trim();
        if (directorRepository.existsByNameIgnoreCase(name)) {
            throw duplicateName(name);
        }
        return DirectorDto.from(directorRepository.save(new Director(name)));
    }

    @Transactional
    public DirectorDto update(Long id, DirectorSaveDto dto) {
        Director director = getDirector(id);
        String name = dto.name().trim();
        if (directorRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw duplicateName(name);
        }
        director.setName(name);
        return DirectorDto.from(director);
    }

    @Transactional
    public void delete(Long id) {
        Director director = getDirector(id);
        if (filmRepository.existsByDirectorId(id)) {
            throw new ConflictException("Director " + id + " has films and cannot be deleted");
        }
        directorRepository.delete(director);
    }

    Director getDirector(Long id) {
        return directorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Director " + id + " not found"));
    }

    private static ConflictException duplicateName(String name) {
        return new ConflictException("Director with name '" + name + "' already exists");
    }
}
