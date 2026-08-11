package com.ditix.backend.Core;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                // Preflight OPTIONS — toujours permis
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/ws/**").permitAll()
                // Explicitly deny legacy register route
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/register").denyAll()
                // Routes admin uniquement
                .requestMatchers(HttpMethod.GET, "/api/v1/structures", "/api/v1/structures/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/structures", "/api/v1/structures/**").hasRole("admin")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/structures", "/api/v1/structures/**").hasRole("admin")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/structures", "/api/v1/structures/**").hasRole("admin")
                .requestMatchers(HttpMethod.GET, "/api/v1/ministeres", "/api/v1/ministeres/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/ministeres", "/api/v1/ministeres/**").hasRole("admin")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/ministeres", "/api/v1/ministeres/**").hasRole("admin")
                .requestMatchers(HttpMethod.GET, "/api/v1/cohortes", "/api/v1/cohortes/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/cohortes", "/api/v1/cohortes/**").hasRole("admin")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/cohortes", "/api/v1/cohortes/**").hasRole("admin")
                .requestMatchers("/api/v1/admin/**").hasRole("admin")
                .requestMatchers("/api/v2/admin/**").hasRole("admin")
                .requestMatchers(HttpMethod.GET, "/api/v1/reports/all").authenticated()
                // Tout le reste nécessite un token
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwt ->
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess == null || !realmAccess.containsKey("roles")) {
                return List.of();
            }
            List<String> roles = (List<String>) realmAccess.get("roles");
            return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
        });
        return converter;
    }
}