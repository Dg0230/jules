package com.example.authservice.security;

import com.example.authservice.entity.User; // If using User entity directly for claims
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails; // Standard Spring Security UserDetails
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final SecretKey jwtSecretKey;
    private final long jwtExpirationMs;

    public JwtTokenProvider(@Value("${jwt.secret}") String jwtSecret,
                            @Value("${jwt.expirationMs}") long jwtExpirationMs) {
        if (jwtSecret == null || jwtSecret.length() < 32) { // Ensure secret is strong enough for HMAC-SHA256
             logger.warn("JWT secret is weak or not configured. Using a default, secure key. PLEASE CONFIGURE a strong jwt.secret in properties.");
             // Generate a secure key if not provided or weak, but this should ideally be a consistent, configured key.
             // For HMAC-SHA algorithms, key length must be at least the size of the hash output (e.g., 256 bits for HS256)
             this.jwtSecretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256); // Generates a 256-bit key
        } else {
            this.jwtSecretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        }
        this.jwtExpirationMs = jwtExpirationMs;
    }

    public String generateToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        return buildToken(userPrincipal.getUsername(), getRoles(userPrincipal));
    }

    public String generateToken(UserDetails userDetails) {
        return buildToken(userDetails.getUsername(), getRoles(userDetails));
    }
    
    public String generateTokenForUser(com.example.authservice.entity.User user) {
         List<String> roles = user.getRoles().stream()
                                 .map(role -> role.getName()) // Assuming Role entity has getName()
                                 .collect(Collectors.toList());
        return buildToken(user.getUsername(), roles);
    }


    private List<String> getRoles(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

    private String buildToken(String username, List<String> roles) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles) // Add roles as a claim
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(jwtSecretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtSecretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public List<String> getRolesFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtSecretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("roles", List.class);
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(jwtSecretKey).build().parseClaimsJws(authToken);
            return true;
        } catch (SignatureException ex) {
            logger.error("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }
}
