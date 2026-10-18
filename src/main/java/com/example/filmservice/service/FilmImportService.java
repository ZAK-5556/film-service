package com.example.filmservice.service;

import com.example.filmservice.dto.FilmSaveDto;
import com.example.filmservice.dto.UploadResultDto;
import com.example.filmservice.entity.Director;
import com.example.filmservice.exception.BadRequestException;
import com.example.filmservice.repository.DirectorRepository;
import com.example.filmservice.repository.FilmRepository;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Imports films from a JSON file in the Block 1 format:
 * {@code [{"title": "...", "director": "Name", "year": 2010, "country": "...", "genres": "A, B"}]}.
 * The file is read with the Jackson streaming API, one record at a time.
 */
@Service
public class FilmImportService {

    private static final int MAX_REPORTED_ERRORS = 100;

    private final ObjectMapper objectMapper;
    private final FilmRepository filmRepository;
    private final DirectorRepository directorRepository;
    private final FilmMapper filmMapper;
    private final Validator validator;

    public FilmImportService(ObjectMapper objectMapper, FilmRepository filmRepository,
                             DirectorRepository directorRepository, FilmMapper filmMapper, Validator validator) {
        this.objectMapper = objectMapper;
        this.filmRepository = filmRepository;
        this.directorRepository = directorRepository;
        this.filmMapper = filmMapper;
        this.validator = validator;
    }

    /** A malformed file rolls back the whole import, including records read before the error. */
    @Transactional(rollbackFor = IOException.class)
    public UploadResultDto importFilms(InputStream input) throws IOException {
        Map<String, Director> directorsByName = loadDirectorsByName();
        ImportStats stats = new ImportStats();
        try (JsonParser parser = objectMapper.getFactory().createParser(input)) {
            if (parser.nextToken() != JsonToken.START_ARRAY) {
                throw new BadRequestException("Uploaded file must contain a JSON array of films");
            }
            int recordNumber = 0;
            while (parser.nextToken() == JsonToken.START_OBJECT) {
                importRecord(objectMapper.readTree(parser), ++recordNumber, directorsByName, stats);
            }
            if (parser.currentToken() != JsonToken.END_ARRAY) {
                throw new BadRequestException("Uploaded file must contain only JSON objects inside the array");
            }
        }
        return stats.toDto();
    }

    private void importRecord(JsonNode node, int recordNumber, Map<String, Director> directorsByName,
                              ImportStats stats) {
        String directorName = text(node.get("director"));
        Director director = directorName == null ? null : directorsByName.get(normalize(directorName));
        if (director == null) {
            stats.fail(recordNumber, "director '" + directorName + "' not found");
            return;
        }
        FilmSaveDto dto = new FilmSaveDto(text(node.get("title")), director.getId(), year(node.get("year")),
                text(node.get("country")), genres(node.get("genres")));
        Set<ConstraintViolation<FilmSaveDto>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            stats.fail(recordNumber, describe(violations));
            return;
        }
        filmRepository.save(filmMapper.toEntity(dto, director));
        stats.success();
    }

    private Map<String, Director> loadDirectorsByName() {
        return directorRepository.findAll().stream()
                .collect(Collectors.toMap(d -> normalize(d.getName()), Function.identity()));
    }

    private static String normalize(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }

    private static String text(JsonNode node) {
        return node != null && node.isTextual() ? node.asText() : null;
    }

    private static Integer year(JsonNode node) {
        if (node == null) {
            return null;
        }
        if (node.canConvertToInt() && node.isIntegralNumber()) {
            return node.intValue();
        }
        try {
            return node.isTextual() ? Integer.valueOf(node.asText().trim()) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Genres may be a comma-separated string or an array of strings. */
    private static String genres(JsonNode node) {
        if (node == null || !node.isArray()) {
            return text(node);
        }
        List<String> values = new ArrayList<>();
        node.forEach(item -> values.add(item.asText()));
        return String.join(", ", values);
    }

    private static String describe(Set<ConstraintViolation<FilmSaveDto>> violations) {
        return violations.stream()
                .map(v -> v.getPropertyPath() + " " + v.getMessage())
                .sorted()
                .collect(Collectors.joining(", "));
    }

    /** Mutable counters collected during one import. */
    private static final class ImportStats {

        private int imported;
        private int failed;
        private final List<String> errors = new ArrayList<>();

        void success() {
            imported++;
        }

        void fail(int recordNumber, String reason) {
            failed++;
            if (errors.size() < MAX_REPORTED_ERRORS) {
                errors.add("Record " + recordNumber + ": " + reason);
            }
        }

        UploadResultDto toDto() {
            return new UploadResultDto(imported, failed, List.copyOf(errors));
        }
    }
}
