package com.example.authservice.service.impl;

import com.example.authservice.dto.CreateOrderRequestDto;
import com.example.authservice.dto.OrderDto;
import com.example.authservice.dto.UpdateOrderStatusDto;
import com.example.authservice.entity.Channel;
import com.example.authservice.entity.Merchant;
import com.example.authservice.entity.Order;
import com.example.authservice.entity.User;
import com.example.authservice.exception.BadRequestException;
import com.example.authservice.exception.ResourceNotFoundException;
import com.example.authservice.repository.ChannelRepository;
import com.example.authservice.repository.MerchantRepository;
import com.example.authservice.repository.OrderRepository;
import com.example.authservice.repository.UserRepository;
import com.example.authservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID; // For generating unique order numbers

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final MerchantRepository merchantRepository;
    private final ChannelRepository channelRepository;

    @Override
    @Transactional
    public OrderDto createOrder(CreateOrderRequestDto orderDto, UserDetails currentUser) {
        Order order = new Order();

        // Set User
        User user;
        if (orderDto.getUserId() != null) { // If admin specifies a user
            user = userRepository.findById(orderDto.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", orderDto.getUserId()));
        } else { // Otherwise, use current authenticated user
            user = userRepository.findByUsername(currentUser.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + currentUser.getUsername()));
        }
        order.setUser(user);

        // Validate and set Merchant or Channel (exclusive)
        if (orderDto.getMerchantId() != null && orderDto.getChannelId() != null) {
            throw new BadRequestException("Order cannot be linked to both a Merchant and a Channel.");
        }
        if (orderDto.getMerchantId() != null) {
            Merchant merchant = merchantRepository.findById(orderDto.getMerchantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Merchant", "id", orderDto.getMerchantId()));
            order.setMerchant(merchant);
        } else if (orderDto.getChannelId() != null) {
            Channel channel = channelRepository.findById(orderDto.getChannelId())
                    .orElseThrow(() -> new ResourceNotFoundException("Channel", "id", orderDto.getChannelId()));
            order.setChannel(channel);
        }
        // If neither is set, it's a general order for the user (e.g. platform subscription)

        order.setOrderNumber(generateUniqueOrderNumber());
        order.setAmount(orderDto.getAmount());
        order.setCurrency(orderDto.getCurrency() != null ? orderDto.getCurrency() : "CNY");
        order.setPackageName(orderDto.getPackageName());
        order.setPackageDetails(orderDto.getPackageDetails());
        order.setNotes(orderDto.getNotes());
        order.setStatus("PENDING"); // Initial status

        Order savedOrder = orderRepository.save(order);
        return mapEntityToDto(savedOrder);
    }

    private String generateUniqueOrderNumber() {
        // Simple UUID based order number. Customize as needed (e.g., timestamp + sequence)
        String orderNumber;
        do {
            orderNumber = "ORD-" + UUID.randomUUID().toString().toUpperCase().substring(0, 12);
        } while (orderRepository.existsByOrderNumber(orderNumber));
        return orderNumber;
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        return mapEntityToDto(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto getOrderByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderNumber", orderNumber));
        return mapEntityToDto(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDto> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(this::mapEntityToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDto> getOrdersByUserId(Long userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        return orderRepository.findByUserId(userId, pageable).map(this::mapEntityToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDto> getOrdersByMerchantId(Long merchantId, Pageable pageable) {
        if (!merchantRepository.existsById(merchantId)) {
            throw new ResourceNotFoundException("Merchant", "id", merchantId);
        }
        return orderRepository.findByMerchantId(merchantId, pageable).map(this::mapEntityToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDto> getOrdersByChannelId(Long channelId, Pageable pageable) {
        if (!channelRepository.existsById(channelId)) {
            throw new ResourceNotFoundException("Channel", "id", channelId);
        }
        return orderRepository.findByChannelId(channelId, pageable).map(this::mapEntityToDto);
    }

    @Override
    @Transactional
    public OrderDto updateOrderStatus(Long id, UpdateOrderStatusDto statusDto, UserDetails currentUser) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        
        // Authorization (e.g. only Admin or specific system roles) handled by @PreAuthorize

        order.setStatus(statusDto.getStatus());
        if (statusDto.getPaymentGatewayTransactionId() != null) {
            order.setPaymentGatewayTransactionId(statusDto.getPaymentGatewayTransactionId());
            // Could also set paymentGateway if it's part of this update logic
        }

        Order updatedOrder = orderRepository.save(order);
        return mapEntityToDto(updatedOrder);
    }

    // --- Helper Mapper ---
    private OrderDto mapEntityToDto(Order order) {
        if (order == null) return null;
        OrderDto dto = new OrderDto();
        BeanUtils.copyProperties(order, dto, "user", "merchant", "channel");

        if (order.getUser() != null) {
            dto.setUserId(order.getUser().getId());
            dto.setUsername(order.getUser().getUsername());
        }
        if (order.getMerchant() != null) {
            dto.setMerchantId(order.getMerchant().getId());
            dto.setMerchantName(order.getMerchant().getName());
        }
        if (order.getChannel() != null) {
            dto.setChannelId(order.getChannel().getId());
            dto.setChannelName(order.getChannel().getName());
        }
        return dto;
    }
}
