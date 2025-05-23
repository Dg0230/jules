package com.example.authservice.service;

import com.example.authservice.dto.JwtResponse;
import com.example.authservice.dto.LoginRequest;
import com.example.authservice.dto.RegisterRequest;
import com.example.authservice.dto.UserResponseDto;

public interface AuthService {
    UserResponseDto registerUser(RegisterRequest registerRequest);
    JwtResponse loginUser(LoginRequest loginRequest);
    // void assignRoleToUser(Long userId, String roleName); // Changed roleId to roleName for convenience
    // void removeRoleFromUser(Long userId, String roleName);
    // void assignPermissionToRole(String roleName, String permissionName);
    // void removePermissionFromRole(String roleName, String permissionName);

    // Simpler versions for now as per subtask description (using IDs)
    UserResponseDto assignRoleToUser(Long userId, Long roleId);
    void assignPermissionToRole(Long roleId, Long permissionId);
}
