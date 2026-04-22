package com.unbidden.jvtaskmanagementsystem.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
    @Query("from User u where u.username like %?1%")
    Page<User> searchByUsername(@NonNull String username, Pageable pageable);

    @NonNull
    @Query("from User u where u.email like %?1%")
    Page<User> searchByEmail(@NonNull String email, Pageable pageable);

    @NonNull
    @Query("select distinct u from User u left join fetch u.roles r where u.id in :ids")
    List<User> findAllWithRolesByIds(@NonNull List<Long> ids);
}
