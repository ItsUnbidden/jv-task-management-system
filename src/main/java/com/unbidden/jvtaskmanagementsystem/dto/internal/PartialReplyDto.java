package com.unbidden.jvtaskmanagementsystem.dto.internal;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import lombok.Getter;

@Getter
public class PartialReplyDto {
    private Long id;

    private Long userId;

    private String username;

    private Long parentId;
    
    private Long commentId;

    private String text;

    private LocalDateTime timestamp;
    
    private LocalDateTime lastUpdated;

    private Boolean isDeleted;

    private Long version;

    public PartialReplyDto(Long id, Long userId, String username, Long parentId, Long commentId, String text,
            Timestamp timestamp, Timestamp lastUpdated, Byte isDeleted, Long version) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.parentId = parentId;
        this.commentId = commentId;
        this.text = text;
        this.timestamp = timestamp.toLocalDateTime();
        this.lastUpdated = (lastUpdated == null) ? null : lastUpdated.toLocalDateTime();
        this.isDeleted = isDeleted != 0;
        this.version = version;
    }
}
