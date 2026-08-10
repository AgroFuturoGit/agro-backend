package com.ufal.smartagro.adapters.out.persistence.repository;

import com.ufal.smartagro.adapters.out.persistence.entity.TechnicalAssistanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaTechnicalAssistanceRepository extends JpaRepository<TechnicalAssistanceEntity, UUID> {
    List<TechnicalAssistanceEntity> findByTechnicianId(UUID technicianId);
    List<TechnicalAssistanceEntity> findByProducerId(UUID producerId);
    Optional<TechnicalAssistanceEntity> findByTechnicianIdAndProducerIdAndEndDateIsNull(UUID technicianId, UUID producerId);

    @Modifying
    @Query("UPDATE TechnicalAssistanceEntity ta SET ta.deletedAt = CURRENT_TIMESTAMP WHERE ta.id = :id")
    void softDeleteById(@Param("id") UUID id);
}
