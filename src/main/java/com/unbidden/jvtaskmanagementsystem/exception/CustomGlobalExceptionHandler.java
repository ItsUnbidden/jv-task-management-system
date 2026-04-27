package com.unbidden.jvtaskmanagementsystem.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.unbidden.jvtaskmanagementsystem.exception.oauth2.OAuth2AuthorizationException;
import com.unbidden.jvtaskmanagementsystem.exception.oauth2.OAuth2UnexpectedException;
import com.unbidden.jvtaskmanagementsystem.exception.thirdparty.ThirdPartyApiException;

@ControllerAdvice
public class CustomGlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger LOGGER = LogManager.getLogger(CustomGlobalExceptionHandler.class);

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            @NonNull MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers, 
            @NonNull HttpStatusCode status, 
            @NonNull WebRequest request
    ) {
        final Map<String, Object> body = new HashMap<>();

        body.put("timestamp", LocalDateTime.now());
        body.put("type", ErrorType.GENERAL_FIELD_VALIDATION);
        body.put("fieldErrors", ex.getBindingResult().getAllErrors());
        return new ResponseEntity<>(body, headers, status);
    }

    @ExceptionHandler({PropertyReferenceException.class})
    protected ResponseEntity<Object> handleInvalidUserInput(
            @NonNull Exception ex
    ) {
        LOGGER.error("Misformed request was sent.", ex);
        final Map<String, Object> body = new HashMap<>();

        body.put("timestamp", LocalDateTime.now());
        body.put("type", ErrorType.GENERAL_MISFORMED_REQUEST);
        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({StateCollisionException.class})
    protected ResponseEntity<Object> handleInvalidUserInput(
            @NonNull StateCollisionException ex
    ) {
        final Map<String, Object> body = new HashMap<>();

        body.put("timestamp", LocalDateTime.now());
        body.put("type", ex.getType());
        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    @ExceptionHandler({IllegalOperationException.class})
    protected ResponseEntity<Object> handleInvalidUserInput(
            @NonNull IllegalOperationException ex
    ) {
        final Map<String, Object> body = new HashMap<>();

        body.put("timestamp", LocalDateTime.now());
        body.put("type", ex.getType());
        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    protected ResponseEntity<Object> handleNotFoundException(
            @NonNull EntityNotFoundException ex
    ) {
        final Map<String, Object> body = new HashMap<>();

        body.put("timestamp", LocalDateTime.now());
        body.put("type", ex.getType());
        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({OAuth2AuthorizationException.class})
    protected ResponseEntity<Object> handleOAuth2Exception(
            @NonNull OAuth2UnexpectedException ex
    ) {
        LOGGER.error("A part of OAuth2 Service threw an exception.", ex);
        final Map<String, Object> body = new HashMap<>();

        body.put("timestamp", LocalDateTime.now());
        body.put("type", ex.getType());
        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.SERVICE_UNAVAILABLE);
    } 

    @ExceptionHandler({ThirdPartyApiException.class})
    protected ResponseEntity<Object> handleDropboxException(
            @NonNull ThirdPartyApiException ex
    ) {
        LOGGER.error("An external service has thrown an exception.", ex);
        final Map<String, Object> body = new HashMap<>();

        body.put("timestamp", LocalDateTime.now());
        body.put("type", ex.getType());
        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.SERVICE_UNAVAILABLE);
    } 

    @ExceptionHandler(FileSizeLimitExceededException.class)
    protected ResponseEntity<Object> handleFileSizeExceededException(
            @NonNull FileSizeLimitExceededException ex
    ) {
        final Map<String, Object> body = new HashMap<>();

        body.put("timestamp", LocalDateTime.now());
        body.put("type", ex.getType());
        body.put("errors", List.of(ex.getMessage()));

        return new ResponseEntity<>(body, HttpStatus.PAYLOAD_TOO_LARGE);
    } 

    @ExceptionHandler(AuthenticationException.class)
    protected ResponseEntity<Object> handleAuthenticationException(
            @NonNull AuthenticationException ex,
            @NonNull WebRequest request
    ) {
        LOGGER.error("An authentication exception occured.", ex);
        final Map<String, Object> body = new HashMap<>();

        body.put("timestamp", LocalDateTime.now());
        body.put("type", ErrorType.GENERAL_AUTHENTICATION_FAILURE);
        body.put("errors", List.of(ex.getMessage()));

        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    } 

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleGenericException(
            @NonNull Exception ex,
            @NonNull WebRequest request
    ) {
        final Map<String, Object> body = new HashMap<>();

        LOGGER.error("Some internal error occured.", ex);

        body.put("timestamp", LocalDateTime.now());      
        body.put("errors", List.of(ex.getMessage()));

        if (ex instanceof UnexpectedException unexpectedException && unexpectedException.getType() != null) {
            body.put("type", unexpectedException.getType());
        } else {
            body.put("type", ErrorType.INTERNAL);
        }

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    } 
}
