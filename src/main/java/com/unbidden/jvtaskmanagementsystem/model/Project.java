package com.unbidden.jvtaskmanagementsystem.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.hibernate.Hibernate;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
@Table(name = "projects")
@SQLDelete(sql = "UPDATE projects SET is_deleted = true, version = version + 1 WHERE id = ? AND version = ?")
@SQLRestriction("is_deleted <> true")
@Access(AccessType.FIELD)
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @Column(nullable = false)
    private ProjectStatus status;
    
    @OneToMany(mappedBy = "project", cascade = 
            {CascadeType.REMOVE, CascadeType.PERSIST,
            CascadeType.MERGE})
    @EqualsAndHashCode.Exclude
    private Set<ProjectRole> projectRoles;

    @OneToMany(mappedBy = "project", cascade = CascadeType.REMOVE)
    @EqualsAndHashCode.Exclude
    private List<Task> tasks;

    @Column(nullable = false)
    private Integer progress;

    private String dropboxProjectFolderId;

    private String dropboxProjectSharedFolderId;

    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private boolean isCalendarConnected;

    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private boolean isPrivate;

    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private boolean isDeleted;

    @Version
    @Column(nullable = false)
    private Long version;

    @Override
    public String toString() {
        return "Project(id=" + id + ", name=" + name + ", description=" + description + ", startDate=" + startDate
                + ", endDate=" + endDate + ", status=" + status
                + (Hibernate.isInitialized(projectRoles) ? ", projectRoles=" + projectRoles : ", projectRoles=LAZY")
                + (Hibernate.isInitialized(tasks) ? ", taskIds=" + tasks.stream().map(t -> t.getId()).toList() : ", taskIds=LAZY")
                + ", progress=" + progress + ", isPrivate=" + isPrivate + ", version=" + version + ")";
    }

    public boolean isDropboxConnected() {
        return dropboxProjectFolderId != null;
    }

    public static enum ProjectStatus {
        INITIATED,
        IN_PROGRESS, 
        COMPLETED,
        OVERDUE
    }
}
