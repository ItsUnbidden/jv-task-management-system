package com.unbidden.jvtaskmanagementsystem.repository;

import com.unbidden.jvtaskmanagementsystem.model.Role;
import com.unbidden.jvtaskmanagementsystem.model.Role.RoleType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

public interface RoleRepository extends JpaRepository<Role, Long> {
    @NonNull
    Optional<Role> findByRoleType(@NonNull RoleType roleType);
}
