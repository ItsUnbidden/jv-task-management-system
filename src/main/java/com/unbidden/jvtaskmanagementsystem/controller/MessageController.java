package com.unbidden.jvtaskmanagementsystem.controller;

import com.unbidden.jvtaskmanagementsystem.dto.message.CommentResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.message.CommentWithTaskIdResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.message.CreateMessageRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.message.MessageResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.message.ReplyResponseDto;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Comments and replies related methods")
public class MessageController {
    private final MessageService messageService;

    @GetMapping("/comments/tasks/{taskId}")
    @Operation(
            summary = "Get comments for task by id",
            responses = {
                @ApiResponse(
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = CommentResponseDto.class)),
                    responseCode = "200",
                    description = "List of comments"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "400",
                    description = "Invalid id"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "401",
                    description = "Unauthorized"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "403",
                    description = "Forbidden. Possible only if project is private")
            }
    )
    public List<CommentResponseDto> getCommentsForTask(Authentication authentication,
            @Parameter(
                description = "Task id"
            )
            @NonNull @PathVariable Long taskId,
            @Parameter(
                description = "Pagination and sorting"
            )
            Pageable pageable) {
        return messageService.getCommentsForTask((User)authentication.getPrincipal(),
                taskId, pageable);
    }
    
    @GetMapping("/comments/projects/{projectId}")
    @Operation(
            summary = "Get comments for project by id",
            responses = {
                @ApiResponse(
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = CommentWithTaskIdResponseDto.class)),
                    responseCode = "200",
                    description = "List of comments"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "400",
                    description = "Invalid id"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "401",
                    description = "Unauthorized"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "403",
                    description = "Forbidden. Possible only if project is private")
            }
    )
    public List<CommentWithTaskIdResponseDto> getCommentsForProject(Authentication authentication,
            @Parameter(
                description = "Project id"
            )
            @NonNull @PathVariable Long projectId,
            @Parameter(
                description = "Pagination and sorting"
            )
            Pageable pageable) {
        return messageService.getCommentsForProject((User)authentication.getPrincipal(),
                projectId, pageable);
    }
    
    @GetMapping("/comments/{commentId}")
    @Operation(
            summary = "Get comment by id",
            responses = {
                @ApiResponse(
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = CommentWithTaskIdResponseDto.class)),
                    responseCode = "200",
                    description = "The comment"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "400",
                    description = "Invalid id"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "401",
                    description = "Unauthorized"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "403",
                    description = "Forbidden. Possible only if project is private")
            }
    )
    public CommentWithTaskIdResponseDto getCommentById(Authentication authentication,
            @Parameter(
                description = "Comment id"
            )
            @NonNull @PathVariable Long commentId) {
        return messageService.getCommentById((User)authentication.getPrincipal(), commentId);
    }

    @GetMapping("/replies/{replyId}")
    @Operation(
            summary = "Get reply by id",
            responses = {
                @ApiResponse(
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ReplyResponseDto.class)),
                    responseCode = "200",
                    description = "The reply"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "400",
                    description = "Invalid id"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "401",
                    description = "Unauthorized"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "403",
                    description = "Forbidden. Possible only if project is private")
            }
    )
    public ReplyResponseDto getReplyById(Authentication authentication,
            @Parameter(
                description = "Reply id"
            )
            @NonNull @PathVariable Long replyId) {
        return messageService.getReplyById((User)authentication.getPrincipal(), replyId);
    }
    
    @GetMapping("/comments/{commentId}/replies")
    @Operation(
            summary = "Get replies for comment by id",
            responses = {
                @ApiResponse(
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ReplyResponseDto.class)),
                    responseCode = "200",
                    description = "List of replies"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "400",
                    description = "Invalid id"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "401",
                    description = "Unauthorized"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "403",
                    description = "Forbidden. Possible only if project is private")
            }
    )
    public List<ReplyResponseDto> getRepliesForComment(Authentication authentication,
            @Parameter(
                description = "Comment id"
            )
            @NonNull @PathVariable Long commentId,
            @Parameter(
                description = "Pagination and sorting"
            )
            Pageable pageable) {
        return messageService.getRepliesForComment((User)authentication.getPrincipal(),
                commentId, pageable);
    }
    
    @PostMapping("/comments/tasks/{taskId}")
    @Operation(
            summary = "Leave a comment under a task by id",
            description = "If project is public, everyone can leave comments "
                + "including non-participants",
            responses = {
                @ApiResponse(
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = CommentResponseDto.class)),
                    responseCode = "200",
                    description = "New comment"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "400",
                    description = "Invalid input"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "401",
                    description = "Unauthorized"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "403",
                    description = "Forbidden. Possible only if project is private")
            }
    )
    public CommentResponseDto leaveComment(Authentication authentication,
            @Parameter(
                description = "Task id"
            )
            @NonNull @PathVariable Long taskId, 
            @Parameter(
                description = "Create message request dto"
            )
            @NonNull @RequestBody @Valid CreateMessageRequestDto requestDto) {
        return messageService.leaveComment((User)authentication.getPrincipal(),
                taskId, requestDto);
    }
    
    @PostMapping("/{messageId}/replies")
    @Operation(
            summary = "Reply to a comment or another reply by id",
            description = "If project is public, everyone can leave comments "
                + "including non-participants",
            responses = {
                @ApiResponse(
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ReplyResponseDto.class)),
                    responseCode = "200",
                    description = "New reply"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "400",
                    description = "Invalid input"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "401",
                    description = "Unauthorized"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "403",
                    description = "Forbidden. Possible only if project is private")
            }
    )
    public ReplyResponseDto replyToMessage(Authentication authentication,
            @Parameter(
                description = "Comment or reply id"
            )
            @NonNull @PathVariable Long messageId,
            @Parameter(
                description = "Create message request dto"
            )
            @NonNull @RequestBody @Valid CreateMessageRequestDto requestDto) {
        return messageService.replyToMessage((User)authentication.getPrincipal(),
                messageId, requestDto);
    }
    
    @PutMapping("/{messageId}")
    @Operation(
            summary = "Update a comment or a reply",
            responses = {
                @ApiResponse(
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = MessageResponseDto.class)),
                    responseCode = "200",
                    description = "Updated message"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "400",
                    description = "Invalid input"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "401",
                    description = "Unauthorized"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "403",
                    description = "Forbidden")
            }
    )
    public MessageResponseDto updateMessage(Authentication authentication,
            @Parameter(
                description = "Comment or reply id"
            )
            @NonNull @PathVariable Long messageId,
            @Parameter(
                description = "Create message request dto"
            )
            @NonNull @RequestBody @Valid CreateMessageRequestDto requestDto) {
        return messageService.updateMessage((User)authentication.getPrincipal(),
                messageId, requestDto);
    }

    @DeleteMapping("/{messageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete a comment or a reply",
            responses = {
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "204"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "400",
                    description = "Invalid id"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "401",
                    description = "Unauthorized"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "403",
                    description = "Forbidden")
            }
    )
    public void deleteMessage(Authentication authentication,
            @Parameter(
                description = "Comment or reply id"
            )
            @NonNull @PathVariable Long messageId) {
        messageService.deleteMessage((User)authentication.getPrincipal(), messageId);
    }
}
