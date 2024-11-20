package com.kodilla.finalProject.security;

import java.io.IOException;

import com.kodilla.finalProject.errorHandling.InvalidTokenException;
import com.kodilla.finalProject.errorHandling.TokenFormatException;
import com.kodilla.finalProject.errorHandling.UserWithNameNotFoundException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtility jwtUtility;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        if (isPublicPage(request)) {
            chain.doFilter(request, response);
            return; // Jeśli strona publiczna, nie sprawdzamy tokena
        }

        String authHeader = request.getHeader("Authorization");
        String username = null;
        String token = null;

        // checking, if endpoint needs token
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);  // Usuwamy "Bearer "
            try {
                jwtUtility.validateTokenFormat(token);  // Sprawdzamy format tokenu
                username = jwtUtility.extractUsername(token);
            } catch (InvalidTokenException | TokenFormatException e) {
                throw new InvalidTokenException(e.getMessage());
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = null;
            try {
                userDetails = userDetailsService.loadUserByUsername(username);
            } catch (UsernameNotFoundException e) {
                throw new UserWithNameNotFoundException(username);
            }

            if (jwtUtility.isTokenValid(token, username)) {
                var authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }
        chain.doFilter(request, response);
    }
    private boolean isPublicPage(HttpServletRequest request) {
        // Sprawdzanie, czy żądana ścieżka jest publiczna
        String path = request.getRequestURI();
        return path.equals("/") || path.equals("/v1/login") || path.equals("/register") || path.startsWith("/actuator/");
    }
}
