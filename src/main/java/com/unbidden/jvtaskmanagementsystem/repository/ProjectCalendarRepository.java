package com.unbidden.jvtaskmanagementsystem.repository;

import com.unbidden.jvtaskmanagementsystem.model.ProjectCalendar;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectCalendarRepository extends JpaRepository<ProjectCalendar, Long> {
    Optional<ProjectCalendar> findByProjectId(Long projectId);

    @EntityGraph(attributePaths = "project")
    List<ProjectCalendar> findByCreatorId(Long creatorId);
}
