package com.unbidden.jvtaskmanagementsystem.service.impl;

import com.unbidden.jvtaskmanagementsystem.dto.message.CommentResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.message.CommentWithTaskIdResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.message.CreateMessageRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.message.MessageResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.message.ReplyResponseDto;
import com.unbidden.jvtaskmanagementsystem.mapper.MessageMapper;
import com.unbidden.jvtaskmanagementsystem.model.Comment;
import com.unbidden.jvtaskmanagementsystem.model.Message;
import com.unbidden.jvtaskmanagementsystem.model.ProjectRole.ProjectRoleType;
import com.unbidden.jvtaskmanagementsystem.model.Reply;
import com.unbidden.jvtaskmanagementsystem.model.Task;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.repository.CommentRepository;
import com.unbidden.jvtaskmanagementsystem.repository.ReplyRepository;
import com.unbidden.jvtaskmanagementsystem.repository.TaskRepository;
import com.unbidden.jvtaskmanagementsystem.security.project.ProjectSecurity;
import com.unbidden.jvtaskmanagementsystem.service.MessageService;
import com.unbidden.jvtaskmanagementsystem.util.EntityUtil;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    private final CommentRepository commentRepository;

    private final ReplyRepository replyRepository;

    private final TaskRepository taskRepository;

    private final EntityUtil entityUtil;

    private final MessageMapper messageMapper;

    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, bypassIfPublic = true,
            entityIdClass = Task.class)
    public List<CommentResponseDto> getCommentsForTask(@NonNull User user, @NonNull Long taskId,
            Pageable pageable) {
        return commentRepository.findByTaskId(taskId, pageable).stream()
                .map(messageMapper::toCommentDto)
                .toList();
    }

    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, bypassIfPublic = true)
    public List<CommentWithTaskIdResponseDto> getCommentsForProject(@NonNull User user,
            @NonNull Long projectId, Pageable pageable) {
        return commentRepository.findByProjectId(projectId, pageable).stream()
                .map(messageMapper::toCommentWithTaskIdDto)
                .toList();
    }

    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, bypassIfPublic = true,
            entityIdClass = Message.class, entityIdParamName = "commentId")
    public CommentWithTaskIdResponseDto getCommentById(@NonNull User user,
            @NonNull Long commentId) {
        final Comment comment = (Comment)entityUtil.getMessageById(commentId, Comment.class);
        return messageMapper.toCommentWithTaskIdDto(comment);
    }

    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, bypassIfPublic = true,
            entityIdClass = Message.class, entityIdParamName = "commentId")
    public List<ReplyResponseDto> getRepliesForComment(@NonNull User user,
            @NonNull Long commentId, Pageable pageable) {
        return replyRepository.findByParentId(commentId, pageable).stream()
                .map(messageMapper::toReplyDto)
                .toList();
    }

    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, bypassIfPublic = true,
            entityIdClass = Message.class, entityIdParamName = "replyId")
    public ReplyResponseDto getReplyById(@NonNull User user, @NonNull Long replyId) {
        final Reply reply = (Reply)entityUtil.getMessageById(replyId, Reply.class);

        return messageMapper.toReplyDto(reply);
    }

    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, bypassIfPublic = true,
            entityIdClass = Task.class)
    public CommentResponseDto leaveComment(@NonNull User user, @NonNull Long taskId,
            @NonNull CreateMessageRequestDto requestDto) {
        final Task task = entityUtil.getTaskById(taskId);

        Comment comment = new Comment();
        comment.setTask(task);
        comment.setUser(user);
        comment.setText(requestDto.getText());
        comment.setTimestamp(LocalDateTime.now());
        comment.setAmountOfReplies(0);
        task.setAmountOfMessages(task.getAmountOfMessages() + 1);
        
        taskRepository.save(task);
        return messageMapper.toCommentWithTaskIdDto(commentRepository.save(comment));
    }

    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, bypassIfPublic = true,
            entityIdClass = Message.class)
    public ReplyResponseDto replyToMessage(@NonNull User user, @NonNull Long messageId,
            @NonNull CreateMessageRequestDto requestDto) {
        final Message message = entityUtil.getMessageById(messageId);

        Reply reply = new Reply();
        reply.setReplies(new ArrayList<>());
        reply.setText(requestDto.getText());
        reply.setTimestamp(LocalDateTime.now());
        reply.setUser(user);

        if (message instanceof Comment) {
            Comment comment = (Comment)message;

            final Task task = comment.getTask();
            task.setAmountOfMessages(task.getAmountOfMessages() + 1);

            comment.setAmountOfReplies(comment.getAmountOfReplies() + 1);
            
            reply.setParent(comment);

            commentRepository.save(comment);
            taskRepository.save(task);
            return messageMapper.toReplyDto(replyRepository.save(reply));
        }

        Reply parent = (Reply)message;
        
        Comment superParentComment = entityUtil.getSuperParent(parent);
        superParentComment.setAmountOfReplies(superParentComment.getAmountOfReplies() + 1);
        parent.getReplies().add(reply);
        reply.setParent(parent);

        final Task task = superParentComment.getTask();
        task.setAmountOfMessages(task.getAmountOfMessages() + 1);

        replyRepository.save(reply);
        replyRepository.save(parent);
        commentRepository.save(superParentComment);
        taskRepository.save(task);
        return messageMapper.toReplyDto(reply);
    }

    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, bypassIfPublic = true,
            entityIdClass = Message.class)
    public MessageResponseDto updateMessage(@NonNull User user, @NonNull Long messageId,
            @NonNull CreateMessageRequestDto requestDto) {
        final Message message = entityUtil.getMessageById(messageId);

        checkMessageBelongsToUser(user, message);

        message.setText(requestDto.getText());
        message.setLastUpdated(LocalDateTime.now());

        if (message instanceof Comment) {
            return messageMapper.toCommentWithTaskIdDto(commentRepository.save((Comment)message));
        }
        return messageMapper.toReplyDto(replyRepository.save((Reply)message));
    }

    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, bypassIfPublic = true,
            entityIdClass = Message.class)
    public void deleteMessage(@NonNull User user, @NonNull Long messageId) {
        final Message message = entityUtil.getMessageById(messageId);

        checkMessageBelongsToUser(user, message);

        if (message instanceof Comment) {
            final Comment comment = (Comment)message;
            List<Reply> replies = replyRepository.findByParentId(messageId, null);

            replyRepository.deleteAll(replies);
            final Task task = comment.getTask();
            task.setAmountOfMessages(task.getAmountOfMessages()
                    - comment.getAmountOfReplies() - 1);
            
            commentRepository.delete(comment);
            taskRepository.save(task);
            return;
        }
        Reply reply = (Reply)message;
        Comment superParent = entityUtil.getSuperParent(reply);
        int amountOfDeletedReplies = unwindReplies(reply).size();

        superParent.setAmountOfReplies(superParent.getAmountOfReplies()
                - amountOfDeletedReplies);
        final Task task = superParent.getTask();
        task.setAmountOfMessages(task.getAmountOfMessages() - amountOfDeletedReplies);

        taskRepository.save(task);
        commentRepository.save(superParent);
        replyRepository.delete(reply);
    }

    private void checkMessageBelongsToUser(User user, Message message) {
        if (!entityUtil.isManager(user) && message.getUser().getId() != user.getId()) {
            throw new AccessDeniedException("In order to change the message with id " 
            + message.getId() + " it needs to belong to the user with id " + user.getId());
        }
    }

    private List<Reply> unwindReplies(Reply reply) {
        List<Reply> replies = new ArrayList<>();

        replies.add(reply);
        for (Reply innerReply : reply.getReplies()) {
            replies.addAll(unwindReplies(innerReply));
            
        }
        return replies;
    }
}
