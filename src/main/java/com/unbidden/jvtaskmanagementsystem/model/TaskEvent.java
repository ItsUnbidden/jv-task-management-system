package com.unbidden.jvtaskmanagementsystem.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Data
@Table(name = "task_events")
@SQLDelete(sql = "UPDATE task_events SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted <> true")
public class TaskEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String eventId;

    @OneToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne
    @JoinColumn(name = "project_calendar_id", nullable = false)
    private ProjectCalendar projectCalendar;

    @Column(nullable = false)
    private boolean isDeleted;

    public TaskEvent() {
    }

    public TaskEvent(Task task, ProjectCalendar projectCalendar) {
        this.task = task;
        this.projectCalendar = projectCalendar;
    }
}
