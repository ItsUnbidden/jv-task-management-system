package com.unbidden.jvtaskmanagementsystem.repository;

import com.unbidden.jvtaskmanagementsystem.model.Project;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    @NonNull
    @EntityGraph(attributePaths = {"projectRoles", "tasks", "projectCalendar"})
    Optional<Project> findById(@NonNull Long id);

    @NonNull
    @EntityGraph(attributePaths = {"projectRoles", "projectCalendar"})
    List<Project> findByNameContainsAllIgnoreCase(String name, Pageable pageable);

    @NonNull
    @Query("from Project p left join fetch p.projectRoles pr left join"
            + " fetch pr.user u left join fetch p.projectCalendar pc "
            + "where (p.isPrivate = false or u.id = ?1) and p.name like %?2%")
    List<Project> findPublicByNameContainsAllIgnoreCase(@NonNull Long userId,
            String name, Pageable pageable);
}
