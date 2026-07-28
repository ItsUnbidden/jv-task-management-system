package com.unbidden.jvtaskmanagementsystem.model;

import org.hibernate.Hibernate;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@EqualsAndHashCode
@Table(name = "task_events")
@SQLDelete(sql = "UPDATE task_events SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted <> true")
public class TaskEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String eventId;

    @OneToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne
    @JoinColumn(name = "project_calendar_id", nullable = false)
    private ProjectCalendar projectCalendar;

    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private boolean isDeleted;

    public TaskEvent() {
    }

    public TaskEvent(Task task, ProjectCalendar projectCalendar) {
        this.task = task;
        this.projectCalendar = projectCalendar;
    }

    @Override
    public String toString() {
        return "TaskEvent [id=" + id + (Hibernate.isInitialized(task) ? ", taskId=" + task.getId() : ", taskId=LAZY")
                + ", projectCalendar=" + projectCalendar + "]";
    }
}
