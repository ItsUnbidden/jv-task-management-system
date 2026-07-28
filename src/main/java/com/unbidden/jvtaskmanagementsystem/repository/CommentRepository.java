package com.unbidden.jvtaskmanagementsystem.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;

import com.unbidden.jvtaskmanagementsystem.dto.internal.NumberOfCommentsInTaskDto;
import com.unbidden.jvtaskmanagementsystem.model.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @NonNull
    @EntityGraph(attributePaths = {"task", "user"})
    Page<Comment> findByTaskId(@NonNull Long taskId, Pageable pageable);

    @NonNull
    @EntityGraph(attributePaths = {"user"})
    Page<Comment> findByTaskProjectId(@NonNull Long projectId, Pageable pageable);

    @NonNull
    @Override
    @EntityGraph(attributePaths = {"task", "task.project", "user"})
    Optional<Comment> findById(@NonNull Long id);

    @Query("""
        select new com.unbidden.jvtaskmanagementsystem.dto.internal.NumberOfCommentsInTaskDto(
            c.task.id,
            count(c.id)
        )
        from Comment c
        where c.task.id in :taskIds
        group by c.task.id
    """)
    List<NumberOfCommentsInTaskDto> countCommentsForTasks(@NonNull List<Long> taskIds);

    long countByTaskId(@NonNull Long taskId);
}
