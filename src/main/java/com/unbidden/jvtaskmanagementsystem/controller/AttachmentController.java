package com.unbidden.jvtaskmanagementsystem.controller;

import com.unbidden.jvtaskmanagementsystem.dto.attachment.AttachmentDto;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.service.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/attachments")
@RequiredArgsConstructor
@Tag(name = "Attachment related methods",
        description = "All methods in this controller require dropbox connection")
public class AttachmentController {
    private final AttachmentService attachmentService;

    @GetMapping("/tasks/{taskId}")
    @Operation(
            summary = "Get attachments for task by id",
            description = "Project must be connected to dropbox",
            responses = {
                @ApiResponse(
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = AttachmentDto.class)),
                    responseCode = "200",
                    description = "List of attachments"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "400",
                    description = "Invalid input or some other issue related to dropbox"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "401",
                    description = "Unauthorized")
            }
    )
    public List<AttachmentDto> getAvailableAttachmentsForTask(Authentication authentication,
            @Parameter(
                description = "Task id"
            )
            @NonNull @PathVariable Long taskId,
            @Parameter(
                description = "Pagination and sorting"
            )
            Pageable pageable) {
        return attachmentService.getAttachmentsForTask((User)authentication.getPrincipal(),
                taskId, pageable);
    }
    
    @PostMapping("/tasks/{taskId}")
    @Operation(
            summary = "Upload file",
            description = "Project must be connected to dropbox. File must not be over 50mb, "
                    + "otherwise dropbox will reject it",
            responses = {
                @ApiResponse(
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = AttachmentDto.class)),
                    responseCode = "200",
                    description = "New attachment"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "400",
                    description = "Invalid input or some other issue related to dropbox"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "401",
                    description = "Unauthorized")
            }
    )
    public AttachmentDto upload(Authentication authentication,
            @Parameter(
                description = "Task id"
            )
            @NonNull @PathVariable Long taskId,
            @Parameter(
                description = "File that will be uploaded"
            )
            @NonNull @RequestParam("file") MultipartFile file) throws IOException {
        return attachmentService.upload((User)authentication.getPrincipal(), taskId, file);
    }
    
    @GetMapping("/{attachmentId}")
    @Operation(
            summary = "Download file in the attachment",
            description = "Project must be connected to dropbox",
            responses = {
                @ApiResponse(
                    content = @Content(
                        mediaType = "application/octet-stream"),
                    responseCode = "200",
                    description = "File will be included with the response"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "400",
                    description = "Invalid input or some other issue related to dropbox"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "401",
                    description = "Unauthorized")
            }
    )
    public void download(Authentication authentication, HttpServletResponse response,
            @Parameter(
                description = "Attachment id"
            )
            @NonNull @PathVariable Long attachmentId) {
        attachmentService.download((User)authentication.getPrincipal(), response, attachmentId);
    }
}
