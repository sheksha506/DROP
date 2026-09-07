package com.drop.drop_auth.dto;

public class AuthResponse {

    private String message;

    private String token;

    private Long userId;

    private String name;

    private String email;

    private String role;


    // ==========================================
    // CONSTRUCTORS
    // ==========================================

    public AuthResponse() {
    }


    public AuthResponse(
            String message,
            String token,
            Long userId,
            String role
    ) {

        this.message = message;
        this.token = token;
        this.userId = userId;
        this.role = role;
    }


    public AuthResponse(
            String message,
            String token,
            Long userId,
            String name,
            String email,
            String role
    ) {

        this.message = message;
        this.token = token;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
    }


    // ==========================================
    // GETTERS
    // ==========================================

    public String getMessage() {
        return message;
    }


    public String getToken() {
        return token;
    }


    public Long getUserId() {
        return userId;
    }


    public String getName() {
        return name;
    }


    public String getEmail() {
        return email;
    }


    public String getRole() {
        return role;
    }


    // ==========================================
    // SETTERS
    // ==========================================

    public void setMessage(String message) {
        this.message = message;
    }


    public void setToken(String token) {
        this.token = token;
    }


    public void setUserId(Long userId) {
        this.userId = userId;
    }


    public void setName(String name) {
        this.name = name;
    }


    public void setEmail(String email) {
        this.email = email;
    }


    public void setRole(String role) {
        this.role = role;
    }
}