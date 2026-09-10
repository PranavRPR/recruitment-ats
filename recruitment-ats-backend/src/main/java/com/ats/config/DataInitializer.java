package com.ats.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.ats.user.Role;
import com.ats.user.User;
import com.ats.user.UserRepository;
import com.ats.user.UserStatus;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner seedAdmin() {
        return args -> {
            if (!userRepository.existsByEmailIgnoreCase("admin@ats.com")) {
                userRepository.save(User.builder()
                        .name("System Admin")
                        .email("admin@ats.com")
                        .password(passwordEncoder.encode("Admin@12345"))
                        .role(Role.ADMIN)
                        .status(UserStatus.ACTIVE)
                        .build());
            }
        };
    }
}
