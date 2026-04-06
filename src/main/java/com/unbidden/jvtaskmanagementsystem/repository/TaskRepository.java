package com.unbidden.jvtaskmanagementsystem.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;

import com.unbidden.jvtaskmanagementsystem.model.Task;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
    @NonNull
    @Override
    @EntityGraph(attributePaths = {"project", "assignee", "labels"})
    Optional<Task> findById(@NonNull Long id);

    @NonNull
    @Query("from Task t left join fetch t.assignee a where a.id = ?1 and t.name like %?2%")
    Page<Task> findByAssigneeIdAndSearchByTaskName(@NonNull Long assigneeId, @NonNull String name, Pageable pageable);

    @NonNull
    @EntityGraph(attributePaths = {"project", "assignee", "labels"})
    Page<Task> findByProjectId(@NonNull Long projectId, Pageable pageable);

    @NonNull
    @Query("from Task t left join fetch t.project p left join fetch t.assignee a "
            + "left join fetch t.labels l where l.id = :labelId")
    Page<Task> findByLabelId(@NonNull Long labelId, Pageable pageable);

    @NonNull
    @Query("from Task t left join fetch t.project p left join fetch t.assignee a "
            + "left join fetch t.labels l where a.id = :assigneeId and p.id = :projectId")
    Page<Task> findByAssigneeIdAndByProjectId(@NonNull Long assigneeId, 
            @NonNull Long projectId, Pageable pageable);
}
