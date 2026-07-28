package com.unbidden.jvtaskmanagementsystem.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unbidden.jvtaskmanagementsystem.dto.internal.NumberOfRepliesInCommentDto;
import com.unbidden.jvtaskmanagementsystem.dto.internal.PartialReplyDto;
import com.unbidden.jvtaskmanagementsystem.dto.message.CommentResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.message.CommentWithTaskIdResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.message.CreateMessageRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.message.MessageResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.message.ReplyResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.message.ReplyWithCommentIdResponseDto;
import com.unbidden.jvtaskmanagementsystem.exception.EntityNotFoundException;
import com.unbidden.jvtaskmanagementsystem.exception.ErrorType;
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

import lombok.RequiredArgsConstructor;

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
    @Transactional(readOnly = true)
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, bypassIfPublic = true,
            entityIdClass = Task.class)
    public Page<CommentResponseDto> getCommentsForTask(@NonNull User user, @NonNull Long taskId,
            Pageable pageable) {
        final Page<CommentResponseDto> commentDtos = commentRepository.findByTaskId(taskId, pageable)
                .map(messageMapper::toCommentDto);
        final List<NumberOfRepliesInCommentDto> countDtos = (!commentDtos.isEmpty())
                ? replyRepository.countRepliesInCommentsForTask(taskId) : List.of();

        return mapCountDtosToComments(commentDtos, countDtos);
    }

    @NonNull
    @Override
    @Transactional(readOnly = true)
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, bypassIfPublic = true)
    public Page<CommentWithTaskIdResponseDto> getCommentsForProject(@NonNull User user,
            @NonNull Long projectId, Pageable pageable) {
        final Page<CommentWithTaskIdResponseDto> commentDtos = commentRepository.findByTaskProjectId(projectId, pageable)
                .map(messageMapper::toCommentWithTaskIdDto);
        final List<NumberOfRepliesInCommentDto> countDtos = (!commentDtos.isEmpty())
                ? replyRepository.countRepliesInCommentsForProject(projectId) : List.of();
        return mapCountDtosToComments(commentDtos, countDtos);
    }

    @NonNull
    @Override
    @Transactional(readOnly = true)
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, bypassIfPublic = true,
            entityIdClass = Message.class, entityIdParamName = "commentId")
    public CommentWithTaskIdResponseDto getCommentById(@NonNull User user,
            @NonNull Long commentId) {
        final Comment comment = entityUtil.getMessageById(commentId, Comment.class);
        final CommentWithTaskIdResponseDto dto = messageMapper.toCommentWithTaskIdDto(comment);

        dto.setNumberOfReplies(replyRepository.countByCommentId(commentId));
        return dto;
    }

    @NonNull
    @Override
    @Transactional(readOnly = true)
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, bypassIfPublic = true,
            entityIdClass = Message.class, entityIdParamName = "commentId")
    public List<ReplyResponseDto> getRepliesForComment(@NonNull User user,
            @NonNull Long commentId) {
        return buildReplyDtoTree(replyRepository.findByCommentIdOrderByTimestampAsc(commentId));
    }

    @NonNull
    @Override
    @Transactional(readOnly = true)
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, bypassIfPublic = true,
            entityIdClass = Message.class, entityIdParamName = "replyId")
    public ReplyWithCommentIdResponseDto getReplyById(@NonNull User user, @NonNull Long replyId) {
        final List<PartialReplyDto> repliesFromDb = replyRepository.findByIdWithRecursion(replyId);

        final List<ReplyResponseDto> dtos = buildReplyDtoTree(repliesFromDb
                .stream().map(messageMapper::toReplyFromPartial).toList());

        if (dtos.isEmpty()) throw new EntityNotFoundException("Reply with ID " + replyId
                + " does not exist.", ErrorType.MESSAGE_REPLY_NOT_FOUND);
        final Long commentId = repliesFromDb.get(0).getCommentId();

        return new ReplyWithCommentIdResponseDto(dtos.get(0), commentId);
    }

    @NonNull
    @Override
    @Transactional
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, bypassIfPublic = true,
            entityIdClass = Task.class)
    public CommentResponseDto leaveComment(@NonNull User user, @NonNull Long taskId,
            @NonNull CreateMessageRequestDto requestDto) {
        final Task task = entityUtil.getTaskById(taskId);

        final Comment comment = new Comment();
        comment.setTask(task);
        comment.setUser(user);
        comment.setText(requestDto.getText());
        comment.setTimestamp(LocalDateTime.now());
        comment.setIsDeleted(false);
        
        taskRepository.save(task);
        final CommentResponseDto dto = messageMapper.toCommentWithTaskIdDto(commentRepository.save(comment));

        dto.setNumberOfReplies(0L);
        return dto;
    }

    @NonNull
    @Override
    @Transactional
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, bypassIfPublic = true,
            entityIdClass = Message.class)
    public ReplyResponseDto replyToMessage(@NonNull User user, @NonNull Long messageId,
            @NonNull CreateMessageRequestDto requestDto) {
        final Message message = entityUtil.getMessageById(messageId);

        final Reply reply = new Reply();
        reply.setReplies(new ArrayList<>());
        reply.setText(requestDto.getText());
        reply.setTimestamp(LocalDateTime.now());
        reply.setUser(user);
        reply.setIsDeleted(false);

        if (message instanceof final Comment comment) {
            reply.setParent(comment);
            reply.setComment(comment);

            return messageMapper.toReplyDto(replyRepository.save(reply));
        }

        final Reply parent = (Reply)message;

        parent.getReplies().add(reply);
        reply.setParent(parent);
        reply.setComment(parent.getComment());

        replyRepository.save(reply);
        return messageMapper.toReplyDto(reply);
    }

    @NonNull
    @Override
    @Transactional
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, bypassIfPublic = true,
            entityIdClass = Message.class)
    public MessageResponseDto updateMessage(@NonNull User user, @NonNull Long messageId,
            @NonNull CreateMessageRequestDto requestDto) {
        final Message message = entityUtil.getMessageById(messageId);

        checkMessageBelongsToUser(user, message);

        message.setText(requestDto.getText());
        message.setLastUpdated(LocalDateTime.now());

        if (message instanceof final Comment comment) {
            final CommentResponseDto dto = messageMapper.toCommentWithTaskIdDto(commentRepository.save(comment));

            dto.setNumberOfReplies(replyRepository.countByCommentId(messageId));
            return dto;
        }
        return messageMapper.toReplyDto(replyRepository.save((Reply)message));
    }

    @Override
    @Transactional
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, bypassIfPublic = true,
            entityIdClass = Message.class)
    public void deleteMessage(@NonNull User user, @NonNull Long messageId) {
        final Message message = entityUtil.getMessageById(messageId);

        checkMessageBelongsToUser(user, message);

        if (message instanceof final Comment comment) {
            replyRepository.deleteByCommentId(messageId);

            commentRepository.delete(comment);
            return;
        }
        replyRepository.delete((Reply)message);
    }

    private void checkMessageBelongsToUser(User user, Message message) {
        if (!entityUtil.isManager(user) && !message.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("In order to change the message with id " 
            + message.getId() + " it needs to belong to the user with id " + user.getId());
        }
    }

    private List<ReplyResponseDto> buildReplyDtoTree(List<Reply> replies) {
        final Map<Long, List<ReplyResponseDto>> replyDtoMap = new HashMap<>();
        final List<ReplyResponseDto> topReplies = new ArrayList<>();

        for (final Reply reply : replies) {
            replyDtoMap.put(reply.getId(), new ArrayList<>());
        }
        for (final Reply reply : replies) {
            final List<ReplyResponseDto> parentReplyDtos = replyDtoMap.get(reply.getParent().getId());
            final ReplyResponseDto dto = messageMapper.toReplyDto(reply);

            dto.setReplyDtos(replyDtoMap.get(reply.getId()));
            if (parentReplyDtos == null) {
                topReplies.add(dto);
            } else {
                parentReplyDtos.add(dto);
            }
        }
        return topReplies;
    }

    private <T extends CommentResponseDto> Page<T> mapCountDtosToComments(Page<T> commentDtos,
            List<NumberOfRepliesInCommentDto> countDtos) {
        final Map<Long, Long> countMap = new HashMap<>();

        for (NumberOfRepliesInCommentDto dto : countDtos) {
            countMap.put(dto.commentId(), dto.numberOfReplies());
        }

        commentDtos.forEach(c -> {
            final Long numberOfReplies = countMap.get(c.getId());

            c.setNumberOfReplies(numberOfReplies != null ? numberOfReplies : 0);
        });

        return commentDtos;
    }
}
