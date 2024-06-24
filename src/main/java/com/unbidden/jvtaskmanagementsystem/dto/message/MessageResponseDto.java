package com.unbidden.jvtaskmanagementsystem.dto.message;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public abstract class MessageResponseDto {
    private Long id;

    private Long userId;

    private String username;

    private String text;

    private LocalDateTime timestamp;

    private LocalDateTime lastUpdated;
}
