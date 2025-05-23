package com.example.authservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private Long id;
    private String username;
    private String email;
    private String phoneNumber;
    private String status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Set<String> roles; // Set of role names
}
