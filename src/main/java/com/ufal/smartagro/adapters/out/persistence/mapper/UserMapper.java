package com.ufal.smartagro.adapters.out.persistence.mapper;

import com.ufal.smartagro.adapters.out.persistence.entity.UserEntity;
import com.ufal.smartagro.domain.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        return new User(
                entity.getId(),
                entity.getFullName(),
                entity.getEmail(),
                entity.getPassword(),
                entity.getCpf(),
                entity.getDateOfBirth(),
                entity.getRole()
        );
    }

    public UserEntity toEntity(User domain) {
        if (domain == null) {
            return null;
        }
        UserEntity entity = new UserEntity();
        entity.setId(domain.getId());
        entity.setFullName(domain.getFullName());
        entity.setEmail(domain.getEmail());
        entity.setPassword(domain.getPassword());
        entity.setCpf(domain.getCpf());
        entity.setDateOfBirth(domain.getDateOfBirth());
        entity.setRole(domain.getRole());
        return entity;
    }
}
