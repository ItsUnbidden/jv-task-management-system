package com.unbidden.jvtaskmanagementsystem.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;

import com.unbidden.jvtaskmanagementsystem.model.Label;

public interface LabelRepository extends JpaRepository<Label, Long> {
    @NonNull
    @Override
    @EntityGraph(attributePaths = {"project", "tasks"})
    Optional<Label> findById(@NonNull Long id);

    @NonNull
    @EntityGraph(attributePaths = {"project", "tasks"})
    List<Label> findByProjectId(@NonNull Long projectId);

    @NonNull
    @Query("from Label l left join fetch l.project p left join fetch l.tasks t where exists (select 1 from l.tasks t2 where t2.id = :taskId)")
    List<Label> findByTaskId(@NonNull Long taskId);
}
