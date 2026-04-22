package com.unbidden.jvtaskmanagementsystem.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import com.unbidden.jvtaskmanagementsystem.model.RefreshToken;

public interface RefreshJwtRepository extends JpaRepository<RefreshToken, Long>{
    @NonNull
    @EntityGraph(attributePaths = {"user"})
    Optional<RefreshToken> findByToken(@NonNull String token);

    void deleteAllByUserId(@NonNull Long userId);

    void deleteByToken(@NonNull String token);
}
