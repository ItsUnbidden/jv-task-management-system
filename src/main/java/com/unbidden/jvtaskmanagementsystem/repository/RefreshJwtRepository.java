package com.unbidden.jvtaskmanagementsystem.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;

import com.unbidden.jvtaskmanagementsystem.model.RefreshToken;

public interface RefreshJwtRepository extends JpaRepository<RefreshToken, Long>{
    @NonNull
    @EntityGraph(attributePaths = {"user"})
    Optional<RefreshToken> findByToken(@NonNull String token);

    @Modifying
    @Query("""
            delete from RefreshToken rt
            where rt.user.id = :userId
    """)
    int deleteAllByUserId(@NonNull Long userId);

    void deleteByToken(@NonNull String token);

    @Modifying
    @Query("""
            delete from RefreshToken rt
            where rt.expiryDate < :boundary
    """)
    int deleteAllByExpiryDateBefore(@NonNull LocalDateTime boundary);
}
