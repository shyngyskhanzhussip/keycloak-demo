package com.example.demo.dto;

import com.example.demo.entity.Order;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private Long id;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String shippingAddress;
    private Order.OrderStatus status;
    private BigDecimal totalAmount;
    private List<OrderItemDto> orderItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
