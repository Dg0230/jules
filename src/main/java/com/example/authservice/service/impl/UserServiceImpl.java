package com.example.authservice.service.impl;

import com.example.authservice.dto.UserResponseDto;
import com.example.authservice.dto.UserUpdateRequestDto;
import com.example.authservice.entity.Role;
import com.example.authservice.entity.User;
import com.example.authservice.exception.BadRequestException;
import com.example.authservice.exception.ResourceNotFoundException;
import com.example.authservice.repository.RoleRepository;
import com.example.authservice.repository.UserRepository;
import com.example.authservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils; // For StringUtils.hasText

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository; // Added for role operations

    // Constructor updated for RoleRepository
    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return mapUserToUserResponseDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return mapUserToUserResponseDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::mapUserToUserResponseDto);
    }

    @Override
    @Transactional
    public UserResponseDto updateUser(Long id, UserUpdateRequestDto userUpdateRequestDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (StringUtils.hasText(userUpdateRequestDto.getEmail())) {
            // Check if new email is already taken by another user
            userRepository.findByEmail(userUpdateRequestDto.getEmail()).ifPresent(existingUser -> {
                if (!existingUser.getId().equals(user.getId())) {
                    throw new BadRequestException("Email '" + userUpdateRequestDto.getEmail() + "' is already registered by another user.");
                }
            });
            user.setEmail(userUpdateRequestDto.getEmail());
        }

        if (StringUtils.hasText(userUpdateRequestDto.getPhoneNumber())) {
            user.setPhoneNumber(userUpdateRequestDto.getPhoneNumber());
        }

        if (StringUtils.hasText(userUpdateRequestDto.getStatus())) {
            user.setStatus(userUpdateRequestDto.getStatus());
        }

        User updatedUser = userRepository.save(user);
        return mapUserToUserResponseDto(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", "id", id);
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public UserResponseDto assignRoleToUser(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleName));

        if (user.getRoles().contains(role)) {
            throw new BadRequestException("User already has role: " + roleName);
        }
        user.getRoles().add(role);
        userRepository.save(user);
        return mapUserToUserResponseDto(user);
    }

    @Override
    @Transactional
    public UserResponseDto removeRoleFromUser(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleName));

        if (!user.getRoles().contains(role)) {
            throw new BadRequestException("User does not have role: " + roleName);
        }
        user.getRoles().remove(role);
        userRepository.save(user);
        return mapUserToUserResponseDto(user);
    }

    // --- Helper DTO Mapper ---
    private UserResponseDto mapUserToUserResponseDto(User user) {
        if (user == null) return null;
        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getRoles().stream().map(Role::getName).collect(Collectors.toSet())
        );
    }
}
