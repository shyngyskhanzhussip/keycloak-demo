package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers(HttpMethod.GET, "/actuator/health/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/h2-console/**").permitAll()
                
                // API endpoints with role-based access
                .requestMatchers(HttpMethod.GET, "/api/products/**").hasAnyRole("admin", "manager", "employee", "customer")
                .requestMatchers(HttpMethod.POST, "/api/products/**").hasAnyRole("admin", "manager")
                .requestMatchers(HttpMethod.PUT, "/api/products/**").hasAnyRole("admin", "manager")
                .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("admin")
                
                .requestMatchers(HttpMethod.GET, "/api/orders/**").hasAnyRole("admin", "manager", "employee")
                .requestMatchers(HttpMethod.POST, "/api/orders/**").hasAnyRole("admin", "manager", "employee", "customer")
                .requestMatchers(HttpMethod.PUT, "/api/orders/**").hasAnyRole("admin", "manager", "employee")
                .requestMatchers(HttpMethod.DELETE, "/api/orders/**").hasRole("admin")
                
                // Admin-only endpoints
                .requestMatchers("/api/admin/**").hasRole("admin")
                
                // All other requests need authentication
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            );

        // Enable H2 console frames (development only)
        http.headers(headers -> headers.frameOptions().sameOrigin());

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(this::extractAuthorities);
        converter.setPrincipalClaimName("preferred_username");
        return converter;
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        // Extract roles from realm_access
        Collection<GrantedAuthority> realmRoles = extractRealmRoles(jwt);
        
        // Extract roles from resource_access
        Collection<GrantedAuthority> resourceRoles = extractResourceRoles(jwt);
        
        // Extract default roles
        Collection<GrantedAuthority> defaultAuthorities = new JwtGrantedAuthoritiesConverter().convert(jwt);
        
        // Combine all authorities
        return Stream.of(realmRoles, resourceRoles, defaultAuthorities)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractRealmRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            Collection<String> roles = (Collection<String>) realmAccess.get("roles");
            return roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractResourceRoles(Jwt jwt) {
        Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
        if (resourceAccess != null && resourceAccess.containsKey("ecommerce-backend")) {
            Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get("ecommerce-backend");
            if (clientAccess.containsKey("roles")) {
                Collection<String> roles = (Collection<String>) clientAccess.get("roles");
                return roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                        .collect(Collectors.toList());
            }
        }
        return List.of();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("http://localhost:4200", "http://127.0.0.1:4200"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
