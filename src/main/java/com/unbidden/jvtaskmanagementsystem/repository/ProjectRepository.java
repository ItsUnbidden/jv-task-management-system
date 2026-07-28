package com.unbidden.jvtaskmanagementsystem.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;

import com.unbidden.jvtaskmanagementsystem.model.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    @NonNull
    @Override
    @EntityGraph(attributePaths = {"projectRoles", "projectRoles.user"})
    Optional<Project> findById(@NonNull Long id);

    @NonNull
    @Query("from Project p where exists(select 1 from p.projectRoles pr where pr.user.id = ?1) and p.name like %?2%")
    Page<Project> findProjectsForUserAndSearchByName(@NonNull Long userId, @NonNull String name, Pageable pageable);

    @NonNull
    Page<Project> findByNameContainsAllIgnoreCase(String name, Pageable pageable);

    @NonNull
    @Query("from Project p left join p.projectRoles pr left join pr.user u "
            + "where (p.isPrivate = false or u.id = ?1) and p.name like %?2%")
    Page<Project> findPublicByNameContainsAllIgnoreCase(Long userId,
            String name, Pageable pageable);

    @NonNull
    @Query("select p.progress from Project p where p.id = :id")
    Optional<Integer> findProgressById(@NonNull Long id);
}
