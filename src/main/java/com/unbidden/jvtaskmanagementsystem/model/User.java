package com.unbidden.jvtaskmanagementsystem.model;

import com.unbidden.jvtaskmanagementsystem.model.Role.RoleType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Data
@Table(name = "users")
@SQLDelete(sql = "UPDATE users SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted <> true")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String username;

    @ToString.Exclude
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;
    
    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private boolean isLocked;

    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private boolean isDeleted;

    @ManyToMany()
    @JoinTable(name = "users_roles", 
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        
        for (Role role : roles) {
            if (role.getRoleType().equals(RoleType.OWNER)) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + RoleType.MANAGER));
            }
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRoleType()));
        }
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !isLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return !isDeleted;
    }
}
