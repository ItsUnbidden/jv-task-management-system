package com.unbidden.jvtaskmanagementsystem.model;

import java.time.LocalDate;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@EqualsAndHashCode
@Table(name = "tasks")
@SQLDelete(sql = "UPDATE tasks SET is_deleted = true, version = version + 1 WHERE id = ? AND version = ?")
@SQLRestriction("is_deleted <> true")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private TaskPriority priority;

    @Column(nullable = false)
    private TaskStatus status;

    private LocalDate dueDate;

    private String dropboxTaskFolderId;

    @Column(nullable = false)
    private Integer progress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id", nullable = false)
    private User assignee;

    @ManyToMany()
    @JoinTable(name = "tasks_labels",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "label_id"))
    private List<Label> labels;

    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private boolean isDeleted;

    @Version
    @Column(nullable = false)
    private Long version;

    @Transient
    private Long numberOfMessages;

    @Override
    public String toString() {
        return "Task(id=" + id + ", name=" + name + ", description=" + description + ", priority=" + priority
                + ", status=" + status + ", dueDate=" + dueDate + ", progress=" + progress
                + (Hibernate.isInitialized(project) ? ", projectId=" + project.getId() : ", projectId=LAZY")
                + (Hibernate.isInitialized(assignee) ? ", assignee=" + assignee : ", assignee=LAZY")
                + (Hibernate.isInitialized(labels) ? ", labels=" + labels : ", labels=LAZY")
                + ", numberOfMessages=" + numberOfMessages + ", version=" + version + ")";
    }

    public static enum TaskStatus {
        NOT_STARTED,
        IN_PROGRESS,
        COMPLETED,
        OVERDUE
    }

    public static enum TaskPriority {
        LOW,
        MEDIUM,
        HIGH
    }
}
