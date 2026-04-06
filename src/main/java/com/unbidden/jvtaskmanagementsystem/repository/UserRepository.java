package com.unbidden.jvtaskmanagementsystem.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import com.unbidden.jvtaskmanagementsystem.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    @NonNull
    @Override
    @EntityGraph(attributePaths = "roles")
    Optional<User> findById(@NonNull Long id);

    @NonNull
    @EntityGraph(attributePaths = "roles")
    Optional<User> findByUsername(@NonNull String username);

    @NonNull
    @EntityGraph(attributePaths = "roles")
    Optional<User> findByEmail(@NonNull String email);

    @NonNull
    @Override
    @EntityGraph(attributePaths = "roles")
    Page<User> findAll(Pageable pageable);
}
