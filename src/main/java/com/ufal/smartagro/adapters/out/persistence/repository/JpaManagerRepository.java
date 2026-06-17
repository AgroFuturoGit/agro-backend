package com.ufal.smartagro.adapters.out.persistence.repository;

import com.ufal.smartagro.adapters.out.persistence.entity.ManagerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface JpaManagerRepository extends JpaRepository<ManagerEntity, UUID> {
    Optional<ManagerEntity> findByUserId(UUID userId);

    @Modifying
    @Query("UPDATE ManagerEntity m SET m.deletedAt = CURRENT_TIMESTAMP WHERE m.id = :id")
    void softDeleteById(@Param("id") UUID id);
}
