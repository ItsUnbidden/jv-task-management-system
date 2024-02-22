package com.unbidden.jvtaskmanagementsystem.repository;

import com.unbidden.jvtaskmanagementsystem.model.ProjectRole;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;

public interface ProjectRoleRepository extends JpaRepository<ProjectRole, Long> {
    @NonNull
    @EntityGraph(attributePaths = "user")
    @Query("from ProjectRole pr left join fetch pr.project p "
            + "left join fetch pr.user u where p.id = :projectId and u.id = :userId")
    Optional<ProjectRole> findByProjectIdWithUserId(@NonNull Long projectId, @NonNull Long userId);

    @NonNull
    @EntityGraph(attributePaths = "project")
    List<ProjectRole> findByUserId(@NonNull Long userId);

    @NonNull
    Set<ProjectRole> findByProjectId(@NonNull Long projectId);
}
