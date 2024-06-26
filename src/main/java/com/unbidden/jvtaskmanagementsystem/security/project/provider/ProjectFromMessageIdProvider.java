package com.unbidden.jvtaskmanagementsystem.security.project.provider;

import com.unbidden.jvtaskmanagementsystem.model.Comment;
import com.unbidden.jvtaskmanagementsystem.model.Message;
import com.unbidden.jvtaskmanagementsystem.model.Project;
import com.unbidden.jvtaskmanagementsystem.model.Reply;
import com.unbidden.jvtaskmanagementsystem.util.EntityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectFromMessageIdProvider implements ProjectProvider {
    private final EntityUtil entityUtil;

    @NonNull
    @Override
    public Project getProject(@NonNull Long id) {
        final Message message = entityUtil.getMessageById(id);
        
        if (message instanceof Comment) {
            return getFromComment((Comment)message);
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
        return superParent.getTask().getProject();
    }
}
