package com.unbidden.jvtaskmanagementsystem.dto.message;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CommentWithTaskIdResponseDto extends CommentResponseDto {
    private Long taskId;
}
