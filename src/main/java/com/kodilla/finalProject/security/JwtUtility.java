package com.kodilla.finalProject.security;

import com.kodilla.finalProject.domain.Role;
import com.kodilla.finalProject.domain.User;
import com.kodilla.finalProject.errorHandling.InvalidTokenException;
import com.kodilla.finalProject.errorHandling.TokenExpiredException;
import com.kodilla.finalProject.errorHandling.TokenFormatException;
import com.kodilla.finalProject.errorHandling.UserWithNameNotFoundException;
import com.kodilla.finalProject.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JwtUtility {
    @Value("${jwt.secret}")
    private String secretKey;
    private UserRepository userRepository;

    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hour
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public String extractUsername(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidTokenException("Invalid token format or token has expired");
        }
    }

    public boolean isTokenValid(String token, String username) {
        try {
            return username.equals(extractUsername(token)) && !isTokenExpired(token);
        } catch (InvalidTokenException e) {
            throw new InvalidTokenException("Token is invalid or expired");
        } catch (RuntimeException e) {
            throw new RuntimeException("Unexpected error while validating token", e);
        }
    }

    private boolean isTokenExpired(String token) {
        try {
            Date expiration = Jwts.parser()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();
            if (expiration.before(new Date())) {
                throw new TokenExpiredException("Token has expired");
            }
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidTokenException("Token parsing failed or token is invalid");
        }
    }
    public void validateTokenFormat(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new TokenFormatException("Token cannot be null or empty");
        }
    }
}
