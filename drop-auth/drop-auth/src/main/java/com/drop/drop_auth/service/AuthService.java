package com.drop.drop_auth.service;

import com.drop.drop_auth.dto.AuthResponse;
import com.drop.drop_auth.dto.LoginRequest;
import com.drop.drop_auth.dto.RegisterRequest;
import com.drop.drop_auth.entity.Roles;
import com.drop.drop_auth.entity.User;
import com.drop.drop_auth.repository.UserRepository;
import com.drop.drop_auth.security.JwtService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;


    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {

        this.userRepository = userRepository;

        this.passwordEncoder = passwordEncoder;

        this.jwtService = jwtService;
    }


    // =====================================================
    // REGISTER
    // =====================================================

    public AuthResponse register(
            RegisterRequest request
    ) {

        String email =
                request.getEmail()
                        .trim()
                        .toLowerCase();


        // Check existing email

        if (userRepository.existsByEmail(email)) {

            throw new RuntimeException(
                    "Email already registered"
            );
        }


        // Create user

        User user = new User();


        user.setName(
                request.getName().trim()
        );


        user.setEmail(email);


        user.setPassword(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );


        user.setRole(Roles.USER);


        user.setEnabled(true);


        // Save user

        User savedUser =
                userRepository.save(user);


        // Generate JWT

        String token =
                jwtService.generateToken(
                        savedUser
                );


        // Return authentication response

        return new AuthResponse(

                "Registration successful",

                token,

                savedUser.getId(),

                savedUser.getName(),

                savedUser.getEmail(),

                savedUser.getRole().name()

        );
    }


    // =====================================================
    // LOGIN
    // =====================================================

    public AuthResponse login(
            LoginRequest request
    ) {

        String email =
                request.getEmail()
                        .trim()
                        .toLowerCase();


        // Find user

        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(
                                () ->
                                        new RuntimeException(
                                                "Invalid email or password"
                                        )
                        );


        // Check account

        if (!user.isEnabled()) {

            throw new RuntimeException(
                    "Account is disabled"
            );
        }


        // Check password

        boolean passwordMatches =
                passwordEncoder.matches(
                        request.getPassword(),
                        user.getPassword()
                );


        if (!passwordMatches) {

            throw new RuntimeException(
                    "Invalid email or password"
            );
        }


        // Generate JWT

        String token =
                jwtService.generateToken(user);


        /*
         * Login response intentionally contains
         * the authentication information required
         * by the frontend.
         *
         * The frontend can use /auth/me for the
         * current user's profile information.
         */

        return new AuthResponse(

                "Login successful",

                token,

                user.getId(),

                user.getName(),

                user.getEmail(),

                user.getRole().name()

        );
    }


    // =====================================================
    // CURRENT USER
    // =====================================================

    public AuthResponse getCurrentUser(
            String email
    ) {

        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(
                                () ->
                                        new RuntimeException(
                                                "User not found"
                                        )
                        );


        return new AuthResponse(

                "Authenticated",

                null,

                user.getId(),

                user.getName(),

                user.getEmail(),

                user.getRole().name()

        );
    }
}