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
            return;
        }

        String authHeader = request.getHeader("Authorization");
        String username = null;
        String token = null;

        // checking, if endpoint needs token
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                jwtUtility.validateTokenFormat(token);
                username = jwtUtility.extractUsername(token);
            } catch (InvalidTokenException e) {
                throw new InvalidTokenException("Token validation failed: " + e.getMessage());
            }
        } else {
            throw new InvalidTokenException("Authorization header is missing or does not contain Bearer token.");
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
        String path = request.getRequestURI();
        return path.equals("/") || path.equals("/v1/login") || path.equals("/v1/users/register") || path.startsWith("/actuator/");
    }
}
