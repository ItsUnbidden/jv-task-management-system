package com.unbidden.jvtaskmanagementsystem.model;

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
@Table(name = "project_roles")
@SQLDelete(sql = "UPDATE project_roles SET is_deleted = true, version = version + 1 WHERE id = ? AND version = ?")
@SQLRestriction("is_deleted <> true")
public class ProjectRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @EqualsAndHashCode.Exclude
    private User user;

    @Column(nullable = false)
    private ProjectRoleType roleType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @EqualsAndHashCode.Exclude
    private Project project;

    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private boolean isDropboxConnected;

    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private boolean isCalendarConnected;

    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private boolean isDeleted;

    @Version
    @Column(nullable = false)
    private Long version;

    @Override
    public String toString() {
        return "ProjectRole(id=" + id + (Hibernate.isInitialized(user) ? ", user=" + user : ", user=LAZY")
                + ", roleType=" + roleType + (Hibernate.isInitialized(project) ? ", projectId=" + project.getId() : ", projectId=LAZY")
                + ", isDropboxConnected=" + isDropboxConnected + ", isCalendarConnected=" + isCalendarConnected
                + ", version=" + version + ")";
    }

    public static enum ProjectRoleType {
        CREATOR,
        ADMIN,
        CONTRIBUTOR
    }
}
