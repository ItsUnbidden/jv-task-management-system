package com.unbidden.jvtaskmanagementsystem.model;

import java.time.LocalDateTime;

import org.hibernate.Hibernate;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
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
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "messages")
@SQLRestriction("is_deleted <> true")
public abstract class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 2000)
    private String text;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    private LocalDateTime lastUpdated;

    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isDeleted;

    @Version
    @Column(nullable = false)
    private Long version;

    @Override
    public String toString() {
        return "Message(id=" + id + (Hibernate.isInitialized(user) ? ", user=" + user : ", user=LAZY")
                + ", text=" + text + ", timestamp=" + timestamp + ", lastUpdated=" + lastUpdated
                + ", version=" + version + ")";
    }
}
