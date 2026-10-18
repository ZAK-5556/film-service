package com.example.filmservice.controller;

import com.example.filmservice.dto.FilmDetailsDto;
import com.example.filmservice.dto.FilmFilterDto;
import com.example.filmservice.dto.FilmListItemDto;
import com.example.filmservice.dto.FilmListRequestDto;
import com.example.filmservice.dto.FilmSaveDto;
import com.example.filmservice.dto.PageDto;
import com.example.filmservice.dto.UploadResultDto;
import com.example.filmservice.service.FilmImportService;
import com.example.filmservice.service.FilmService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/film")
public class FilmController {

    private static final String REPORT_FILE_NAME = "films_report.csv";

    private final FilmService filmService;
    private final FilmImportService filmImportService;

    public FilmController(FilmService filmService, FilmImportService filmImportService) {
        this.filmService = filmService;
        this.filmImportService = filmImportService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FilmDetailsDto create(@Valid @RequestBody FilmSaveDto dto) {
        return filmService.create(dto);
    }

    @GetMapping("/{id}")
    public FilmDetailsDto get(@PathVariable Long id) {
        return filmService.get(id);
    }

    @PutMapping("/{id}")
    public FilmDetailsDto update(@PathVariable Long id, @Valid @RequestBody FilmSaveDto dto) {
        return filmService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        filmService.delete(id);
    }

    @PostMapping("/_list")
    public PageDto<FilmListItemDto> list(@Valid @RequestBody FilmListRequestDto request) {
        return filmService.list(request);
    }

    @PostMapping("/_report")
    public void report(@RequestBody FilmFilterDto filter, HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment().filename(REPORT_FILE_NAME).build().toString());
        filmService.writeReport(filter, response.getWriter());
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadResultDto upload(@RequestParam("file") MultipartFile file) throws IOException {
        try (InputStream input = file.getInputStream()) {
            return filmImportService.importFilms(input);
        }
    }
}
