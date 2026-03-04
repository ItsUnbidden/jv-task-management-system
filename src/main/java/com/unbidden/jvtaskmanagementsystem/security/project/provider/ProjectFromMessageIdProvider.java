package com.unbidden.jvtaskmanagementsystem.security.project.provider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.unbidden.jvtaskmanagementsystem.model.Comment;
import com.unbidden.jvtaskmanagementsystem.model.Message;
import com.unbidden.jvtaskmanagementsystem.model.Project;
import com.unbidden.jvtaskmanagementsystem.model.Reply;
import com.unbidden.jvtaskmanagementsystem.util.EntityUtil;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProjectFromMessageIdProvider implements ProjectProvider {
    private static final Logger LOGGER = LogManager.getLogger(ProjectFromMessageIdProvider.class); 

    private final EntityUtil entityUtil;

    @NonNull
    @Override
    public Project getProject(@NonNull Long id) {
        LOGGER.warn("Message id that was provided: " + id);
        final Message message = entityUtil.getMessageById(id);
        
        if (message instanceof Comment comment) {
            return getFromComment(comment);
        }
        return getFromReply((Reply)message);
    }

    @NonNull
    @Override
    public Class<?> getProviderClass() {
        return Message.class;
    }

    private Project getFromComment(Comment comment) {
        return comment.getTask().getProject();
    }

    private Project getFromReply(Reply reply) {
        Comment superParent = entityUtil.getSuperParent(reply);
        LOGGER.warn("Super parent of this reply: " + superParent.toString());
        return superParent.getTask().getProject();
    }
}
