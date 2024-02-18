package com.unbidden.jvtaskmanagementsystem.exception;

import io.jsonwebtoken.JwtException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class CustomGlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            @NonNull MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers, 
            @NonNull HttpStatusCode status, 
            @NonNull WebRequest request
    ) {
        Map<String, Object> body = new LinkedHashMap<>();

        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST);
        body.put("errors", ex.getBindingResult().getAllErrors().stream()
                .map(this::getErrorMessage)
                .toList());
        return new ResponseEntity<Object>(body, headers, status);
    }

    @ExceptionHandler({EntityNotFoundException.class, 
            RegistrationException.class, 
            PropertyReferenceException.class})
    protected ResponseEntity<Object> handleInvalidUserInput(
            @NonNull Exception ex,
            @NonNull WebRequest request
    ) {
        Map<String, Object> body = new LinkedHashMap<>();

        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST);
        body.put("error", ex.getMessage());

        return handleExceptionInternal(ex, body, new HttpHeaders(), 
                HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(JwtException.class)
    protected ResponseEntity<Object> handleInvalidJwtToken(
            @NonNull JwtException ex,
            @NonNull WebRequest request
    ) {
        Map<String, Object> body = new LinkedHashMap<>();

        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.UNAUTHORIZED);
        body.put("error", ex.getMessage());

        return handleExceptionInternal(ex, body, new HttpHeaders(), 
                HttpStatus.UNAUTHORIZED, request);
    }

    private String getErrorMessage(ObjectError e) {
        if (e instanceof FieldError) {
            String field = ((FieldError)e).getField();
            String message = e.getDefaultMessage();
            return field + " " + message;
        }
        return e.getDefaultMessage();
    }
}
