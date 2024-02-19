package com.unbidden.jvtaskmanagementsystem.repository;

import com.unbidden.jvtaskmanagementsystem.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

public interface UserRepository extends JpaRepository<User, Long> {
    @NonNull
    @EntityGraph(attributePaths = "roles")
    Optional<User> findById(@NonNull Long id);

    @NonNull
    @EntityGraph(attributePaths = "roles")
    Optional<User> findByUsername(String username);

    @NonNull
    @EntityGraph(attributePaths = "roles")
    Optional<User> findByEmail(String email);
}
