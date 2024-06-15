package com.unbidden.jvtaskmanagementsystem.repository;

import com.unbidden.jvtaskmanagementsystem.model.Reply;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

public interface ReplyRepository extends JpaRepository<Reply, Long> {
    @NonNull
    @EntityGraph(attributePaths = {"parent", "replies", "user"})
    Optional<Reply> findById(@NonNull Long id);

    @NonNull
    @EntityGraph(attributePaths = {"parent", "replies", "user"})
    List<Reply> findByParentId(@NonNull Long parentId, Pageable pageable);
}
