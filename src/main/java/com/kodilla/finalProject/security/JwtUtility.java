package com.kodilla.finalProject.security;

import com.kodilla.finalProject.errorHandling.InvalidTokenException;
import com.kodilla.finalProject.errorHandling.TokenExpiredException;
import com.kodilla.finalProject.errorHandling.TokenFormatException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtility {
    @Value("${jwt.secret}")
    private String secretKey;

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
        } catch (InvalidTokenException e) {
            throw new InvalidTokenException(e.getMessage());
        }
    }

    public boolean isTokenValid(String token, String username) {
        try {
            return username.equals(extractUsername(token)) && !isTokenExpired(token);
        } catch (RuntimeException e) {
            throw new InvalidTokenException(e.getMessage());
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
                throw new TokenExpiredException();
            }
            return false;
        } catch (InvalidTokenException e) {
            throw new InvalidTokenException(e.getMessage());
        }
    }
    public void validateTokenFormat(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new TokenFormatException();
        }
    }
}
