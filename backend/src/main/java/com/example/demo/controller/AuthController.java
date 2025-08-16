package com.example.demo.controller;

import com.example.demo.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final JwtService jwtService;

    @GetMapping("/user-info")
    public ResponseEntity<Map<String, Object>> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        
        Map<String, Object> userInfo = jwtService.getUserInfo(authentication);
        return ResponseEntity.ok(userInfo);
    }

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getUserProfile(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> profile = Map.of(
            "username", jwtService.getCurrentUsername(authentication),
            "email", jwtService.getCurrentUserEmail(authentication),
            "firstName", jwtService.getCurrentUserFirstName(authentication),
            "lastName", jwtService.getCurrentUserLastName(authentication),
            "roles", jwtService.getCurrentUserRoles(authentication),
            "groups", jwtService.getCurrentUserGroups(authentication)
        );

        return ResponseEntity.ok(profile);
    }

    @GetMapping("/roles")
    public ResponseEntity<Map<String, Object>> getUserRoles(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> roles = Map.of(
            "roles", jwtService.getCurrentUserRoles(authentication),
            "groups", jwtService.getCurrentUserGroups(authentication),
            "isAdmin", jwtService.hasRole(authentication, "admin"),
            "isManager", jwtService.hasRole(authentication, "manager"),
            "isEmployee", jwtService.hasRole(authentication, "employee"),
            "isCustomer", jwtService.hasRole(authentication, "customer")
        );

        return ResponseEntity.ok(roles);
    }

    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkAuthentication(Authentication authentication) {
        boolean isAuthenticated = authentication != null && authentication.isAuthenticated();
        
        Map<String, Object> status = Map.of(
            "authenticated", isAuthenticated,
            "username", isAuthenticated ? jwtService.getCurrentUsername(authentication) : null,
            "roles", isAuthenticated ? jwtService.getCurrentUserRoles(authentication) : null
        );

        return ResponseEntity.ok(status);
    }

    @GetMapping("/admin/info")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Map<String, Object>> getAdminInfo(Authentication authentication) {
        Map<String, Object> adminInfo = Map.of(
            "message", "Admin access granted",
            "user", jwtService.getCurrentUsername(authentication),
            "timestamp", System.currentTimeMillis()
        );

        return ResponseEntity.ok(adminInfo);
    }

    @GetMapping("/manager/info")
    @PreAuthorize("hasAnyRole('admin', 'manager')")
    public ResponseEntity<Map<String, Object>> getManagerInfo(Authentication authentication) {
        Map<String, Object> managerInfo = Map.of(
            "message", "Manager access granted",
            "user", jwtService.getCurrentUsername(authentication),
            "timestamp", System.currentTimeMillis()
        );

        return ResponseEntity.ok(managerInfo);
    }

    @GetMapping("/employee/info")
    @PreAuthorize("hasAnyRole('admin', 'manager', 'employee')")
    public ResponseEntity<Map<String, Object>> getEmployeeInfo(Authentication authentication) {
        Map<String, Object> employeeInfo = Map.of(
            "message", "Employee access granted",
            "user", jwtService.getCurrentUsername(authentication),
            "timestamp", System.currentTimeMillis()
        );

        return ResponseEntity.ok(employeeInfo);
    }
}
