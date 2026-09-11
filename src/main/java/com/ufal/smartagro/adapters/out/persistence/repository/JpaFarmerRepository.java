package com.ufal.smartagro.adapters.out.persistence.repository;

import com.ufal.smartagro.adapters.out.persistence.entity.FarmerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface JpaFarmerRepository extends JpaRepository<FarmerEntity, UUID> {
    Optional<FarmerEntity> findByUserId(UUID userId);
    java.util.List<FarmerEntity> findAllByCommunityId(UUID communityId);

    @Modifying
    @Query("UPDATE FarmerEntity p SET p.deletedAt = CURRENT_TIMESTAMP WHERE p.id = :id")
    void softDeleteById(@Param("id") UUID id);
}
