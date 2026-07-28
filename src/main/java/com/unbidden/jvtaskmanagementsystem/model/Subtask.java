package com.unbidden.jvtaskmanagementsystem.model;

import org.hibernate.Hibernate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
@Table(name = "subtasks")
public class Subtask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(columnDefinition = "TINYINT(1)")
    private boolean isCompleted;

    @Version
    @Column(nullable = false)
    private Long version;

    @Override
    public String toString() {
        return "Subtask(id=" + id + ", name=" + name
                + (Hibernate.isInitialized(task) ? ", taskId=" + task.getId() : ", taskId=LAZY")
                + ", isCompleted=" + isCompleted + ", version=" + version + ")";
    }
}
