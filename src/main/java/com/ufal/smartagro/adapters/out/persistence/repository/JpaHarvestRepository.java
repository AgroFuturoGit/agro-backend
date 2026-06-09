package com.ufal.smartagro.adapters.out.persistence.repository;

import com.ufal.smartagro.adapters.out.persistence.entity.HarvestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface JpaHarvestRepository extends JpaRepository <HarvestEntity, UUID> {

    boolean existsByLabel(String label);

    @Modifying
    @Query("UPDATE HarvestEntity h SET h.deletedAt = CURRENT_TIMESTAMP WHERE h.id = :id")
    void softDeleteById(@Param("id") UUID id);
}
