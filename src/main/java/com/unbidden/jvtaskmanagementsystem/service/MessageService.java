package com.unbidden.jvtaskmanagementsystem.service;

import com.unbidden.jvtaskmanagementsystem.dto.message.CommentResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.message.CommentWithTaskIdResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.message.CreateMessageRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.message.MessageResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.message.ReplyResponseDto;
import com.unbidden.jvtaskmanagementsystem.model.User;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;

public interface MessageService {

    List<CommentResponseDto> getCommentsForTask(User user, @NonNull Long taskId,
            Pageable pageable);

    List<CommentWithTaskIdResponseDto> getCommentsForProject(User user,
            @NonNull Long projectId, Pageable pageable);

    CommentWithTaskIdResponseDto getCommentById(User user, @NonNull Long commentId);

    List<ReplyResponseDto> getRepliesForComment(User user, @NonNull Long commentId,
            Pageable pageable);

    ReplyResponseDto getReplyById(User user, @NonNull Long replyId);

    CommentResponseDto leaveComment(User user, @NonNull Long taskId,
            @NonNull CreateMessageRequestDto requestDto);

    ReplyResponseDto replyToMessage(User user, @NonNull Long messageId,
            @NonNull CreateMessageRequestDto requestDto);

    MessageResponseDto updateMessage(User user, @NonNull Long messageId,
            @NonNull CreateMessageRequestDto requestDto);

    void deleteMessage(User user, @NonNull Long messageId);

}
