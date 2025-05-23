package com.example.authservice.controller;

import com.example.authservice.dto.SuccessResponseDto;
import com.example.authservice.dto.UserResponseDto;
import com.example.authservice.dto.UserUpdateRequestDto;
import com.example.authservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(@PageableDefault(size = 20) Pageable pageable) {
        Page<UserResponseDto> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @customPermissionEvaluator.isSelfOrAdmin(authentication, #id)")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        UserResponseDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @customPermissionEvaluator.isSelfOrAdmin(authentication, #id)")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequestDto userUpdateRequestDto) {
        UserResponseDto updatedUser = userService.updateUser(id, userUpdateRequestDto);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponseDto<Object>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(new SuccessResponseDto<>("User deleted successfully with id: " + id));
    }

    @PostMapping("/{userId}/roles/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> assignRoleToUser(@PathVariable Long userId, @PathVariable String roleName) {
        UserResponseDto updatedUser = userService.assignRoleToUser(userId, roleName);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{userId}/roles/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> removeRoleFromUser(@PathVariable Long userId, @PathVariable String roleName) {
        UserResponseDto updatedUser = userService.removeRoleFromUser(userId, roleName);
        return ResponseEntity.ok(updatedUser);
    }
}
