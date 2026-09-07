package drop_review.drop_review.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Service
public class JwtService {

    private final SecretKey secretKey;


    public JwtService(
            @Value("${drop.jwt.secret}")
            String secret
    ) {

        this.secretKey =
                Keys.hmacShaKeyFor(
                        secret.getBytes(
                                StandardCharsets.UTF_8
                        )
                );
    }


    // =====================================================
    // VALIDATE TOKEN
    // =====================================================

    public boolean isValid(
            String token
    ) {

        try {

            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);

            return true;

        } catch (Exception exception) {

            return false;
        }
    }


    // =====================================================
    // GET USER ID
    // =====================================================

    public Long extractUserId(
            String token
    ) {

        Claims claims =
                Jwts.parser()
                        .verifyWith(secretKey)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();


        Object userId =
                claims.get("userId");


        if (userId == null) {

            throw new RuntimeException(
                    "User ID not found in token"
            );
        }


        return Long.valueOf(
                String.valueOf(userId)
        );
    }


    // =====================================================
    // GET EMAIL
    // =====================================================

    public String extractEmail(
            String token
    ) {

        Claims claims =
                Jwts.parser()
                        .verifyWith(secretKey)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();


        return claims.getSubject();
    }


    // =====================================================
    // GET ROLE
    // =====================================================

    public String extractRole(
            String token
    ) {

        Claims claims =
                Jwts.parser()
                        .verifyWith(secretKey)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();


        return claims.get("role", String.class);
    }
}