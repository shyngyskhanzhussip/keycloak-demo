package com.example.demo.service;

import com.example.demo.dto.OrderDto;
import com.example.demo.dto.OrderItemDto;
import com.example.demo.entity.Order;
import com.example.demo.entity.OrderItem;
import com.example.demo.entity.Product;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    
    public List<OrderDto> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public OrderDto getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        return convertToDto(order);
    }
    
    public OrderDto createOrder(OrderDto orderDto) {
        Order order = convertToEntity(orderDto);
        Order savedOrder = orderRepository.save(order);
        return convertToDto(savedOrder);
    }
    
    public OrderDto updateOrderStatus(Long id, Order.OrderStatus status) {
        Order existingOrder = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        
        existingOrder.setStatus(status);
        Order updatedOrder = orderRepository.save(existingOrder);
        return convertToDto(updatedOrder);
    }
    
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new RuntimeException("Order not found with id: " + id);
        }
        orderRepository.deleteById(id);
    }
    
    public List<OrderDto> getOrdersByStatus(Order.OrderStatus status) {
        return orderRepository.findByStatus(status).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<OrderDto> getOrdersByCustomerEmail(String email) {
        return orderRepository.findByCustomerEmail(email).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    private OrderDto convertToDto(Order order) {
        List<OrderItemDto> orderItemDtos = order.getOrderItems().stream()
                .map(this::convertOrderItemToDto)
                .collect(Collectors.toList());
        
        return new OrderDto(
                order.getId(),
                order.getCustomerName(),
                order.getCustomerEmail(),
                order.getCustomerPhone(),
                order.getShippingAddress(),
                order.getStatus(),
                order.getTotalAmount(),
                orderItemDtos,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
    
    private OrderItemDto convertOrderItemToDto(OrderItem orderItem) {
        return new OrderItemDto(
                orderItem.getId(),
                orderItem.getProduct().getId(),
                orderItem.getProduct().getName(),
                orderItem.getQuantity(),
                orderItem.getUnitPrice(),
                orderItem.getTotalPrice()
        );
    }
    
    private Order convertToEntity(OrderDto orderDto) {
        Order order = new Order();
        order.setCustomerName(orderDto.getCustomerName());
        order.setCustomerEmail(orderDto.getCustomerEmail());
        order.setCustomerPhone(orderDto.getCustomerPhone());
        order.setShippingAddress(orderDto.getShippingAddress());
        order.setStatus(Order.OrderStatus.PENDING);
        order.setTotalAmount(BigDecimal.ZERO);
        
        // Convert order items
        List<OrderItem> orderItems = orderDto.getOrderItems().stream()
                .map(itemDto -> {
                    Product product = productRepository.findById(itemDto.getProductId())
                            .orElseThrow(() -> new RuntimeException("Product not found with id: " + itemDto.getProductId()));
                    
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(order);
                    orderItem.setProduct(product);
                    orderItem.setQuantity(itemDto.getQuantity());
                    orderItem.setUnitPrice(product.getPrice());
                    orderItem.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity())));
                    
                    return orderItem;
                })
                .collect(Collectors.toList());
        
        // Calculate total amount
        BigDecimal totalAmount = orderItems.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);
        
        return order;
    }
}
