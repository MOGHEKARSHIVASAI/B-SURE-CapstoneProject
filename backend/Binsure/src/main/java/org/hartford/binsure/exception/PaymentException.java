package org.hartford.binsure.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a payment-related rule is violated.
 * Maps to HTTP 400 Bad Request.
 *
 * Usage:
 *   throw new PaymentException("Payment has already been made for this application.");
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PaymentException extends RuntimeException {

    public PaymentException(String message) {
        super(message);
    }
}

