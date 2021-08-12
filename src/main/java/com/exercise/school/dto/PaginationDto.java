package com.exercise.school.dto;

public record PaginationDto(long offset, int pageNumber, int pageSize, boolean last, long totalElements) {
}