package com.example.authservice.service.impl;

import com.example.authservice.dto.RegisterRequest;
import com.example.authservice.dto.UserResponseDto;
import com.example.authservice.entity.Permission;
import com.example.authservice.entity.Role;
import com.example.authservice.entity.User;
import com.example.authservice.exception.BadRequestException;
import com.example.authservice.exception.ResourceNotFoundException;
import com.example.authservice.dto.JwtResponse;
import com.example.authservice.dto.LoginRequest;
import com.example.authservice.repository.PermissionRepository;
import com.example.authservice.repository.RoleRepository;
import com.example.authservice.repository.UserRepository;
import com.example.authservice.security.JwtTokenProvider;
import com.example.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
// @RequiredArgsConstructor will not work correctly if we add new final fields after initial generation.
// Manually creating constructor or ensuring all final fields are present for Lombok.
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager; // Added
    private final JwtTokenProvider jwtTokenProvider; // Added

    private static final String DEFAULT_USER_ROLE = "ROLE_USER"; // Standard Spring Security role prefix

    // Manual Constructor
    public AuthServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PermissionRepository permissionRepository,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    @Transactional
    public UserResponseDto registerUser(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new BadRequestException("Username '" + registerRequest.getUsername() + "' is already taken.");
        }
        if (registerRequest.getEmail() != null && userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BadRequestException("Email '" + registerRequest.getEmail() + "' is already registered.");
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setPhoneNumber(registerRequest.getPhoneNumber());
        user.setStatus("active"); // Default status

        // Assign default role
        Role userRole = roleRepository.findByName(DEFAULT_USER_ROLE)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(DEFAULT_USER_ROLE);
                    newRole.setDescription("Default role for new users");
                    return roleRepository.save(newRole);
                });
        user.setRoles(new HashSet<>());
        user.getRoles().add(userRole);

        User savedUser = userRepository.save(user);
        return mapUserToUserResponseDto(savedUser);
    }

    @Override
    @Transactional
    public JwtResponse loginUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateToken(authentication);

        // UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        // Or fetch user from DB to get full User entity if more details are needed for JwtResponse
        User user = userRepository.findByUsername(authentication.getName())
            .orElseThrow(() -> new ResourceNotFoundException("User", "username", authentication.getName()));

        List<String> roles = user.getRoles().stream()
                                 .map(Role::getName)
                                 .collect(Collectors.toList());

        return new JwtResponse(jwt, user.getId(), user.getUsername(), user.getEmail(), roles);
    }

    @Override
    @Transactional
    public UserResponseDto assignRoleToUser(Long userId, Long roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));

        user.getRoles().add(role);
        // role.getUsers().add(user); // Ensure bidirectional consistency if not handled by JPA automatically on user.save()
        userRepository.save(user);
        return mapUserToUserResponseDto(user);
    }

    @Override
    @Transactional
    public void assignPermissionToRole(Long roleId, Long permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", permissionId));

        role.getPermissions().add(permission);
        // permission.getRoles().add(role); // Ensure bidirectional consistency
        roleRepository.save(role);
    }

    // --- Helper DTO Mappers ---
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

    private RoleResponseDto mapRoleToRoleResponseDto(Role role) {
        // To be created in RoleService or as a static mapper
        // For now, this structure is just for AuthServiceImpl's needs if it were to return Role details
        if (role == null) return null;
        return new RoleResponseDto(
                role.getId(),
                role.getName(),
                role.getDescription(),
                role.getCreatedAt(),
                role.getUpdatedAt(),
                role.getPermissions().stream().map(Permission::getName).collect(Collectors.toSet())
        );
    }

     private com.example.authservice.dto.RoleResponseDto mapRoleToRoleResponseDto(Role role, boolean mapPermissions) {
        if (role == null) return null;
        com.example.authservice.dto.RoleResponseDto dto = new com.example.authservice.dto.RoleResponseDto();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        dto.setCreatedAt(role.getCreatedAt());
        dto.setUpdatedAt(role.getUpdatedAt());
        if (mapPermissions) {
            dto.setPermissions(role.getPermissions().stream().map(Permission::getName).collect(Collectors.toSet()));
        } else {
            dto.setPermissions(new HashSet<>());
        }
        return dto;
    }
}
