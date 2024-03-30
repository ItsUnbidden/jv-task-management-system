package com.unbidden.jvtaskmanagementsystem.controller;

import com.unbidden.jvtaskmanagementsystem.dto.message.CommentResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.message.CommentWithTaskIdResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.message.CreateMessageRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.message.MessageResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.message.ReplyResponseDto;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.service.MessageService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;

    @GetMapping("/comments/tasks/{taskId}")
    public List<CommentResponseDto> getCommentsForTask(Authentication authentication,
            @NonNull @PathVariable Long taskId, Pageable pageable) {
        return messageService.getCommentsForTask((User)authentication.getPrincipal(),
                taskId, pageable);
    }
    
    @GetMapping("/comments/projects/{projectId}")
    public List<CommentWithTaskIdResponseDto> getCommentsForProject(Authentication authentication,
            @NonNull @PathVariable Long projectId, Pageable pageable) {
        return messageService.getCommentsForProject((User)authentication.getPrincipal(),
                projectId, pageable);
    }
    
    @GetMapping("/comments/{commentId}")
    public CommentWithTaskIdResponseDto getCommentById(Authentication authentication,
            @NonNull @PathVariable Long commentId) {
        return messageService.getCommentById((User)authentication.getPrincipal(), commentId);
    }

    @GetMapping("/replies/{replyId}")
    public ReplyResponseDto getReplyById(Authentication authentication,
            @NonNull @PathVariable Long replyId) {
        return messageService.getReplyById((User)authentication.getPrincipal(), replyId);
    }
    
    @GetMapping("/comments/{commentId}/replies")
    public List<ReplyResponseDto> getRepliesForComment(Authentication authentication,
            @NonNull @PathVariable Long commentId, Pageable pageable) {
        return messageService.getRepliesForComment((User)authentication.getPrincipal(),
                commentId, pageable);
    }
    
    @PostMapping("/comments/tasks/{taskId}")
    public CommentResponseDto leaveComment(Authentication authentication,
            @NonNull @PathVariable Long taskId, 
            @NonNull @RequestBody @Valid CreateMessageRequestDto requestDto) {
        return messageService.leaveComment((User)authentication.getPrincipal(),
                taskId, requestDto);
    }
    
    @PostMapping("/{messageId}/replies")
    public ReplyResponseDto replyToMessage(Authentication authentication,
            @NonNull @PathVariable Long messageId,
            @NonNull @RequestBody @Valid CreateMessageRequestDto requestDto) {
        return messageService.replyToMessage((User)authentication.getPrincipal(),
                messageId, requestDto);
    }
    
    @PutMapping("/{messageId}")
    public MessageResponseDto updateMessage(Authentication authentication,
            @NonNull @PathVariable Long messageId,
            @NonNull @RequestBody @Valid CreateMessageRequestDto requestDto) {
        return messageService.updateMessage((User)authentication.getPrincipal(),
                messageId, requestDto);
    }

    @DeleteMapping("/{messageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMessage(Authentication authentication,
            @NonNull @PathVariable Long messageId) {
        messageService.deleteMessage((User)authentication.getPrincipal(), messageId);
    }
}
