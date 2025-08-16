package com.example.demo.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JwtService {

    public String getCurrentUsername(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            return jwt.getClaimAsString("preferred_username");
        }
        return authentication.getName();
    }

    public String getCurrentUserEmail(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            return jwt.getClaimAsString("email");
        }
        return null;
    }

    public String getCurrentUserFirstName(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            return jwt.getClaimAsString("given_name");
        }
        return null;
    }

    public String getCurrentUserLastName(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            return jwt.getClaimAsString("family_name");
        }
        return null;
    }

    public List<String> getCurrentUserRoles(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring(5)) // Remove "ROLE_" prefix
                .collect(Collectors.toList());
    }

    public List<String> getCurrentUserGroups(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            @SuppressWarnings("unchecked")
            List<String> groups = (List<String>) jwt.getClaims().get("groups");
            return groups != null ? groups : List.of();
        }
        return List.of();
    }

    public boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("ROLE_" + role.toUpperCase()));
    }

    public boolean hasAnyRole(Authentication authentication, String... roles) {
        for (String role : roles) {
            if (hasRole(authentication, role)) {
                return true;
            }
        }
        return false;
    }

    public Map<String, Object> getUserInfo(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            return Map.of(
                "username", getCurrentUsername(authentication),
                "email", getCurrentUserEmail(authentication),
                "firstName", getCurrentUserFirstName(authentication),
                "lastName", getCurrentUserLastName(authentication),
                "roles", getCurrentUserRoles(authentication),
                "groups", getCurrentUserGroups(authentication),
                "sub", jwt.getSubject(),
                "iss", jwt.getIssuer().toString(),
                "exp", jwt.getExpiresAt(),
                "iat", jwt.getIssuedAt()
            );
        }
        return Map.of("username", authentication.getName());
    }
}
