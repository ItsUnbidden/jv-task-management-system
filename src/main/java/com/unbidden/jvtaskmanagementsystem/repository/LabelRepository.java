package com.unbidden.jvtaskmanagementsystem.repository;

import com.unbidden.jvtaskmanagementsystem.model.Label;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

public interface LabelRepository extends JpaRepository<Label, Long> {
    @NonNull
    @EntityGraph(attributePaths = {"project", "tasks"})
    Optional<Label> findById(@NonNull Long id);

    @NonNull
    @EntityGraph(attributePaths = {"project", "tasks"})
    List<Label> findByProjectId(@NonNull Long projectId);
}
