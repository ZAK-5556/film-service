package com.example.filmservice.dto;

import java.util.List;

public record PageDto<T>(List<T> list, int totalPages) {
}
