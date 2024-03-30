package com.unbidden.jvtaskmanagementsystem.dto.message;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReplyResponseDto extends MessageResponseDto {
    private List<ReplyResponseDto> replyDtos;
}
