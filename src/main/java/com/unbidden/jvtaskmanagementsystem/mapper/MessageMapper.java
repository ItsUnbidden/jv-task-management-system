package com.unbidden.jvtaskmanagementsystem.mapper;

import com.unbidden.jvtaskmanagementsystem.config.MapperConfig;
import com.unbidden.jvtaskmanagementsystem.dto.message.CommentResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.message.CommentWithTaskIdResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.message.MessageResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.message.ReplyResponseDto;
import com.unbidden.jvtaskmanagementsystem.model.Comment;
import com.unbidden.jvtaskmanagementsystem.model.Message;
import com.unbidden.jvtaskmanagementsystem.model.Reply;
import java.util.ArrayList;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface MessageMapper {
    CommentResponseDto toCommentDto(Comment comment);

    CommentWithTaskIdResponseDto toCommentWithTaskIdDto(Comment comment);

    ReplyResponseDto toReplyDto(Reply reply);

    @AfterMapping
    default void setTaskId(@MappingTarget CommentWithTaskIdResponseDto dto, Comment comment) {
        dto.setTaskId(comment.getTask().getId());
    }

    @AfterMapping
    default void setUsernameAndUserId(@MappingTarget MessageResponseDto dto, Message comment) {
        dto.setUserId(comment.getUser().getId());
        dto.setUsername(comment.getUser().getUsername());
    }

    @AfterMapping
    default void setReplyDtos(@MappingTarget ReplyResponseDto dto, Reply reply) {
        dto.setReplyDtos(new ArrayList<>());

        for (Reply innerReply : reply.getReplies()) {
            dto.getReplyDtos().add(toReplyDto(innerReply));
        }
    }
}
