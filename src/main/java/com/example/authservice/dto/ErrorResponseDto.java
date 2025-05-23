package com.example.authservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class ErrorResponseDto {
    private OffsetDateTime timestamp;
    private int status;
    private String error; // General error type (e.g., "Bad Request", "Not Found")
    private String message; // More specific error message
    private String path;
    private Map<String, List<String>> details; // For validation errors or more detailed field errors

    public ErrorResponseDto(HttpStatus httpStatus, String message, String path) {
        this.timestamp = OffsetDateTime.now();
        this.status = httpStatus.value();
        this.error = httpStatus.getReasonPhrase();
        this.message = message;
        this.path = path;
    }

    public ErrorResponseDto(HttpStatus httpStatus, String message, String path, Map<String, List<String>> details) {
        this(httpStatus, message, path);
        this.details = details;
    }
}
