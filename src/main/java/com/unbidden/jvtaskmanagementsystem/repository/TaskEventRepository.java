package com.unbidden.jvtaskmanagementsystem.repository;

import com.unbidden.jvtaskmanagementsystem.model.TaskEvent;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskEventRepository extends JpaRepository<TaskEvent, Long> {
    Optional<TaskEvent> findByTaskId(Long taskId);
}
