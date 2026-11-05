package com.ufal.smartagro.adapters.out.persistence.repository;

import com.ufal.smartagro.adapters.out.persistence.entity.TechnicianEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaTechnicianRepository extends JpaRepository<TechnicianEntity, UUID> {
    Optional<TechnicianEntity> findByUserId(UUID userId);

    List<TechnicianEntity> findAllByCreatedById(UUID createdById);

    @Query("""
        SELECT DISTINCT t FROM TechnicianEntity t
        WHERE t.deletedAt IS NULL AND (
            EXISTS (
                SELECT 1 FROM TechnicalAssistanceEntity ta
                WHERE ta.technician = t
                  AND ta.community.organization.id = :organizationId
                  AND ta.deletedAt IS NULL
            )
            OR EXISTS (
                SELECT 1 FROM ManagerEntity m
                WHERE m.user = t.createdBy
                  AND m.organization.id = :organizationId
                  AND m.deletedAt IS NULL
            )
        )
    """)
    List<TechnicianEntity> findAllByOrganizationId(@Param("organizationId") UUID organizationId);

    @Query("""
        SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END
        FROM TechnicianEntity t
        WHERE t.id = :technicianId
          AND t.deletedAt IS NULL
          AND (
              EXISTS (
                  SELECT 1 FROM TechnicalAssistanceEntity ta
                  WHERE ta.technician = t
                    AND ta.community.organization.id = :organizationId
                    AND ta.deletedAt IS NULL
              )
              OR EXISTS (
                  SELECT 1 FROM ManagerEntity m
                  WHERE m.user = t.createdBy
                    AND m.organization.id = :organizationId
                    AND m.deletedAt IS NULL
              )
          )
    """)
    boolean existsByIdAndOrganizationId(@Param("technicianId") UUID technicianId, @Param("organizationId") UUID organizationId);

    @Modifying
    @Query("UPDATE TechnicianEntity t SET t.deletedAt = CURRENT_TIMESTAMP WHERE t.id = :id")
    void softDeleteById(@Param("id") UUID id);
}
