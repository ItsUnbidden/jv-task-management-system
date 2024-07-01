package com.unbidden.jvtaskmanagementsystem.repository;

import com.unbidden.jvtaskmanagementsystem.model.ProjectCalendar;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

public interface ProjectCalendarRepository extends JpaRepository<ProjectCalendar, Long> {
    @NonNull
    Optional<ProjectCalendar> findByProjectId(@NonNull Long projectId);

    @NonNull
    @EntityGraph(attributePaths = "project")
    List<ProjectCalendar> findByCreatorId(@NonNull Long creatorId);
}
