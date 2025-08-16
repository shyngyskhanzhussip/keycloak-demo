package com.example.demo.controller;

import com.example.demo.dto.OrderDto;
import com.example.demo.entity.Order;
import com.example.demo.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class OrderController {
    
    private final OrderService orderService;
    
    @GetMapping
    @PreAuthorize("hasAnyRole('admin', 'manager', 'employee')")
    public ResponseEntity<List<OrderDto>> getAllOrders(Authentication authentication) {
        return ResponseEntity.ok(orderService.getAllOrders());
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin', 'manager', 'employee')")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('admin', 'manager', 'employee', 'customer')")
    public ResponseEntity<OrderDto> createOrder(@RequestBody OrderDto orderDto, Authentication authentication) {
        return ResponseEntity.ok(orderService.createOrder(orderDto));
    }
    
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('admin', 'manager', 'employee')")
    public ResponseEntity<OrderDto> updateOrderStatus(@PathVariable Long id, @RequestParam Order.OrderStatus status, Authentication authentication) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id, Authentication authentication) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('admin', 'manager', 'employee')")
    public ResponseEntity<List<OrderDto>> getOrdersByStatus(@PathVariable Order.OrderStatus status, Authentication authentication) {
        return ResponseEntity.ok(orderService.getOrdersByStatus(status));
    }
    
    @GetMapping("/customer/{email}")
    @PreAuthorize("hasAnyRole('admin', 'manager', 'employee')")
    public ResponseEntity<List<OrderDto>> getOrdersByCustomerEmail(@PathVariable String email, Authentication authentication) {
        return ResponseEntity.ok(orderService.getOrdersByCustomerEmail(email));
    }
}
