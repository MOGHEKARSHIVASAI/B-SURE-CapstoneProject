package org.hartford.binsure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.hartford.binsure.entity.User;
import org.hartford.binsure.exception.InvalidOperationException;
import org.hartford.binsure.exception.ResourceNotFoundException;
import org.hartford.binsure.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {
    @Autowired
    private UserRepository userRepository;

    @Value("${app.jwtSecret:mySecretKeyForJWTTokenGenerationAndValidationPurposeLongEnoughForHS512Algorithm}")
    private String jwtSecret;

    @Value("${app.jwtExpirationMs:86400000}")
    private long jwtExpirationMs;

    // Generate token — email as subject, additional user info stored as claims
    public String generateToken(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        if (!user.isActive()) {
            throw new InvalidOperationException("Your account has been deactivated. Please contact admin.");
        }
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        Long businessId = (user.getBusinesses() != null && !user.getBusinesses().isEmpty())
                ? user.getBusinesses().get(0).getId()
                : null;

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("id", user.getId())
                .claim("role", user.getRole().name())
                .claim("firstName", user.getFirstName())
                .claim("lastName", user.getLastName())
                .claim("businessId", businessId)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    // Get email from token
    public String getEmailFromJWT(String token) {
        return getClaims(token).getSubject();
    }

    // Get role from token — no DB call needed
    public String getRoleFromJWT(String token) {
        return (String) getClaims(token).get("role");
    }

    // Get user id from token
    public Long getUserIdFromJWT(String token) {
        return ((Number) getClaims(token).get("id")).longValue();
    }

    // Get business id from token
    public Long getBusinessIdFromJWT(String token) {
        Object businessId = getClaims(token).get("businessId");
        return businessId != null ? ((Number) businessId).longValue() : null;
    }

    // Check if token is valid (not expired, not tampered)
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            System.err.println("JWT invalid: " + e.getMessage());
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
}
