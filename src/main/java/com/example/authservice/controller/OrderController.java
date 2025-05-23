package com.example.authservice.controller;

import com.example.authservice.dto.CreateOrderRequestDto;
import com.example.authservice.dto.OrderDto;
import com.example.authservice.dto.SuccessResponseDto; // Assuming this exists for general success
import com.example.authservice.dto.UpdateOrderStatusDto;
import com.example.authservice.entity.User;
import com.example.authservice.repository.UserRepository;
import com.example.authservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository; // To get current user ID

    // Helper to extract UserDetails from Authentication
    private UserDetails getCurrentUserDetails(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null || "anonymousUser".equals(authentication.getPrincipal())) {
            // For @PreAuthorize("isAuthenticated()"), Spring Security ensures auth is not null.
            // This check is more for direct calls if auth could be optional.
            throw new AuthenticationCredentialsNotFoundException("User is not authenticated or is anonymous");
        }
        if (authentication.getPrincipal() instanceof UserDetails) {
            return (UserDetails) authentication.getPrincipal();
        }
        // This case should ideally not be reached if Spring Security is configured correctly with UserDetailsService
        throw new AuthenticationCredentialsNotFoundException("Principal is not an instance of UserDetails");
    }

    // Helper to get current authenticated user's ID
    private Long getCurrentUserId(Authentication authentication) {
        UserDetails userDetails = getCurrentUserDetails(authentication);
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userDetails.getUsername()));
        return currentUser.getId();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody CreateOrderRequestDto createOrderDto, Authentication authentication) {
        UserDetails currentUser = getCurrentUserDetails(authentication);
        OrderDto createdOrder = orderService.createOrder(createOrderDto, currentUser);
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()") // Further checks (isOwner or Admin) can be inside service or with @PostAuthorize
    public ResponseEntity<OrderDto> getOrderById(@PathVariable Long id) {
        // Consider adding a check here or in service if user is not admin and not owner of order
        OrderDto order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/byOrderNumber/{orderNumber}")
    @PreAuthorize("isAuthenticated()") // Similar to getOrderById, further checks might be needed
    public ResponseEntity<OrderDto> getOrderByOrderNumber(@PathVariable String orderNumber) {
        OrderDto order = orderService.getOrderByOrderNumber(orderNumber);
        return ResponseEntity.ok(order);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderDto>> getAllOrders(@PageableDefault(size = 20) Pageable pageable) {
        Page<OrderDto> orders = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/my-orders")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<OrderDto>> getMyOrders(Authentication authentication, @PageableDefault(size = 20) Pageable pageable) {
        Long userId = getCurrentUserId(authentication);
        Page<OrderDto> orders = orderService.getOrdersByUserId(userId, pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/by-merchant/{merchantId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderDto>> getOrdersByMerchantId(@PathVariable Long merchantId, @PageableDefault(size = 20) Pageable pageable) {
        Page<OrderDto> orders = orderService.getOrdersByMerchantId(merchantId, pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/by-channel/{channelId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderDto>> getOrdersByChannelId(@PathVariable Long channelId, @PageableDefault(size = 20) Pageable pageable) {
        Page<OrderDto> orders = orderService.getOrdersByChannelId(channelId, pageable);
        return ResponseEntity.ok(orders);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderDto> updateOrderStatus(@PathVariable Long id, @Valid @RequestBody UpdateOrderStatusDto statusDto, Authentication authentication) {
        UserDetails currentUser = getCurrentUserDetails(authentication); // Admin user performing the action
        OrderDto updatedOrder = orderService.updateOrderStatus(id, statusDto, currentUser);
        return ResponseEntity.ok(updatedOrder);
    }
}
