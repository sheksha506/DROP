package com.drop.drop_auth.controller;

import com.drop.drop_auth.dto.AuthResponse;
import com.drop.drop_auth.dto.LoginRequest;
import com.drop.drop_auth.dto.RegisterRequest;
import com.drop.drop_auth.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {

    private final AuthService authService;


    public AuthController(
            AuthService authService
    ) {

        this.authService = authService;
    }


    // =====================================================
    // REGISTER
    // =====================================================

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(

            @Valid
            @RequestBody
            RegisterRequest request

    ) {

        AuthResponse response =
                authService.register(
                        request
                );


        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    // =====================================================
    // LOGIN
    // =====================================================

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(

            @Valid
            @RequestBody
            LoginRequest request

    ) {

        AuthResponse response =
                authService.login(
                        request
                );


        return ResponseEntity.ok(
                response
        );
    }


    // =====================================================
    // CURRENT USER
    // =====================================================

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser(

            Authentication authentication

    ) {

        String email =
                authentication.getName();


        AuthResponse response =
                authService.getCurrentUser(
                        email
                );


        return ResponseEntity.ok(
                response
        );
    }
}