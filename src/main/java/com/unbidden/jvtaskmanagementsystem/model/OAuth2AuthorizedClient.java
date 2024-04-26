package com.unbidden.jvtaskmanagementsystem.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Data;

@Entity
@Data
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

    @Column(nullable = false)
    private String token;
    
    @Column(nullable = false)
    private LocalDateTime aquiredAt;
    
    @Column(nullable = false)
    private Integer expiresIn;
    
    private String refreshToken;

    private String externalAccountId;
}
