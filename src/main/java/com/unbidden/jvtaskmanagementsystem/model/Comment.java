package com.unbidden.jvtaskmanagementsystem.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import org.hibernate.Hibernate;
import org.hibernate.annotations.SQLDelete;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SQLDelete(sql = "UPDATE messages SET is_deleted = true, version = version + 1 WHERE id = ? AND version = ?")
public class Comment extends Message {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;

    @Override
    public String toString() {
        return "Comment(id=" + getId() + (Hibernate.isInitialized(getUser()) ? ", user=" + getUser() : ", user=LAZY")
                + ", text=" + getText() + ", timestamp=" + getTimestamp() + ", lastUpdated=" + getLastUpdated()               
                + (Hibernate.isInitialized(task) ? ", taskId=" + task.getId() : ", taskId=LAZY")
                + ", version=" + getVersion() + ")";
    }
}
