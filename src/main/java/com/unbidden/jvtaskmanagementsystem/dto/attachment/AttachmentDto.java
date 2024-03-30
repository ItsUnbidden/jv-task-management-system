package com.unbidden.jvtaskmanagementsystem.dto.attachment;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AttachmentDto {
    private Long id;

    private Long taskId;

    private String filename;

    private LocalDateTime uploadDate;
}
