package com.ufal.smartagro.adapters.out.persistence.repository;

import com.ufal.smartagro.adapters.out.persistence.entity.TechnicianEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface JpaTechnicianRepository extends JpaRepository<TechnicianEntity, UUID> {
    Optional<TechnicianEntity> findByUserId(UUID userId);

    @Modifying
    @Query("UPDATE TechnicianEntity t SET t.deletedAt = CURRENT_TIMESTAMP WHERE t.id = :id")
    void softDeleteById(@Param("id") UUID id);
}
