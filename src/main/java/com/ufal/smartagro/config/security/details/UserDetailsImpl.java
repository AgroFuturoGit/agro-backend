package com.ufal.smartagro.config.security.details;

import com.ufal.smartagro.adapters.out.persistence.entity.UserEntity;
import com.ufal.smartagro.domain.model.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
public class UserDetailsImpl implements UserDetails {
    private final UserEntity user;
    private final User domainUser;

    public UserDetailsImpl(UserEntity user) {
        this.user = user;
        this.domainUser = user != null ? User.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .password(user.getPassword())
                .cpf(user.getCpf())
                .role(user.getRole())
                .build() : null;
    }

    public UserDetailsImpl(User domainUser, String password) {
        this.domainUser = domainUser;
        if (domainUser != null) {
            UserEntity entity = new UserEntity();
            entity.setId(domainUser.getId());
            entity.setFullName(domainUser.getFullName());
            entity.setEmail(domainUser.getEmail());
            entity.setPassword(password != null ? password : domainUser.getPassword());
            entity.setCpf(domainUser.getCpf());
            entity.setRole(domainUser.getRole());
            this.user = entity;
        } else {
            this.user = null;
        }
    }

    public User toDomainUser() {
        if (user != null) {
            return User.builder()
                    .id(user.getId())
                    .fullName(user.getFullName())
                    .email(user.getEmail())
                    .password(user.getPassword())
                    .cpf(user.getCpf())
                    .role(user.getRole())
                    .build();
        }
        return domainUser;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (user != null && user.getRole() != null) {
            return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        }
        if (domainUser != null && domainUser.getRole() != null) {
            return List.of(new SimpleGrantedAuthority("ROLE_" + domainUser.getRole().name()));
        }
        return List.of();
    }

    @Override
    public String getPassword() {
        if (user != null) {
            return user.getPassword();
        }
        return domainUser != null ? domainUser.getPassword() : null;
    }

    @Override
    public String getUsername() {
        if (user != null) {
            return user.getEmail();
        }
        return domainUser != null ? domainUser.getEmail() : null;
    }

    public String getName() {
        if (user != null) {
            return user.getFullName();
        }
        return domainUser != null ? domainUser.getFullName() : null;
    }

    public UUID getId() {
        if (user != null) {
            return user.getId();
        }
        return domainUser != null ? domainUser.getId() : null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
