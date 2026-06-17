package com.ufal.smartagro.adapters.out.persistence.repository;

import com.ufal.smartagro.adapters.out.persistence.entity.ProducerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface JpaProducerRepository extends JpaRepository<ProducerEntity, UUID> {
    Optional<ProducerEntity> findByUserId(UUID userId);

    @Modifying
    @Query("UPDATE ProducerEntity p SET p.deletedAt = CURRENT_TIMESTAMP WHERE p.id = :id")
    void softDeleteById(@Param("id") UUID id);
}
