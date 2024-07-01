package com.unbidden.jvtaskmanagementsystem.repository;

import com.unbidden.jvtaskmanagementsystem.model.TaskEvent;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

public interface TaskEventRepository extends JpaRepository<TaskEvent, Long> {
    @NonNull
    Optional<TaskEvent> findByTaskId(@NonNull Long taskId);
}
