package com.example.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequestDto {

    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must be less than 100 characters")
    private String email;

    @Size(max = 50, message = "Phone number must be less than 50 characters")
    private String phoneNumber;

    @Size(max = 50, message = "Status must be less than 50 characters")
    private String status; // e.g., active, inactive. Consider an Enum for specific values.
}
