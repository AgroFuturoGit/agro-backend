package com.ufal.smartagro.adapters.out.persistence.repository;

import com.ufal.smartagro.adapters.out.persistence.entity.OrganizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.UUID;

public interface JpaOrganizationRepository extends JpaRepository<OrganizationEntity, UUID> {
    boolean existsByTaxId(String taxId);

    @Modifying
    @Query("UPDATE OrganizationEntity o SET o.deletedAt = CURRENT_TIMESTAMP WHERE o.id = :id")
    void softDeleteById(@Param("id") UUID id);
}
