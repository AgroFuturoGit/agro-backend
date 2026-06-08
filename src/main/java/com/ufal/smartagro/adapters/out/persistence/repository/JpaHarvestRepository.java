package com.ufal.smartagro.adapters.out.persistence.repository;

import com.ufal.smartagro.adapters.out.persistence.entity.HarvestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.UUID;

public interface JpaHarvestRepository extends JpaRepository <HarvestEntity, UUID> {

    boolean existsByLabel(String label);

    boolean existsByStartDateAndEndDate(
            LocalDate startDate,
            LocalDate endDate
    );

    @Modifying
    @Query("UPDATE CropEntity c SET c.deletedAt = CURRENT_TIMESTAMP WHERE c.id = :id")
    void softDeleteById(@Param("id") UUID id);
}
