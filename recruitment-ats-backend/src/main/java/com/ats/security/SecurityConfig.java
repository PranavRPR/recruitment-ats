package com.ats.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;

    // =========================
    // Password Encoder
    // =========================
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // =========================
    // Authentication Provider
    // =========================
    @Bean
    public AuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }

    // =========================
    // Authentication Manager
    // =========================
    @Bean
    public AuthenticationManager authenticationManager() {

        return authentication -> authenticationProvider()
                .authenticate(authentication);
    }

    // =========================
    // Security Filter Chain
    // =========================
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            .authenticationProvider(authenticationProvider())

            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(jsonAuthenticationEntryPoint())
                .accessDeniedHandler(jsonAccessDeniedHandler())
            )

            .authorizeHttpRequests(auth -> auth

                // Public authentication APIs
                .requestMatchers(
                    "/api/auth/**",
                    "/error"
                ).permitAll()

                // CORS preflight requests do not carry JWT credentials.
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Public job browsing
                .requestMatchers("/api/jobs/recruiter/**").authenticated()
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/jobs/**"
                ).permitAll()

                // Admin
                .requestMatchers(
                    "/api/admin/**"
                ).hasRole("ADMIN")

                // Recruiter
                .requestMatchers(
                    "/api/recruiter/**"
                ).hasAnyRole(
                    "RECRUITER",
                    "ADMIN"
                )

                // Hiring Manager
                .requestMatchers(
                    "/api/hiring-manager/**"
                ).hasAnyRole(
                    "HIRING_MANAGER",
                    "RECRUITER",
                    "ADMIN"
                )

                // Candidate
                .requestMatchers(
                    "/api/candidate/**"
                ).hasAnyRole("CANDIDATE", "ADMIN")

                // Everything else requires login
                .anyRequest().authenticated()
            )

            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    @Bean
    public AuthenticationEntryPoint jsonAuthenticationEntryPoint() {
        return (request, response, exception) -> {
            response.setStatus(401);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"success\":false,\"message\":\"Authentication required\",\"data\":null}");
        };
    }

    @Bean
    public AccessDeniedHandler jsonAccessDeniedHandler() {
        return (request, response, exception) -> {
            response.setStatus(403);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"success\":false,\"message\":\"Access denied\",\"data\":null}");
        };
    }

    // =========================
    // CORS
    // =========================
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(
            "http://localhost:3000",
            "http://localhost:4200",
            "http://localhost:5173"
        ));

        config.setAllowedMethods(List.of(
            "GET",
            "POST",
            "PUT",
            "PATCH",
            "DELETE",
            "OPTIONS"
        ));

        config.setAllowedHeaders(List.of(
            "Authorization",
            "Content-Type",
            "Accept",
            "Origin",
            "X-Requested-With"
        ));

        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
            "/**",
            config
        );

        return source;
    }
}