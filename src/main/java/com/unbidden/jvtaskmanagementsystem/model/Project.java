package com.unbidden.jvtaskmanagementsystem.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Data
@Table(name = "projects")
@SQLDelete(sql = "UPDATE projects SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted <> true")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private ProjectStatus status;
    
    @OneToMany(mappedBy = "project", cascade = 
            {CascadeType.REMOVE, CascadeType.PERSIST,
            CascadeType.MERGE})
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<ProjectRole> projectRoles;

    @OneToMany(mappedBy = "project", cascade = CascadeType.REMOVE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private List<Task> tasks;

    private String dropboxProjectFolderId;

    private String dropboxProjectSharedFolderId;

    @Column(nullable = false)
    private boolean isPrivate;

    @Column(nullable = false)
    private boolean isDeleted;

    public static enum ProjectStatus {
        INITIATED,
        IN_PROGRESS, 
        COMPLETED,
        OVERDUE
    }
}
