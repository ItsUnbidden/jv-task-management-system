package com.unbidden.jvtaskmanagementsystem.util;

import com.unbidden.jvtaskmanagementsystem.exception.EntityNotFoundException;
import com.unbidden.jvtaskmanagementsystem.model.Attachment;
import com.unbidden.jvtaskmanagementsystem.model.ClientRegistration;
import com.unbidden.jvtaskmanagementsystem.model.Comment;
import com.unbidden.jvtaskmanagementsystem.model.Label;
import com.unbidden.jvtaskmanagementsystem.model.Message;
import com.unbidden.jvtaskmanagementsystem.model.Project;
import com.unbidden.jvtaskmanagementsystem.model.ProjectRole;
import com.unbidden.jvtaskmanagementsystem.model.Reply;
import com.unbidden.jvtaskmanagementsystem.model.Role.RoleType;
import com.unbidden.jvtaskmanagementsystem.model.Task;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.repository.AttachmentRepository;
import com.unbidden.jvtaskmanagementsystem.repository.CommentRepository;
import com.unbidden.jvtaskmanagementsystem.repository.LabelRepository;
import com.unbidden.jvtaskmanagementsystem.repository.ProjectRepository;
import com.unbidden.jvtaskmanagementsystem.repository.ProjectRoleRepository;
import com.unbidden.jvtaskmanagementsystem.repository.ReplyRepository;
import com.unbidden.jvtaskmanagementsystem.repository.TaskRepository;
import com.unbidden.jvtaskmanagementsystem.repository.UserRepository;
import com.unbidden.jvtaskmanagementsystem.repository.oauth2.ClientRegistrationRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@SuppressWarnings("null")
public class EntityUtil {
    private final ProjectRepository projectRepository;

    private final ProjectRoleRepository projectRoleRepository;

    private final UserRepository userRepository;

    private final TaskRepository taskRepository;

    private final LabelRepository labelRepository;

    private final CommentRepository commentRepository;

    private final ReplyRepository replyRepository;

    private final AttachmentRepository attachmentRepository;

    private final ClientRegistrationRepository clientRegistrationRepository;

    @NonNull
    public Project getProjectById(@NonNull Long projectId) {
        return projectRepository.findById(projectId).orElseThrow(() ->
                new EntityNotFoundException("Was not able to find a project with id "
                + projectId));
    }

    @NonNull
    public User getUserById(@NonNull Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new EntityNotFoundException("Was not able to find a user with id " 
                + userId));
    }

    @NonNull 
    public Task getTaskById(@NonNull Long taskId) {
        return taskRepository.findById(taskId).orElseThrow(() ->
                new EntityNotFoundException("Was not able to find a task with id " 
                + taskId));
    }

    @NonNull
    public Label getLabelById(@NonNull Long labelId) {
        return labelRepository.findById(labelId).orElseThrow(() ->
                new EntityNotFoundException("Was not able to find a label with id " 
                + labelId));
    }

    @NonNull
    public Attachment getAttachmentById(@NonNull Long attachmentId) {
        return attachmentRepository.findById(attachmentId).orElseThrow(() ->
                new EntityNotFoundException("Was not able to find an attachment with id "
                + attachmentId));
    }

    /**
     * This method finds an instance of {@link Message} entity in the database. 
     * There is no guarantee that a desired type will be returned and therefore it 
     * is safer to use {@link #getMessageById(Long, Class)}.
     * @throws EntityNotFoundException if no entity was found
     * @param messageId of either a comment or a reply
     * @return <b>message</b> which can be an instance of either a comment or a reply
     */
    @NonNull
    public Message getMessageById(@NonNull Long messageId) {
        Optional<Comment> commentOptional = commentRepository.findById(messageId);
        if (commentOptional.isPresent()) {
            return commentOptional.get();
        } 

        Optional<Reply> replyOptional = replyRepository.findById(messageId);
        return replyOptional.orElseThrow(() -> new EntityNotFoundException(
                "There is neither a comment nor a reply with id " + messageId));
    }

    /**
     * This method finds an instance of {@link Message} entity in the database. 
     * It is guaranteed that only a desired type of message will be returned.
     * @throws EntityNotFoundException if no entity was found
     * @param messageId of either a comment or a reply
     * @param clazz of desired message type
     * @return <b>message</b> which can be an instance of either a comment or a reply
     */
    @NonNull
    public Message getMessageById(@NonNull Long messageId,
            @NonNull Class<? extends Message> clazz) {
        if (clazz.equals(Comment.class)) {
            return commentRepository.findById(messageId).orElseThrow(() -> 
                    new EntityNotFoundException("Was not able to find a comment with id "
                    + messageId));
        }
        return replyRepository.findById(messageId).orElseThrow(() -> 
                new EntityNotFoundException("Was not able to find a reply with id "
                + messageId));
    }

    @NonNull
    public ProjectRole getProjectRoleByProjectIdAndUserId(@NonNull Long projectId,
            @NonNull Long userId) {
        return projectRoleRepository.findByProjectIdWithUserId(
                projectId, userId).orElseThrow(() -> new AccessDeniedException(
                "User is not a member of this project."));
    }

    @NonNull
    public ClientRegistration getClientRegistrationByName(
            @NonNull String clientRegistrationName) {
        return clientRegistrationRepository.findByClientName(clientRegistrationName)
                .orElseThrow(() -> new EntityNotFoundException("There is no client registration "
                + "with name " + clientRegistrationName));
    }

    public boolean isManager(@NonNull User user) {
        for (GrantedAuthority grantedAuthority : user.getAuthorities()) {
            if (grantedAuthority.getAuthority().equals("ROLE_" + RoleType.MANAGER)) {
                return true;
            }      
        }
        return false;
    }

    /**
     * This method returns the {@link Comment} this {@link Reply} ultimately belongs to.
     * @param reply for which is needed to find comment
     * @return {@link Comment}
     */
    @NonNull
    public Comment getSuperParent(@NonNull Reply reply) {
        Message superParent;
        do {
            superParent = reply.getParent();
        } while (superParent instanceof Reply);

        return (Comment)superParent;
    }
}
