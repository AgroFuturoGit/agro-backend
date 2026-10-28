package com.ufal.smartagro.adapters.out.persistence.repository;

import com.ufal.smartagro.adapters.out.persistence.entity.FieldOccurrenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface JpaFieldOccurrenceRepository extends JpaRepository<FieldOccurrenceEntity, UUID> {
    List<FieldOccurrenceEntity> findAllByExecutionId(UUID executionId);
    @Modifying @Query("UPDATE FieldOccurrenceEntity e SET e.deletedAt = CURRENT_TIMESTAMP WHERE e.id = :id")
    void softDeleteById(@Param("id") UUID id);
}
