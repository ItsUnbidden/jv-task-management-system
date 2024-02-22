package com.unbidden.jvtaskmanagementsystem.service;

import com.unbidden.jvtaskmanagementsystem.dto.project.CreateProjectRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.ProjectResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.UpdateProjectRoleRequestDto;
import com.unbidden.jvtaskmanagementsystem.model.User;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.RequestParam;

public interface ProjectService {
    public ProjectResponseDto findProjectById(User user, @NonNull Long id);
    
    public List<ProjectResponseDto> findAllProjectsForUser(User user);

    public List<ProjectResponseDto> searchProjectsByName(User user, 
            @RequestParam String name, @NonNull Pageable pageable);
    
    public ProjectResponseDto createProject(User user,
            @NonNull CreateProjectRequestDto requestDto);
    
    public ProjectResponseDto updateProject(User user, @NonNull Long id,
            @NonNull CreateProjectRequestDto requestDto);

    public void deleteProject(User user, @NonNull Long id);

    public ProjectResponseDto addUserToProject(User user, @NonNull Long projectId,
            @NonNull Long userId);

    public ProjectResponseDto changeProjectMemberRole(User user, @NonNull Long projectId,
            @NonNull Long userId,
            @NonNull UpdateProjectRoleRequestDto requestDto);

    public ProjectResponseDto removeUserFromProject(User user,
            @NonNull Long projectId, @NonNull Long userId);
}
