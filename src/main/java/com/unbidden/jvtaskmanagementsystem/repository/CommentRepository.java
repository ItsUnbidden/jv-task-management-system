package com.unbidden.jvtaskmanagementsystem.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;

import com.unbidden.jvtaskmanagementsystem.model.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @NonNull
    @EntityGraph(attributePaths = {"task", "user"})
    Page<Comment> findByTaskId(@NonNull Long taskId, Pageable pageable);

    @NonNull
    @Query("from Comment c left join fetch c.task t left join fetch c.user u "
            + "left join fetch t.project p where p.id = :projectId")
    Page<Comment> findByProjectId(@NonNull Long projectId, Pageable pageable);

    @NonNull
    @Override
    @EntityGraph(attributePaths = {"task", "user"})
    Optional<Comment> findById(@NonNull Long id);
}
