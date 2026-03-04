package com.unbidden.jvtaskmanagementsystem.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import com.unbidden.jvtaskmanagementsystem.model.Reply;

public interface ReplyRepository extends JpaRepository<Reply, Long> {
    @NonNull
    @Override
    @EntityGraph(attributePaths = {"parent", "replies", "user"})
    Optional<Reply> findById(@NonNull Long id);

    @NonNull
    @EntityGraph(attributePaths = {"parent", "replies", "user"})
    Page<Reply> findByParentId(@NonNull Long parentId, Pageable pageable);
}
