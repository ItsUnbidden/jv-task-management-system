package com.unbidden.jvtaskmanagementsystem.controller;

import com.unbidden.jvtaskmanagementsystem.dto.label.CreateLabelRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.label.LabelResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.label.UpdateLabelRequestDto;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.service.LabelService;
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
@RequestMapping("/labels")
@RequiredArgsConstructor
@SuppressWarnings("null")
@Tag(name = "Label related methods")
public class LabelController {
    private final LabelService labelService;

    @GetMapping("/projects/{projectId}")
    @Operation(
            summary = "Get labels in project by id",
            responses = {
                @ApiResponse(
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = LabelResponseDto.class)),
                    responseCode = "200",
                    description = "List of labels"),
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
    public List<LabelResponseDto> getLabelsForProject(Authentication authentication,
            @Parameter(
                description = "Project id"
            )
            @NonNull @PathVariable Long projectId,
            @Parameter(
                description = "Pagination and sorting"
            )
            Pageable pageable) {
        return labelService.getLablesForProject((User)authentication.getPrincipal(),
                projectId, pageable);
    }
    
    @GetMapping("/{labelId}")
    @Operation(
            summary = "Get label by id",
            responses = {
                @ApiResponse(
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = LabelResponseDto.class)),
                    responseCode = "200",
                    description = "The label"),
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
    public LabelResponseDto getLabelById(Authentication authentication,
            @Parameter(
                description = "Label id"
            )
            @NonNull @PathVariable Long labelId) {
        return labelService.getLabelById((User)authentication.getPrincipal(), labelId);
    }
    
    @PostMapping()
    @Operation(
            summary = "Create new label",
            responses = {
                @ApiResponse(
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = LabelResponseDto.class)),
                    responseCode = "200",
                    description = "New label"),
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
    public LabelResponseDto createLabel(Authentication authentication,
            @Parameter(
                description = "Create label request dto"
            )
            @NonNull @RequestBody @Valid CreateLabelRequestDto requestDto) {
        return labelService.createLabel((User)authentication.getPrincipal(),
                requestDto.getProjectId(), requestDto);
    }
    
    @PutMapping("/{labelId}")
    @Operation(
            summary = "Update label by id",
            responses = {
                @ApiResponse(
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = LabelResponseDto.class)),
                    responseCode = "200",
                    description = "Updated label"),
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
    public LabelResponseDto updateLabel(Authentication authentication,
            @Parameter(
                description = "Label id"
            )
            @NonNull @PathVariable Long labelId,
            @Parameter(
                description = "Update label request dto"
            )
            @NonNull @RequestBody @Valid UpdateLabelRequestDto requestDto) {
        return labelService.updateLabel((User)authentication.getPrincipal(),
                labelId, requestDto);
    }

    @DeleteMapping("/{labelId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete label by id",
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
    public void deleteLabel(Authentication authentication,
            @Parameter(
                description = "Label id"
            )
            @NonNull @PathVariable Long labelId) {
        labelService.deleteLabel((User)authentication.getPrincipal(), labelId);
    }
}
