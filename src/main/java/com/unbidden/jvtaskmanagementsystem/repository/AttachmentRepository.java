package com.unbidden.jvtaskmanagementsystem.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import com.unbidden.jvtaskmanagementsystem.model.Attachment;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    @EntityGraph(attributePaths = "task")
    Page<Attachment> findByTaskId(@NonNull Long taskId, Pageable pageable);
}
