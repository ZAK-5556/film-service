package com.example.filmservice.dto;

import java.util.List;

/**
 * Result of a JSON file import.
 *
 * @param errors description of the first failed records (limited in size)
 */
public record UploadResultDto(int imported, int failed, List<String> errors) {
}
