package com.example.filmservice.controller;

import com.example.filmservice.dto.FilmFilterDto;
import com.example.filmservice.dto.FilmListRequestDto;
import com.example.filmservice.dto.FilmSaveDto;
import com.example.filmservice.entity.Director;
import com.example.filmservice.entity.Film;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FilmControllerTest extends IntegrationTestBase {

    private Director nolan;
    private Director bong;

    @BeforeEach
    void createDirectors() {
        nolan = saveDirector("Christopher Nolan");
        bong = saveDirector("Bong Joon-ho");
    }

    // ---------- POST /api/film ----------

    @Test
    void shouldCreateFilm() throws Exception {
        // given
        FilmSaveDto request = new FilmSaveDto("Inception", nolan.getId(), 2010, "USA", "Sci-Fi, Thriller");

        // when / then
        mockMvc.perform(post("/api/film").contentType(MediaType.APPLICATION_JSON).content(toJson(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Inception"))
                .andExpect(jsonPath("$.year").value(2010))
                .andExpect(jsonPath("$.director.name").value("Christopher Nolan"));
        assertThat(filmRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldRejectInvalidFilm() throws Exception {
        // given
        FilmSaveDto request = new FilmSaveDto("", null, 1500, "USA", null);

        // when / then
        mockMvc.perform(post("/api/film").contentType(MediaType.APPLICATION_JSON).content(toJson(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.title").exists())
                .andExpect(jsonPath("$.fieldErrors.directorId").exists())
                .andExpect(jsonPath("$.fieldErrors.year").exists());
        assertThat(filmRepository.count()).isZero();
    }

    @Test
    void shouldRejectFilmWithUnknownDirector() throws Exception {
        // given
        FilmSaveDto request = new FilmSaveDto("Inception", 999_999L, 2010, "USA", null);

        // when / then
        mockMvc.perform(post("/api/film").contentType(MediaType.APPLICATION_JSON).content(toJson(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("999999")));
    }

    // ---------- GET /api/film/{id} ----------

    @Test
    void shouldReturnFilmWithDirector() throws Exception {
        // given
        Film film = saveFilm("Parasite", bong, 2019, "South Korea", "Thriller, Drama");

        // when / then
        mockMvc.perform(get("/api/film/{id}", film.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Parasite"))
                .andExpect(jsonPath("$.country").value("South Korea"))
                .andExpect(jsonPath("$.genres").value("Thriller, Drama"))
                .andExpect(jsonPath("$.director.id").value(bong.getId()))
                .andExpect(jsonPath("$.director.name").value("Bong Joon-ho"));
    }

    @Test
    void shouldReturnNotFoundForMissingFilm() throws Exception {
        // when / then
        mockMvc.perform(get("/api/film/{id}", 999_999))
                .andExpect(status().isNotFound());
    }

    // ---------- PUT /api/film/{id} ----------

    @Test
    void shouldUpdateFilm() throws Exception {
        // given
        Film film = saveFilm("Inception", nolan, 2010, "USA", "Sci-Fi");
        FilmSaveDto request = new FilmSaveDto("Memories of Murder", bong.getId(), 2003, "South Korea", "Crime");

        // when / then
        mockMvc.perform(put("/api/film/{id}", film.getId())
                        .contentType(MediaType.APPLICATION_JSON).content(toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Memories of Murder"))
                .andExpect(jsonPath("$.director.name").value("Bong Joon-ho"));
        Film updated = filmRepository.findById(film.getId()).orElseThrow();
        assertThat(updated.getYear()).isEqualTo(2003);
        assertThat(updated.getCountry()).isEqualTo("South Korea");
    }

    @Test
    void shouldRejectInvalidUpdate() throws Exception {
        // given
        Film film = saveFilm("Inception", nolan, 2010, "USA", "Sci-Fi");
        FilmSaveDto request = new FilmSaveDto(" ", nolan.getId(), 2010, "USA", null);

        // when / then
        mockMvc.perform(put("/api/film/{id}", film.getId())
                        .contentType(MediaType.APPLICATION_JSON).content(toJson(request)))
                .andExpect(status().isBadRequest());
        assertThat(filmRepository.findById(film.getId()).orElseThrow().getTitle()).isEqualTo("Inception");
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingMissingFilm() throws Exception {
        // given
        FilmSaveDto request = new FilmSaveDto("Inception", nolan.getId(), 2010, "USA", null);

        // when / then
        mockMvc.perform(put("/api/film/{id}", 999_999)
                        .contentType(MediaType.APPLICATION_JSON).content(toJson(request)))
                .andExpect(status().isNotFound());
    }

    // ---------- DELETE /api/film/{id} ----------

    @Test
    void shouldDeleteFilm() throws Exception {
        // given
        Film film = saveFilm("Inception", nolan, 2010, "USA", "Sci-Fi");

        // when / then
        mockMvc.perform(delete("/api/film/{id}", film.getId()))
                .andExpect(status().isNoContent());
        assertThat(filmRepository.existsById(film.getId())).isFalse();
    }

    @Test
    void shouldReturnNotFoundWhenDeletingMissingFilm() throws Exception {
        // when / then
        mockMvc.perform(delete("/api/film/{id}", 999_999))
                .andExpect(status().isNotFound());
    }

    // ---------- POST /api/film/_list ----------

    @Test
    void shouldReturnPageFilteredByDirector() throws Exception {
        // given
        saveFilm("Inception", nolan, 2010, "USA", "Sci-Fi");
        saveFilm("Interstellar", nolan, 2014, "USA", "Sci-Fi");
        saveFilm("Tenet", nolan, 2020, "UK", "Action");
        saveFilm("Parasite", bong, 2019, "South Korea", "Drama");
        FilmListRequestDto request = new FilmListRequestDto(nolan.getId(), null, null, 0, 2);

        // when / then
        mockMvc.perform(post("/api/film/_list").contentType(MediaType.APPLICATION_JSON).content(toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.list", hasSize(2)))
                .andExpect(jsonPath("$.list[0].title").value("Inception"))
                .andExpect(jsonPath("$.list[0].directorName").value("Christopher Nolan"))
                .andExpect(jsonPath("$.list[1].title").value("Interstellar"));
    }

    @Test
    void shouldFilterByYearAndCountry() throws Exception {
        // given
        saveFilm("Inception", nolan, 2010, "USA", "Sci-Fi");
        saveFilm("Tenet", nolan, 2020, "UK", "Action");
        saveFilm("Other", bong, 2020, "South Korea", "Drama");
        FilmListRequestDto request = new FilmListRequestDto(null, 2020, "UK", null, null);

        // when / then
        mockMvc.perform(post("/api/film/_list").contentType(MediaType.APPLICATION_JSON).content(toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.list", hasSize(1)))
                .andExpect(jsonPath("$.list[0].title").value("Tenet"));
    }

    @Test
    void shouldReturnEmptyPageWhenNothingMatches() throws Exception {
        // given
        saveFilm("Inception", nolan, 2010, "USA", "Sci-Fi");
        FilmListRequestDto request = new FilmListRequestDto(bong.getId(), null, null, 0, 20);

        // when / then
        mockMvc.perform(post("/api/film/_list").contentType(MediaType.APPLICATION_JSON).content(toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.list", hasSize(0)));
    }

    @Test
    void shouldRejectInvalidPageSize() throws Exception {
        // given
        FilmListRequestDto request = new FilmListRequestDto(null, null, null, 0, 0);

        // when / then
        mockMvc.perform(post("/api/film/_list").contentType(MediaType.APPLICATION_JSON).content(toJson(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.size").exists());
    }

    // ---------- POST /api/film/_report ----------

    @Test
    void shouldGenerateCsvReportWithAllMatchingFilms() throws Exception {
        // given
        saveFilm("Inception", nolan, 2010, "USA", "Sci-Fi, Thriller");
        saveFilm("Interstellar", nolan, 2014, "USA", "Sci-Fi");
        saveFilm("Parasite", bong, 2019, "South Korea", "Drama");
        FilmFilterDto filter = new FilmFilterDto(nolan.getId(), null, null);

        // when
        String csv = mockMvc.perform(post("/api/film/_report")
                        .contentType(MediaType.APPLICATION_JSON).content(toJson(filter)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, containsString("text/csv")))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        containsString("attachment; filename=\"films_report.csv\"")))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        // then
        assertThat(csv).contains("id,title,year,country,genres,director");
        assertThat(csv).contains("Inception,2010,USA,\"Sci-Fi, Thriller\",Christopher Nolan");
        assertThat(csv).contains("Interstellar");
        assertThat(csv).doesNotContain("Parasite");
    }

    // ---------- POST /api/film/upload ----------

    @Test
    void shouldImportValidRecordsAndCountFailedOnes() throws Exception {
        // given
        String json = """
                [
                  {"title": "Inception", "director": "Christopher Nolan", "year": 2010, "country": "USA", "genres": "Sci-Fi"},
                  {"title": "Parasite", "director": "bong joon-ho", "year": "2019", "genres": ["Drama", "Thriller"]},
                  {"title": "Unknown", "director": "Nobody", "year": 2000},
                  {"director": "Christopher Nolan", "year": 2014}
                ]
                """;
        MockMultipartFile file = new MockMultipartFile(
                "file", "films.json", MediaType.APPLICATION_JSON_VALUE, json.getBytes(StandardCharsets.UTF_8));

        // when / then
        mockMvc.perform(multipart("/api/film/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imported").value(2))
                .andExpect(jsonPath("$.failed").value(2))
                .andExpect(jsonPath("$.errors", hasSize(2)));
        assertThat(filmRepository.count()).isEqualTo(2);
    }

    @Test
    void shouldRejectMalformedUploadFile() throws Exception {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file", "films.json", MediaType.APPLICATION_JSON_VALUE, "[{\"title\": ".getBytes(StandardCharsets.UTF_8));

        // when / then
        mockMvc.perform(multipart("/api/film/upload").file(file))
                .andExpect(status().isBadRequest());
        assertThat(filmRepository.count()).isZero();
    }

    @Test
    void shouldRejectUploadThatIsNotArray() throws Exception {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file", "films.json", MediaType.APPLICATION_JSON_VALUE, "{\"title\": \"A\"}".getBytes(StandardCharsets.UTF_8));

        // when / then
        mockMvc.perform(multipart("/api/film/upload").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("JSON array")));
    }
}
