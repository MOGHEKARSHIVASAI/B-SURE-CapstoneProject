package org.hartford.binsure.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hartford.binsure.dto.AuthLoginRequest;
import org.hartford.binsure.dto.AuthResponse;
import org.hartford.binsure.dto.RegisterRequest;
import org.hartford.binsure.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "1. Authentication", description = "Public endpoints — register and login")
public class AuthController {

    @Autowired
    private AuthService authService;

    // POST /api/v1/auth/register
    // Business Customer self-registers — creates User + BusinessCustomer profile
    @PostMapping("/register")
    @Operation(summary = "Register as Business Customer", description = "Creates a new CUSTOMER user account plus a linked business profile. Returns JWT token on success.")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    // POST /api/v1/auth/login
    // Any user (CUSTOMER, ADMIN, UNDERWRITER, CLAIMS_OFFICER) logs in
    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate with email and password. Returns access token.")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthLoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // POST /api/v1/auth/logout
    // Client discards token — stateless JWT, no server-side invalidation
    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Stateless logout — client must discard the JWT token.")
    public ResponseEntity<Map<String, String>> logout() {
        return ResponseEntity.ok(Map.of("message", "Logged out successfully. Please discard your token."));
    }
}
