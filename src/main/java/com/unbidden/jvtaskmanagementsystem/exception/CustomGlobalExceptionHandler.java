package com.unbidden.jvtaskmanagementsystem.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
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
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.unbidden.jvtaskmanagementsystem.exception.dropbox.DropboxException;
import com.unbidden.jvtaskmanagementsystem.exception.dropbox.GeneralDropboxException;
import com.unbidden.jvtaskmanagementsystem.exception.dropbox.SpecificDropboxException;
import com.unbidden.jvtaskmanagementsystem.exception.oauth2.OAuth2AuthorizationException;
import com.unbidden.jvtaskmanagementsystem.exception.oauth2.OAuth2CodeExchangeException;
import com.unbidden.jvtaskmanagementsystem.exception.oauth2.OAuth2PropertiesParsingException;

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
        final Map<String, Object> body = new LinkedHashMap<>();

        body.put("timestamp", LocalDateTime.now());
        body.put("errors", ex.getBindingResult().getAllErrors().stream()
                .map(this::getErrorMessage)
                .toList());
        return new ResponseEntity<>(body, headers, status);
    }

    @ExceptionHandler({RegistrationException.class, 
            PropertyReferenceException.class,
            UnsupportedOperationException.class,
            IllegalArgumentException.class})
    protected ResponseEntity<Object> handleInvalidUserInput(
            @NonNull Exception ex
    ) {
        LOGGER.error("Some invalid user action was detected.", ex);
        return handleBadRequest(ex);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    protected ResponseEntity<Object> handleNotFoundException(
            @NonNull EntityNotFoundException ex
    ) {
        final Map<String, Object> body = new LinkedHashMap<>();

        LOGGER.error("Failed to find an entity.", ex);
        body.put("timestamp", LocalDateTime.now());
        body.put("errors", List.of(ex.getMessage()));

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({OAuth2AuthorizationException.class,
            OAuth2CodeExchangeException.class,
            OAuth2PropertiesParsingException.class})
    protected ResponseEntity<Object> handleOAuth2Exception(
            @NonNull Exception ex
    ) {
        LOGGER.error("A part of OAuth2 Service threw an exception.", ex);
        return handleBadRequest(ex);
    } 

    @ExceptionHandler({SpecificDropboxException.class,
            GeneralDropboxException.class})
    protected ResponseEntity<Object> handleDropboxException(
            @NonNull DropboxException ex
    ) {
        if (ex instanceof SpecificDropboxException) {
            LOGGER.error("A method specific exception was thrown by dropbox service.", ex);
        } else {
            LOGGER.error("General dropbox exception was thrown by dropbox service. "
                    + "This requires investigation.", ex);
        }
        return handleBadRequest(ex);
    } 

    @ExceptionHandler(FileSizeLimitExceededException.class)
    protected ResponseEntity<Object> handleFileSizeExceededException(
            @NonNull FileSizeLimitExceededException ex
    ) {
        final Map<String, Object> body = new LinkedHashMap<>();

        body.put("timestamp", LocalDateTime.now());
        body.put("errors", List.of(ex.getMessage()));

        return new ResponseEntity<>(body, HttpStatus.PAYLOAD_TOO_LARGE);
    } 

    @ExceptionHandler(AuthenticationException.class)
    protected ResponseEntity<Object> handleAuthenticationException(
            @NonNull Exception ex,
            @NonNull WebRequest request
    ) {
        final Map<String, Object> body = new LinkedHashMap<>();

        body.put("timestamp", LocalDateTime.now());
        body.put("errors", List.of(ex.getMessage()));

        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    } 

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleGenericException(
            @NonNull Exception ex,
            @NonNull WebRequest request
    ) {
        final Map<String, Object> body = new LinkedHashMap<>();

        body.put("timestamp", LocalDateTime.now());
        body.put("errors", List.of(ex.getMessage()));

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    } 

    private ResponseEntity<Object> handleBadRequest(@NonNull Exception ex) {
        final Map<String, Object> body = new LinkedHashMap<>();

        body.put("timestamp", LocalDateTime.now());
        body.put("errors", List.of(ex.getMessage()));

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    private String getErrorMessage(ObjectError e) {
        if (e instanceof FieldError fieldError) {
            String field = fieldError.getField();
            String message = e.getDefaultMessage();
            return field + " " + message;
        }
        return (e != null) ? e.getDefaultMessage() : "Exception is null.";
    }
}
