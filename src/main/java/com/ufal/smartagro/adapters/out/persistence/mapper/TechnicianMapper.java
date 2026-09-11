package com.ufal.smartagro.adapters.out.persistence.mapper;

import com.ufal.smartagro.adapters.out.persistence.entity.TechnicianEntity;
import com.ufal.smartagro.domain.model.Technician;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TechnicianMapper {

    private final UserMapper userMapper;

    public Technician toDomain(TechnicianEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Technician(
                entity.getId(),
                userMapper.toDomain(entity.getUser()),
                entity.getProfessionalId(),
                entity.getSpecialty(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    public TechnicianEntity toEntity(Technician domain) {
        if (domain == null) {
            return null;
        }
        TechnicianEntity entity = new TechnicianEntity();
        entity.setId(domain.getId());
        entity.setUser(userMapper.toEntity(domain.getUser()));
        entity.setProfessionalId(domain.getProfessionalId());
        entity.setSpecialty(domain.getSpecialty());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setDeletedAt(domain.getDeletedAt());
        return entity;
    }
}
