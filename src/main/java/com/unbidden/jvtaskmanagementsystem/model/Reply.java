package com.unbidden.jvtaskmanagementsystem.model;

import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SQLDelete(sql = "UPDATE messages SET is_deleted = true, version = version + 1 WHERE id = ? AND version = ?")
public class Reply extends Message {
    @ManyToOne()
    @JoinColumn(name = "parent_id")
    private Message parent;

    @ManyToOne()
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE)
    @SQLRestriction("is_deleted <> true")
    private List<Reply> replies;

    @Override
    public String toString() {
        return "Reply(id=" + getId() + (Hibernate.isInitialized(getUser()) ? ", user=" + getUser() : ", user=LAZY")
                + ", text=" + getText() + ", timestamp=" + getTimestamp() + ", lastUpdated=" + getLastUpdated()               
                + (Hibernate.isInitialized(parent) ? ", parentId=" + parent.getId() : ", parentId=LAZY")
                + (Hibernate.isInitialized(comment) ? ", commentId=" + comment.getId() : ", commentId=LAZY")
                + (Hibernate.isInitialized(replies) ? ", replyIds=" + replies.stream().map(r -> r.getId()).toList() : ", replyIds=LAZY")
                + ", version=" + getVersion() + ")";
    }
}
