package com.ats.auth;

import com.ats.security.JwtService;
import com.ats.user.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (request.role() == Role.ADMIN) {
            throw new IllegalArgumentException("Administrator accounts cannot be self-registered");
        }
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email().trim().toLowerCase())
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .status(UserStatus.ACTIVE)
                .build();

        user = userRepository.save(user);

        return toResponse(user, jwtService.generateToken(user));
    }

    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                email,
                        request.password()
                )
        );

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return toResponse(user, jwtService.generateToken(user));
    }

        public AuthResponse currentUser(String email) {
        User user = userRepository.findByEmailIgnoreCase(normalizeEmail(email))
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return toResponse(user, null);
        }

        private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
        }

    private AuthResponse toResponse(User user, String token) {
        return new AuthResponse(
                token,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}
