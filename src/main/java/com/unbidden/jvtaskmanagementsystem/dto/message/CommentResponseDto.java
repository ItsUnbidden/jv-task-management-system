package com.unbidden.jvtaskmanagementsystem.dto.message;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CommentResponseDto extends MessageResponseDto {
    private Integer amountOfReplies;
}
