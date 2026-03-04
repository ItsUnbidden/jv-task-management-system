package com.unbidden.jvtaskmanagementsystem.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;

import com.unbidden.jvtaskmanagementsystem.dto.message.CommentResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.message.CommentWithTaskIdResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.message.CreateMessageRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.message.MessageResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.message.ReplyResponseDto;
import com.unbidden.jvtaskmanagementsystem.model.User;

public interface MessageService {

    Page<CommentResponseDto> getCommentsForTask(@NonNull User user, @NonNull Long taskId,
            Pageable pageable);

    Page<CommentWithTaskIdResponseDto> getCommentsForProject(@NonNull User user,
            @NonNull Long projectId, Pageable pageable);

    CommentWithTaskIdResponseDto getCommentById(@NonNull User user, @NonNull Long commentId);

    Page<ReplyResponseDto> getRepliesForComment(@NonNull User user, @NonNull Long commentId,
            Pageable pageable);

    ReplyResponseDto getReplyById(@NonNull User user, @NonNull Long replyId);

    CommentResponseDto leaveComment(@NonNull User user, @NonNull Long taskId,
            @NonNull CreateMessageRequestDto requestDto);

    ReplyResponseDto replyToMessage(@NonNull User user, @NonNull Long messageId,
            @NonNull CreateMessageRequestDto requestDto);

    MessageResponseDto updateMessage(@NonNull User user, @NonNull Long messageId,
            @NonNull CreateMessageRequestDto requestDto);

    void deleteMessage(@NonNull User user, @NonNull Long messageId);

}
