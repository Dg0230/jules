package com.example.authservice.service;

import com.example.authservice.dto.UserResponseDto;
import com.example.authservice.dto.UserUpdateRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserResponseDto getUserById(Long id);
    UserResponseDto getUserByUsername(String username);
    Page<UserResponseDto> getAllUsers(Pageable pageable);
    UserResponseDto updateUser(Long id, UserUpdateRequestDto userUpdateRequestDto);
    void deleteUser(Long id);
    // createUser might be handled by AuthService.registerUser

    UserResponseDto assignRoleToUser(Long userId, String roleName);
    UserResponseDto removeRoleFromUser(Long userId, String roleName);
}
