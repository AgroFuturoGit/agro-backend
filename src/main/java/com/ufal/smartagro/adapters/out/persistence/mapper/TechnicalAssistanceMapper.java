package com.ufal.smartagro.adapters.out.persistence.mapper;

import com.ufal.smartagro.adapters.out.persistence.entity.TechnicalAssistanceEntity;
import com.ufal.smartagro.domain.model.TechnicalAssistance;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TechnicalAssistanceMapper {

    private final TechnicianMapper technicianMapper;
    private final ProducerMapper producerMapper;

    public TechnicalAssistance toDomain(TechnicalAssistanceEntity entity) {
        if (entity == null) {
            return null;
        }
        return new TechnicalAssistance(
                entity.getId(),
                technicianMapper.toDomain(entity.getTechnician()),
                producerMapper.toDomain(entity.getProducer()),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    public TechnicalAssistanceEntity toEntity(TechnicalAssistance domain) {
        if (domain == null) {
            return null;
        }
        TechnicalAssistanceEntity entity = new TechnicalAssistanceEntity();
        entity.setId(domain.getId());
        entity.setTechnician(technicianMapper.toEntity(domain.getTechnician()));
        entity.setProducer(producerMapper.toEntity(domain.getProducer()));
        entity.setStartDate(domain.getStartDate());
        entity.setEndDate(domain.getEndDate());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setDeletedAt(domain.getDeletedAt());
        return entity;
    }
}
