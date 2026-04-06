package org.hartford.binsure.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when an invalid business operation is attempted.
 * Maps to HTTP 400 Bad Request.
 *
 * Usage:
 *   throw new InvalidOperationException("Admin cannot create CUSTOMER from this endpoint.");
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidOperationException extends RuntimeException {

    public InvalidOperationException(String message) {
        super(message);
    }
}

