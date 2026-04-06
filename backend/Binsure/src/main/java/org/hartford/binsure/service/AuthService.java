package org.hartford.binsure.service;

import org.hartford.binsure.dto.*;
import org.hartford.binsure.entity.User;
import org.hartford.binsure.enums.UserRole;
import org.hartford.binsure.exception.DuplicateResourceException;
import org.hartford.binsure.exception.InvalidOperationException;
import org.hartford.binsure.exception.InvalidCredentialsException;
import org.hartford.binsure.repository.BusinessRepository;
import org.hartford.binsure.repository.UserRepository;
import org.hartford.binsure.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service

public class AuthService {

    @Autowired
    private BusinessRepository businessRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtTokenProvider tokenProvider;

    // LOGIN — returns token with email + role embedded
    public AuthResponse login(AuthLoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (org.springframework.security.core.AuthenticationException e) {
            throw new InvalidCredentialsException("Invalid Email ID or Password");
        }

        // Token contains all necessary user claims
        String token = tokenProvider.generateToken(request.getEmail());

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .build();
    }

    // REGISTER — creates User + Business, returns token
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        if (request.getCompanyName() == null || request.getCompanyName().trim().isEmpty()) {
            throw new InvalidOperationException("Company name is required for customer registration.");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole(UserRole.CUSTOMER);
        user.setActive(true);
        user = userRepository.save(user);

        org.hartford.binsure.entity.Business business = new org.hartford.binsure.entity.Business();
        business.setUser(user);
        business.setCompanyName(request.getCompanyName());
        business.setCompanyRegNumber(request.getCompanyRegNumber());
        business.setIndustryType(request.getIndustryType());
        business.setAnnualRevenue(request.getAnnualRevenue() != null
                ? new BigDecimal(request.getAnnualRevenue())
                : null);
        business.setNumEmployees(request.getNumEmployees());
        business.setAddressLine1(request.getAddressLine1());
        business.setAddressLine2(request.getAddressLine2());
        business.setCity(request.getCity());
        business.setState(request.getState());
        business.setPostalCode(request.getPostalCode());
        business.setCountry(request.getCountry() != null ? request.getCountry() : "India");
        business.setTaxId(request.getTaxId());
        businessRepository.save(business);

        String token = tokenProvider.generateToken(user.getEmail());

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .build();
    }
}
