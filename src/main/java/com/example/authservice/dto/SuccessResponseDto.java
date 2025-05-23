package com.example.authservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuccessResponseDto<T> {
    private OffsetDateTime timestamp;
    private String message;
    private T data;
    private boolean success;

    public SuccessResponseDto(String message, T data) {
        this.timestamp = OffsetDateTime.now();
        this.message = message;
        this.data = data;
        this.success = true;
    }

    public SuccessResponseDto(String message) {
        this.timestamp = OffsetDateTime.now();
        this.message = message;
        this.success = true;
        this.data = null;
    }
}
