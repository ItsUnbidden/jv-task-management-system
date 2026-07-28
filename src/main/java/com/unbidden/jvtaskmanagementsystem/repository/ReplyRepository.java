package com.unbidden.jvtaskmanagementsystem.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;

import com.unbidden.jvtaskmanagementsystem.dto.internal.NumberOfRepliesInCommentDto;
import com.unbidden.jvtaskmanagementsystem.dto.internal.NumberOfRepliesInTaskDto;
import com.unbidden.jvtaskmanagementsystem.dto.internal.PartialReplyDto;
import com.unbidden.jvtaskmanagementsystem.model.Reply;

public interface ReplyRepository extends JpaRepository<Reply, Long> {
    @NonNull
    @Query(nativeQuery = true, value = """
        WITH RECURSIVE reply_tree AS (
            SELECT
                r.id,
                r.user_id,
                r.parent_id,
                r.comment_id,
                r.text,
                r.timestamp,
                r.last_updated,
                r.is_deleted,
                r.version
            FROM messages r
            WHERE r.id = ? AND r.DTYPE = "Reply"

            UNION ALL

            SELECT 
                child.id,
                child.user_id,
                child.parent_id,
                child.comment_id,
                child.text,
                child.timestamp,
                child.last_updated,
                child.is_deleted,
                child.version
            FROM messages child
            JOIN reply_tree tree 
            ON child.parent_id = tree.id
        )
        SELECT 
            r.id,
            r.user_id,
            u.username,
            r.parent_id,
            r.comment_id,
            r.text,
            r.timestamp,
            r.last_updated,
            r.is_deleted,
            r.version
        FROM reply_tree r
        JOIN users u
        ON r.user_id = u.id
        ORDER BY r.timestamp ASC
    """)
    List<PartialReplyDto> findByIdWithRecursion(@NonNull Long id);

    @NonNull
    @Override
    @EntityGraph(attributePaths = {"comment", "comment.task"})
    Optional<Reply> findById(@NonNull Long id);

    @NonNull
    @EntityGraph(attributePaths = {"parent", "user"})
    List<Reply> findByCommentIdOrderByTimestampAsc(@NonNull Long commentId);

    int deleteByCommentId(@NonNull Long commentId);

    @Query("""
        select new com.unbidden.jvtaskmanagementsystem.dto.internal.NumberOfRepliesInCommentDto(
            r.comment.id,
            count(r.id)
        )
        from Reply r
        where r.comment.task.id = :taskId
        group by r.comment.id
    """)
    List<NumberOfRepliesInCommentDto> countRepliesInCommentsForTask(@NonNull Long taskId);

    @Query("""
        select new com.unbidden.jvtaskmanagementsystem.dto.internal.NumberOfRepliesInCommentDto(
            r.comment.id,
            count(r.id)
        )
        from Reply r
        where r.comment.task.project.id = :projectId
        group by r.comment.id
    """)
    List<NumberOfRepliesInCommentDto> countRepliesInCommentsForProject(@NonNull Long projectId);

    @Query("""
        select new com.unbidden.jvtaskmanagementsystem.dto.internal.NumberOfRepliesInTaskDto(
            r.comment.task.id,
            count(r.id)
        )
        from Reply r
        where r.comment.task.id in :taskIds
        group by r.comment.task.id
    """)
    List<NumberOfRepliesInTaskDto> countRepliesForTasks(@NonNull List<Long> taskIds);

    long countByCommentId(@NonNull Long commentId);

    long countByCommentTaskId(@NonNull Long taskId);
}
