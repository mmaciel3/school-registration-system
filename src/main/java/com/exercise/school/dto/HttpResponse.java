package com.exercise.school.dto;

import lombok.Builder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Builder
public class HttpResponse {
    @Builder.Default
    private HttpStatus statusCode = HttpStatus.OK;

    private Object responseBody;

    public ResponseEntity<Object> toResponseEntity() {
        return new ResponseEntity<>(responseBody, statusCode);
    }
}
