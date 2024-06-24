package com.unbidden.jvtaskmanagementsystem.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.Set;
import lombok.Data;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Data
@Table(name = "project_calendars")
@SQLDelete(sql = "UPDATE project_calendars SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted <> true")
public class ProjectCalendar {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String calendarId;

    @Column(nullable = false)
    private String startEventId;

    private String endEventId;

    @OneToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @OneToMany(mappedBy = "projectCalendar", cascade = CascadeType.REMOVE)
    private Set<TaskEvent> taskEvents;

    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private boolean isDeleted;

    public ProjectCalendar() {
    }

    public ProjectCalendar(Project project, User creator) {
        this.project = project;
        this.creator = creator;
    }
}
