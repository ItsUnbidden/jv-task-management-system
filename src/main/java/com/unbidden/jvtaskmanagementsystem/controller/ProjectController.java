package com.unbidden.jvtaskmanagementsystem.controller;

import com.unbidden.jvtaskmanagementsystem.dto.project.CreateProjectRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.ProjectResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.UpdateProjectRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.UpdateProjectRoleRequestDto;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.service.ProjectService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    @GetMapping("/{id}")
    public ProjectResponseDto getProjectById(Authentication authentication,
            @NonNull @PathVariable Long id) {
        return projectService.findProjectById((User)authentication.getPrincipal(), id);
    }
    
    @GetMapping("/me")
    public List<ProjectResponseDto> getAllProjectsForUser(Authentication authentication) {
        return projectService.findAllProjectsForUser((User)authentication.getPrincipal());
    }

    @GetMapping("/search")
    public List<ProjectResponseDto> searchProjectsByName(Authentication authentication, 
            @RequestParam String name, @NonNull Pageable pageable) {
        return projectService.searchProjectsByName((User)authentication.getPrincipal(),
                name, pageable);
    }
    
    @PostMapping
    public ProjectResponseDto createProject(Authentication authentication,
            @NonNull @RequestBody @Valid CreateProjectRequestDto requestDto) {
        return projectService.createProject((User)authentication.getPrincipal(), requestDto);
    }
    
    @PutMapping("/{id}")
    public ProjectResponseDto updateProject(Authentication authentication, 
            @NonNull @PathVariable Long id,
            @NonNull @RequestBody @Valid UpdateProjectRequestDto requestDto) {
        return projectService.updateProject((User)authentication.getPrincipal(), id, requestDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProject(Authentication authentication, @NonNull @PathVariable Long id) {
        projectService.deleteProject((User)authentication.getPrincipal(), id);
    }

    @PostMapping("/{projectId}/users/{userId}/add")
    public ProjectResponseDto addUserToProject(Authentication authentication,
            @NonNull @PathVariable Long projectId,
            @NonNull @PathVariable Long userId) {
        return projectService.addUserToProject((User)authentication.getPrincipal(),
                projectId, userId);
    }

    @DeleteMapping("/{projectId}/users/{userId}/remove")
    public ProjectResponseDto removeUserFromProject(Authentication authentication,
            @NonNull @PathVariable Long projectId,
            @NonNull @PathVariable Long userId) {
        return projectService.removeUserFromProject((User)authentication.getPrincipal(),
                projectId, userId);
    }

    @PatchMapping("/{projectId}/users/{userId}/roles")
    public ProjectResponseDto changeProjectMemberRole(Authentication authentication,
            @NonNull @PathVariable Long projectId,
            @NonNull @PathVariable Long userId,
            @NonNull @RequestBody @Valid UpdateProjectRoleRequestDto requestDto) {
        return projectService.changeProjectMemberRole((User)authentication.getPrincipal(),
                projectId, userId, requestDto);
    }
}
