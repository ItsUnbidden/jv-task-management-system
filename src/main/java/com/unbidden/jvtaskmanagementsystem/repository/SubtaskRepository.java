package com.unbidden.jvtaskmanagementsystem.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import com.unbidden.jvtaskmanagementsystem.model.Subtask;

public interface SubtaskRepository extends JpaRepository<Subtask, Long> {
    @NonNull
    @Override
    @EntityGraph(attributePaths = {"task", "task.project"})
    Optional<Subtask> findById(Long id);

    @NonNull
    Page<Subtask> findByTaskId(@NonNull Long taskId, @NonNull Pageable pageable);

    int countByTaskId(@NonNull Long taskId);

    int countByTaskIdAndIsCompletedTrue(@NonNull Long taskId);
}
