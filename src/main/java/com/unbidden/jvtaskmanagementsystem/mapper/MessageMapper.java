package com.unbidden.jvtaskmanagementsystem.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.unbidden.jvtaskmanagementsystem.config.MapperConfig;
import com.unbidden.jvtaskmanagementsystem.dto.message.CommentResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.message.CommentWithTaskIdResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.message.ReplyResponseDto;
import com.unbidden.jvtaskmanagementsystem.model.Comment;
import com.unbidden.jvtaskmanagementsystem.model.Reply;

@Mapper(config = MapperConfig.class)
public interface MessageMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    CommentResponseDto toCommentDto(Comment comment);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "taskId", source = "task.id")
    CommentWithTaskIdResponseDto toCommentWithTaskIdDto(Comment comment);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "replyDtos", source = "replies")
    ReplyResponseDto toReplyDto(Reply reply);
}
