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
import com.unbidden.jvtaskmanagementsystem.security.project.ProjectSecurity;
import com.unbidden.jvtaskmanagementsystem.service.MessageService;
import com.unbidden.jvtaskmanagementsystem.service.util.EntityUtil;
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
@SuppressWarnings("null")
public class MessageServiceImpl implements MessageService {
    private final CommentRepository commentRepository;

    private final ReplyRepository replyRepository;

    private final EntityUtil entityUtil;

    private final MessageMapper messageMapper;

    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, bypassIfPublic = true,
            entityIdClass = Task.class, entityIdParamName = "taskId")
    public List<CommentResponseDto> getCommentsForTask(User user, @NonNull Long taskId,
            Pageable pageable) {
        return commentRepository.findByTaskId(taskId, pageable).stream()
                .map(messageMapper::toCommentDto)
                .toList();
    }

    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, bypassIfPublic = true)
    public List<CommentWithTaskIdResponseDto> getCommentsForProject(User user,
            @NonNull Long projectId, Pageable pageable) {
        return commentRepository.findByProjectId(projectId, pageable).stream()
                .map(messageMapper::toCommentWithTaskIdDto)
                .toList();
    }

    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, bypassIfPublic = true,
            entityIdClass = Message.class, entityIdParamName = "commentId")
    public CommentWithTaskIdResponseDto getCommentById(User user, @NonNull Long commentId) {
        final Comment comment = (Comment)entityUtil.getMessageById(commentId, Comment.class);
        return messageMapper.toCommentWithTaskIdDto(comment);
    }

    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, bypassIfPublic = true,
            entityIdClass = Message.class, entityIdParamName = "commentId")
    public List<ReplyResponseDto> getRepliesForComment(User user, @NonNull Long commentId,
            Pageable pageable) {
        return replyRepository.findByParentId(commentId, pageable).stream()
                .map(messageMapper::toReplyDto)
                .toList();
    }

    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, bypassIfPublic = true,
            entityIdClass = Message.class, entityIdParamName = "replyId")
    public ReplyResponseDto getReplyById(User user, @NonNull Long replyId) {
        final Reply reply = (Reply)entityUtil.getMessageById(replyId, Reply.class);

        return messageMapper.toReplyDto(reply);
    }

    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, bypassIfPublic = true,
            entityIdClass = Task.class, entityIdParamName = "taskId")
    public CommentResponseDto leaveComment(User user, @NonNull Long taskId,
            @NonNull CreateMessageRequestDto requestDto) {
        final Task task = entityUtil.getTaskById(taskId);

        Comment comment = new Comment();
        comment.setTask(task);
        comment.setUser(user);
        comment.setText(requestDto.getText());
        comment.setTimestamp(LocalDateTime.now());
        comment.setAmountOfReplies(0);
        
        return messageMapper.toCommentWithTaskIdDto(commentRepository.save(comment));
    }

    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, bypassIfPublic = true,
            entityIdClass = Message.class, entityIdParamName = "messageId")
    public ReplyResponseDto replyToMessage(User user, @NonNull Long messageId,
            @NonNull CreateMessageRequestDto requestDto) {
        final Message message = entityUtil.getMessageById(messageId);

        Reply reply = new Reply();
        reply.setReplies(new ArrayList<>());
        reply.setText(requestDto.getText());
        reply.setTimestamp(LocalDateTime.now());
        reply.setUser(user);

        if (message instanceof Comment) {
            Comment comment = (Comment)message;
            comment.setAmountOfReplies(comment.getAmountOfReplies() + 1);

            reply.setParent(comment);

            commentRepository.save(comment);
            return messageMapper.toReplyDto(replyRepository.save(reply));
        }

        Reply parent = (Reply)message;
        
        Comment superParentComment = entityUtil.getSuperParent(parent);
        superParentComment.setAmountOfReplies(superParentComment.getAmountOfReplies() + 1);
        parent.getReplies().add(reply);
        reply.setParent(parent);

        replyRepository.save(reply);
        replyRepository.save(parent);
        commentRepository.save(superParentComment);
        return messageMapper.toReplyDto(reply);
    }

    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, bypassIfPublic = true,
            entityIdClass = Message.class, entityIdParamName = "messageId")
    public MessageResponseDto updateMessage(User user, @NonNull Long messageId,
            @NonNull CreateMessageRequestDto requestDto) {
        final Message message = entityUtil.getMessageById(messageId);

        checkMessageBelongsToUser(user, message);

        message.setText(requestDto.getText());
        message.setTimestamp(LocalDateTime.now());

        if (message instanceof Comment) {
            return messageMapper.toCommentWithTaskIdDto(commentRepository.save((Comment)message));
        }
        return messageMapper.toReplyDto(replyRepository.save((Reply)message));
    }

    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, bypassIfPublic = true,
            entityIdClass = Message.class, entityIdParamName = "messageId")
    public void deleteMessage(User user, @NonNull Long messageId) {
        final Message message = entityUtil.getMessageById(messageId);

        checkMessageBelongsToUser(user, message);

        if (message instanceof Comment) {
            List<Reply> replies = replyRepository.findByParentId(messageId, null);
            replyRepository.deleteAll(replies);
            commentRepository.delete((Comment)message);
            return;
        }
        Reply reply = (Reply)message;
        Comment superParent = entityUtil.getSuperParent(reply);
        superParent.setAmountOfReplies(superParent.getAmountOfReplies()
                - unwindReplies(reply).size());
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
