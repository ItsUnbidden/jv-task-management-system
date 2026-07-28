package com.unbidden.jvtaskmanagementsystem.dto.message;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReplyWithCommentIdResponseDto extends ReplyResponseDto {
    private final Long commentId;

    public ReplyWithCommentIdResponseDto(ReplyResponseDto dto, Long commentId) {
        setId(dto.getId());
        setUserId(dto.getUserId());
        setUsername(dto.getUsername());
        setTimestamp(dto.getTimestamp());
        setText(dto.getText());
        setLastUpdated(dto.getLastUpdated());
        setReplyDtos(dto.getReplyDtos());
        this.commentId = commentId;
    }
}
