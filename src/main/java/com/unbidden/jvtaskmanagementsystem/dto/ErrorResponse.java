package com.unbidden.jvtaskmanagementsystem.dto;

import java.time.LocalDateTime;

import com.unbidden.jvtaskmanagementsystem.exception.ErrorType;

public record ErrorResponse(LocalDateTime timestamp, ErrorType type, String error) {}
