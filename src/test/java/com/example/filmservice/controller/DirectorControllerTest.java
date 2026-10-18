package com.example.filmservice.controller;

import com.example.filmservice.dto.DirectorSaveDto;
import com.example.filmservice.entity.Director;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DirectorControllerTest extends IntegrationTestBase {

    @Test
    void shouldReturnAllDirectorsSortedByName() throws Exception {
        // given
        saveDirector("Wes Anderson");
        saveDirector("Bong Joon-ho");

        // when / then
        mockMvc.perform(get("/api/director"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Bong Joon-ho"))
                .andExpect(jsonPath("$[1].name").value("Wes Anderson"));
    }

    @Test
    void shouldCreateDirector() throws Exception {
        // given
        DirectorSaveDto request = new DirectorSaveDto("  Greta Gerwig ");

        // when / then
        mockMvc.perform(post("/api/director").contentType(MediaType.APPLICATION_JSON).content(toJson(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Greta Gerwig"));
        assertThat(directorRepository.existsByNameIgnoreCase("Greta Gerwig")).isTrue();
    }

    @Test
    void shouldRejectDuplicateDirectorNameIgnoringCase() throws Exception {
        // given
        saveDirector("Greta Gerwig");
        DirectorSaveDto request = new DirectorSaveDto("greta gerwig");

        // when / then
        mockMvc.perform(post("/api/director").contentType(MediaType.APPLICATION_JSON).content(toJson(request)))
                .andExpect(status().isConflict());
        assertThat(directorRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldRejectBlankDirectorName() throws Exception {
        // given
        DirectorSaveDto request = new DirectorSaveDto(" ");

        // when / then
        mockMvc.perform(post("/api/director").contentType(MediaType.APPLICATION_JSON).content(toJson(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists());
    }

    @Test
    void shouldUpdateDirector() throws Exception {
        // given
        Director director = saveDirector("Greta Gerwig");
        DirectorSaveDto request = new DirectorSaveDto("Greta Celeste Gerwig");

        // when / then
        mockMvc.perform(put("/api/director/{id}", director.getId())
                        .contentType(MediaType.APPLICATION_JSON).content(toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Greta Celeste Gerwig"));
        assertThat(directorRepository.findById(director.getId()).orElseThrow().getName())
                .isEqualTo("Greta Celeste Gerwig");
    }

    @Test
    void shouldAllowUpdatingDirectorWithItsOwnName() throws Exception {
        // given
        Director director = saveDirector("Greta Gerwig");
        DirectorSaveDto request = new DirectorSaveDto("GRETA GERWIG");

        // when / then
        mockMvc.perform(put("/api/director/{id}", director.getId())
                        .contentType(MediaType.APPLICATION_JSON).content(toJson(request)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectUpdateToExistingName() throws Exception {
        // given
        saveDirector("Greta Gerwig");
        Director other = saveDirector("Sofia Coppola");
        DirectorSaveDto request = new DirectorSaveDto("Greta Gerwig");

        // when / then
        mockMvc.perform(put("/api/director/{id}", other.getId())
                        .contentType(MediaType.APPLICATION_JSON).content(toJson(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingMissingDirector() throws Exception {
        // given
        DirectorSaveDto request = new DirectorSaveDto("Anyone");

        // when / then
        mockMvc.perform(put("/api/director/{id}", 999_999)
                        .contentType(MediaType.APPLICATION_JSON).content(toJson(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteDirector() throws Exception {
        // given
        Director director = saveDirector("Greta Gerwig");

        // when / then
        mockMvc.perform(delete("/api/director/{id}", director.getId()))
                .andExpect(status().isNoContent());
        assertThat(directorRepository.existsById(director.getId())).isFalse();
    }

    @Test
    void shouldReturnNotFoundWhenDeletingMissingDirector() throws Exception {
        // when / then
        mockMvc.perform(delete("/api/director/{id}", 999_999))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotDeleteDirectorWithFilms() throws Exception {
        // given
        Director director = saveDirector("Christopher Nolan");
        saveFilm("Inception", director, 2010, "USA", "Sci-Fi");

        // when / then
        mockMvc.perform(delete("/api/director/{id}", director.getId()))
                .andExpect(status().isConflict());
        assertThat(directorRepository.existsById(director.getId())).isTrue();
    }
}
