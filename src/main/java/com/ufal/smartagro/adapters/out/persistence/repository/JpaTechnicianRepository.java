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

    @Query("SELECT DISTINCT ta.technician FROM TechnicalAssistanceEntity ta WHERE ta.producer.community.organization.id = :organizationId AND ta.deletedAt IS NULL AND ta.technician.deletedAt IS NULL")
    java.util.List<TechnicianEntity> findAllByOrganizationId(@Param("organizationId") UUID organizationId);
    @Query("SELECT CASE WHEN COUNT(ta) > 0 THEN true ELSE false END FROM TechnicalAssistanceEntity ta WHERE ta.technician.id = :technicianId AND ta.producer.community.organization.id = :organizationId AND ta.deletedAt IS NULL AND ta.technician.deletedAt IS NULL")
    boolean existsByIdAndOrganizationId(@Param("technicianId") UUID technicianId, @Param("organizationId") UUID organizationId);


    @Modifying
    @Query("UPDATE TechnicianEntity t SET t.deletedAt = CURRENT_TIMESTAMP WHERE t.id = :id")
    void softDeleteById(@Param("id") UUID id);
}
