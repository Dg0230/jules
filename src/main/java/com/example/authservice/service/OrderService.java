package com.example.authservice.service;

import com.example.authservice.dto.CreateOrderRequestDto;
import com.example.authservice.dto.OrderDto;
import com.example.authservice.dto.UpdateOrderStatusDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;

public interface OrderService {
    OrderDto createOrder(CreateOrderRequestDto orderDto, UserDetails currentUser);
    OrderDto getOrderById(Long id);
    OrderDto getOrderByOrderNumber(String orderNumber);
    Page<OrderDto> getAllOrders(Pageable pageable); // Admin view
    Page<OrderDto> getOrdersByUserId(Long userId, Pageable pageable);
    Page<OrderDto> getOrdersByMerchantId(Long merchantId, Pageable pageable);
    Page<OrderDto> getOrdersByChannelId(Long channelId, Pageable pageable);
    OrderDto updateOrderStatus(Long id, UpdateOrderStatusDto statusDto, UserDetails currentUser);
}
