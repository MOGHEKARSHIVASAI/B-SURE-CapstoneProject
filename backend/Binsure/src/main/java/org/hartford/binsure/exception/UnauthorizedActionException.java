package org.hartford.binsure.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a user tries to perform an action on a resource they don't own.
 * Maps to HTTP 403 Forbidden.
 *
 * Usage:
 *   throw new UnauthorizedActionException("You do not own this policy application.");
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class UnauthorizedActionException extends RuntimeException {

    public UnauthorizedActionException(String message) {
        super(message);
    }
}

