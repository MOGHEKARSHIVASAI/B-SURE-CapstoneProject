package org.hartford.binsure.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ─── Helper to build ErrorResponse ───────────────────────────────────────────

    private ResponseEntity<ErrorResponse> build(HttpStatus status,
            String error,
            String message,
            WebRequest request) {
        ErrorResponse body = new ErrorResponse();
        body.setStatus(status.value());
        body.setError(error);
        body.setMessage(message);
        body.setTimestamp(LocalDateTime.now());
        body.setPath(request.getDescription(false).replace("uri=", ""));
        return new ResponseEntity<>(body, status);
    }

    // ─── 404 — Resource Not Found
    // ─────────────────────────────────────────────────

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex,
            WebRequest request) {
        return build(HttpStatus.NOT_FOUND, "Resource Not Found", ex.getMessage(), request);
    }

    // ─── 409 — Duplicate / Conflict
    // ───────────────────────────────────────────────

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateResourceException ex,
            WebRequest request) {
        return build(HttpStatus.CONFLICT, "Duplicate Resource", ex.getMessage(), request);
    }

    // ─── 400 — Invalid Status Transition ─────────────────────────────────────────

    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTransition(InvalidStatusTransitionException ex,
            WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Invalid Status Transition", ex.getMessage(), request);
    }

    // ─── 400 — Invalid Operation ─────────────────────────────────────────────────

    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOperation(InvalidOperationException ex,
            WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Invalid Operation", ex.getMessage(), request);
    }

    // ─── 400 — Payment Error ─────────────────────────────────────────────────────

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ErrorResponse> handlePayment(PaymentException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Payment Error", ex.getMessage(), request);
    }

    // ─── 403 — Unauthorized Action (owns wrong resource) ─────────────────────────

    @ExceptionHandler(UnauthorizedActionException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedAction(UnauthorizedActionException ex,
            WebRequest request) {
        return build(HttpStatus.FORBIDDEN, "Unauthorized Action", ex.getMessage(), request);
    }

    // ─── 403 — Spring Security Access Denied ─────────────────────────────────────

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex,
            WebRequest request) {
        return build(HttpStatus.FORBIDDEN, "Access Denied",
                "You do not have permission to perform this action.", request);
    }

    // ─── 401 — Authentication Errors ─────────────────────────────────────────────

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex,
            WebRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "Authentication Failed",
                "Invalid email or password.", request);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex,
            WebRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "Authentication Failed",
                ex.getMessage(), request);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabled(DisabledException ex, WebRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "Account Disabled",
                "Your account has been deactivated. Please contact admin.", request);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ErrorResponse> handleLocked(LockedException ex, WebRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "Account Locked",
                "Your account has been locked. Please contact admin.", request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex,
            WebRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "Authentication Failed", ex.getMessage(), request);
    }

    // ─── 400 — Validation Errors (@Valid on request body) ────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
            WebRequest request) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }

        // Build a readable message from all field errors
        String message = fieldErrors.entrySet().stream()
                .map(e -> "'" + e.getKey() + "' " + e.getValue())
                .reduce((a, b) -> a + " | " + b)
                .orElse("Validation failed");

        ErrorResponse body = new ErrorResponse();
        body.setStatus(HttpStatus.BAD_REQUEST.value());
        body.setError("Validation Failed");
        body.setMessage(message);
        body.setTimestamp(LocalDateTime.now());
        body.setPath(request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // ─── 400 — Missing Request Body ──────────────────────────────────────────────

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMissingBody(HttpMessageNotReadableException ex,
            WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Missing or Malformed Request Body",
                "Request body is missing or contains invalid JSON. Please send a valid JSON body.",
                request);
    }

    // ─── 400 — Missing @RequestParam ─────────────────────────────────────────────

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex,
            WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Missing Request Parameter",
                "Required parameter '" + ex.getParameterName() + "' is missing.", request);
    }

    // ─── 400 — Wrong Type for Path Variable or Request Param ─────────────────────

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
            WebRequest request) {
        String expected = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        String msg = String.format("Parameter '%s' should be of type '%s' but received: '%s'",
                ex.getName(), expected, ex.getValue());
        return build(HttpStatus.BAD_REQUEST, "Invalid Parameter Type", msg, request);
    }

    // ─── 400 — Missing Path Variable ─────────────────────────────────────────────

    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<ErrorResponse> handleMissingPath(MissingPathVariableException ex,
            WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Missing Path Variable",
                "Required path variable '" + ex.getVariableName() + "' is missing.", request);
    }

    // ─── 405 — Method Not Allowed ────────────────────────────────────────────────

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex,
            WebRequest request) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, "Method Not Allowed",
                "HTTP method '" + ex.getMethod() + "' is not supported for this endpoint.", request);
    }

    // ─── 404 — No Handler Found (wrong URL) ──────────────────────────────────────

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandler(NoHandlerFoundException ex,
            WebRequest request) {
        return build(HttpStatus.NOT_FOUND, "Endpoint Not Found",
                "No endpoint found for " + ex.getHttpMethod() + " " + ex.getRequestURL(), request);
    }

    // ─── 400 — IllegalArgumentException ──────────────────────────────────────────

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex,
            WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Invalid Argument", ex.getMessage(), request);
    }

    // ─── 500 — Catch-all for anything else ───────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception ex, WebRequest request) {
        // Log it for debugging
        System.err.println("[UNHANDLED EXCEPTION] " + ex.getClass().getName() + ": " + ex.getMessage());
        ex.printStackTrace();
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "Something went wrong on our end. Please try again later.", request);
    }
}
