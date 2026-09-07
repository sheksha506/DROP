package com.drop.drop_auth.security;

import com.drop.drop_auth.entity.User;
import com.drop.drop_auth.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private final JwtService jwtService;

    private final UserRepository userRepository;


    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserRepository userRepository
    ) {

        this.jwtService = jwtService;

        this.userRepository = userRepository;
    }


    @Override
    protected void doFilterInternal(

            HttpServletRequest request,

            HttpServletResponse response,

            FilterChain filterChain

    ) throws ServletException, IOException {


        // =====================================================
        // GET AUTHORIZATION HEADER
        // =====================================================

        String authorizationHeader =
                request.getHeader("Authorization");


        // No Authorization header
        if (
                authorizationHeader == null
                        ||
                        !authorizationHeader.startsWith("Bearer ")
        ) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }


        // =====================================================
        // EXTRACT TOKEN
        // =====================================================

        String token =
                authorizationHeader.substring(7);


        try {

            // =================================================
            // VALIDATE TOKEN
            // =================================================

            if (!jwtService.isValid(token)) {

                filterChain.doFilter(
                        request,
                        response
                );

                return;
            }


            // =================================================
            // GET EMAIL FROM TOKEN
            // =================================================

            String email =
                    jwtService.extractEmail(
                            token
                    );


            // =================================================
            // FIND USER
            // =================================================

            User user =
                    userRepository
                            .findByEmail(email)
                            .orElse(null);


            if (user == null || !user.isEnabled()) {

                filterChain.doFilter(
                        request,
                        response
                );

                return;
            }


            // =================================================
            // CREATE AUTHENTICATION
            // =================================================

            SimpleGrantedAuthority authority =
                    new SimpleGrantedAuthority(
                            "ROLE_" +
                                    user.getRole().name()
                    );


            UsernamePasswordAuthenticationToken authentication =

                    new UsernamePasswordAuthenticationToken(

                            user,

                            null,

                            List.of(authority)

                    );


            // =================================================
            // STORE AUTHENTICATION
            // =================================================

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(
                            authentication
                    );


        } catch (Exception e) {

            // Invalid token
            SecurityContextHolder
                    .clearContext();
        }


        // Continue request

        filterChain.doFilter(
                request,
                response
        );
    }
}