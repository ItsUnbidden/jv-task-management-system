package com.unbidden.jvtaskmanagementsystem.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.unbidden.jvtaskmanagementsystem.config.MapperConfig;
import com.unbidden.jvtaskmanagementsystem.dto.internal.PartialReplyDto;
import com.unbidden.jvtaskmanagementsystem.dto.message.CommentResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.message.CommentWithTaskIdResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.message.ReplyResponseDto;
import com.unbidden.jvtaskmanagementsystem.model.Comment;
import com.unbidden.jvtaskmanagementsystem.model.Reply;
import com.unbidden.jvtaskmanagementsystem.model.User;

@Mapper(config = MapperConfig.class)
public interface MessageMapper {
    @Mapping(target = "userId", source = "comment.user.id")
    @Mapping(target = "username", source = "comment.user.username")
    @Mapping(target = "numberOfReplies", ignore = true)
    CommentResponseDto toCommentDto(Comment comment);

    @Mapping(target = "userId", source = "comment.user.id")
    @Mapping(target = "username", source = "comment.user.username")
    @Mapping(target = "taskId", source = "comment.task.id")
    @Mapping(target = "numberOfReplies", ignore = true)
    CommentWithTaskIdResponseDto toCommentWithTaskIdDto(Comment comment);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "replyDtos", ignore = true)
    ReplyResponseDto toReplyDto(Reply reply);

    @Mapping(target = "replies", ignore = true)
    @Mapping(target = "parent", source = "parentId")
    @Mapping(target = "comment", source = "commentId")
    @Mapping(target = "user", source = "partialReplyDto")
    Reply toReplyFromPartial(PartialReplyDto partialReplyDto);

    default Comment createComment(Long messageId) {
        final Comment dummyComment = new Comment();

        dummyComment.setId(messageId);
        return dummyComment;
    }

    default User createUser(PartialReplyDto dto) {
        final User dummyUser = new User();

        dummyUser.setId(dto.getUserId());
        dummyUser.setUsername(dto.getUsername());
        return dummyUser;
    }
}
