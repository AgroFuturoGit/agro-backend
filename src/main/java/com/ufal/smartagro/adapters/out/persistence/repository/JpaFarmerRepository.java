package com.ufal.smartagro.adapters.out.persistence.repository;

import com.ufal.smartagro.adapters.out.persistence.entity.FarmerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaFarmerRepository extends JpaRepository<FarmerEntity, UUID> {
    Optional<FarmerEntity> findByUserId(UUID userId);

    Optional<FarmerEntity> findByCpf(String cpf);

    boolean existsByCpf(String cpf);

    List<FarmerEntity> findAllByCommunityId(UUID communityId);

    @Query("SELECT f FROM FarmerEntity f WHERE f.community.organization.id = :organizationId")
    List<FarmerEntity> findAllByOrganizationId(@Param("organizationId") UUID organizationId);

    @Query("SELECT f FROM FarmerEntity f WHERE f.community.id IN :communityIds")
    List<FarmerEntity> findAllByCommunityIdIn(@Param("communityIds") Collection<UUID> communityIds);

    @Modifying
    @Query("UPDATE FarmerEntity p SET p.deletedAt = CURRENT_TIMESTAMP WHERE p.id = :id")
    void softDeleteById(@Param("id") UUID id);
}
