package com.unbidden.jvtaskmanagementsystem.dto.user;

import java.util.List;

import org.springframework.lang.NonNull;

import com.unbidden.jvtaskmanagementsystem.dto.project.DeleteProjectResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.ProjectWithDropboxResultResponseDto;

import lombok.Data;

@Data
public class DeleteUserResponseDto {
    @NonNull
    private List<DeleteProjectResponseDto> deletedProjects;

    @NonNull
    private List<ProjectWithDropboxResultResponseDto> quittedProjects;
}
