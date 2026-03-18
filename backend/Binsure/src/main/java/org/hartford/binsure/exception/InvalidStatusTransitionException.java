package org.hartford.binsure.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a business rule is violated during a status transition.
 * Maps to HTTP 400 Bad Request.
 *
 * Usage:
 *   throw new InvalidStatusTransitionException("Application", "DRAFT", "APPROVED",
 *       "Application must be SUBMITTED first");
 *   → "Cannot transition Application from DRAFT to APPROVED: Application must be SUBMITTED first"
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidStatusTransitionException extends RuntimeException {

    private final String resourceName;
    private final String currentStatus;
    private final String targetStatus;

    public InvalidStatusTransitionException(String resourceName,
                                            String currentStatus,
                                            String targetStatus,
                                            String reason) {
        super(String.format("Cannot transition %s from '%s' to '%s'. Reason: %s",
                resourceName, currentStatus, targetStatus, reason));
        this.resourceName  = resourceName;
        this.currentStatus = currentStatus;
        this.targetStatus  = targetStatus;
    }

    public String getResourceName()  { return resourceName; }
    public String getCurrentStatus() { return currentStatus; }
    public String getTargetStatus()  { return targetStatus; }
}

