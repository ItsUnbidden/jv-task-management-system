package com.unbidden.jvtaskmanagementsystem.model;

import java.time.LocalDateTime;

import org.hibernate.Hibernate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@EqualsAndHashCode
@Table(name = "authorized_clients")
public class OAuth2AuthorizedClient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(nullable = false)
    private String clientRegistrationName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String token;
    
    @Column(nullable = false)
    private LocalDateTime aquiredAt;
    
    @Column(nullable = false)
    private Integer expiresIn;
    
    @Column(columnDefinition = "TEXT")
    private String refreshToken;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String externalAccountId;

    @Override
    public String toString() {
        return "OAuth2AuthorizedClient(id=" + id + (Hibernate.isInitialized(user) ? ", user=" + user : ", user=LAZY")
                + ", clientRegistrationName=" + clientRegistrationName + ", aquiredAt=" + aquiredAt
                + ", expiresIn=" + expiresIn + ")";
    }
}
