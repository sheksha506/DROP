package drop_review.drop_review.security;

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


    public JwtAuthenticationFilter(
            JwtService jwtService
    ) {

        this.jwtService =
                jwtService;
    }


    @Override
    protected void doFilterInternal(

            HttpServletRequest request,

            HttpServletResponse response,

            FilterChain filterChain

    ) throws ServletException, IOException {


        String authorizationHeader =
                request.getHeader(
                        "Authorization"
                );


        // =====================================================
        // NO TOKEN
        // =====================================================

        if (
                authorizationHeader == null
                        ||
                        !authorizationHeader.startsWith(
                                "Bearer "
                        )
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
                authorizationHeader.substring(
                        7
                );


        try {

            // =================================================
            // VALIDATE TOKEN
            // =================================================

            boolean valid =
                    jwtService.isValid(
                            token
                    );


            if (!valid) {

                filterChain.doFilter(
                        request,
                        response
                );

                return;
            }


            // =================================================
            // EXTRACT USER
            // =================================================

            Long userId =
                    jwtService.extractUserId(
                            token
                    );


            String email =
                    jwtService.extractEmail(
                            token
                    );


            String role =
                    jwtService.extractRole(
                            token
                    );


            if (
                    role == null ||
                            role.isBlank()
            ) {

                filterChain.doFilter(
                        request,
                        response
                );

                return;
            }


            // =================================================
            // CREATE AUTHORITY
            // =================================================

            SimpleGrantedAuthority authority =
                    new SimpleGrantedAuthority(
                            "ROLE_" + role
                    );


            // =================================================
            // CREATE AUTHENTICATION
            // =================================================

            UsernamePasswordAuthenticationToken authentication =

                    new UsernamePasswordAuthenticationToken(

                            userId,

                            null,

                            List.of(authority)

                    );


            // =================================================
            // STORE EMAIL
            // =================================================

            authentication.setDetails(
                    email
            );


            // =================================================
            // SET SECURITY CONTEXT
            // =================================================

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(
                            authentication
                    );


        } catch (Exception exception) {

            SecurityContextHolder
                    .clearContext();
        }


        // =====================================================
        // CONTINUE
        // =====================================================

        filterChain.doFilter(
                request,
                response
        );
    }
}