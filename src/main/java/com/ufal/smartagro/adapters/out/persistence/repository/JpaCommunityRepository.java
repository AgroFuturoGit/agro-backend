package com.ufal.smartagro.adapters.out.persistence.repository;

import com.ufal.smartagro.adapters.out.persistence.entity.CommunityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.UUID;

public interface JpaCommunityRepository extends JpaRepository<CommunityEntity, UUID> {
    java.util.List<CommunityEntity> findAllByOrganizationId(UUID organizationId);

    @Modifying
    @Query("UPDATE CommunityEntity c SET c.deletedAt = CURRENT_TIMESTAMP WHERE c.id = :id")
    void softDeleteById(@Param("id") UUID id);
}
