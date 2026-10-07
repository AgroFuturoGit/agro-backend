package com.ufal.smartagro.adapters.out.persistence.repository;

import com.ufal.smartagro.adapters.out.persistence.entity.ExecutionAttachmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaExecutionAttachmentRepository extends JpaRepository<ExecutionAttachmentEntity, UUID> {

    @Query("""
            SELECT a.id AS id,
                   a.productionExecution.id AS productionExecutionId,
                   a.clientId AS clientId,
                   a.filename AS filename,
                   a.contentType AS contentType,
                   a.sizeBytes AS sizeBytes,
                   a.createdAt AS createdAt
            FROM ExecutionAttachmentEntity a
            WHERE a.productionExecution.id = :executionId
            ORDER BY a.createdAt ASC
            """)
    List<ExecutionAttachmentView> findMetadataByExecutionId(@Param("executionId") UUID executionId);

    @Query("""
            SELECT a.id AS id,
                   a.productionExecution.id AS productionExecutionId,
                   a.clientId AS clientId,
                   a.filename AS filename,
                   a.contentType AS contentType,
                   a.sizeBytes AS sizeBytes,
                   a.createdAt AS createdAt
            FROM ExecutionAttachmentEntity a
            WHERE a.id = :id
            """)
    Optional<ExecutionAttachmentView> findMetadataById(@Param("id") UUID id);

    @Query("""
            SELECT a.id AS id,
                   a.productionExecution.id AS productionExecutionId,
                   a.clientId AS clientId,
                   a.filename AS filename,
                   a.contentType AS contentType,
                   a.sizeBytes AS sizeBytes,
                   a.createdAt AS createdAt
            FROM ExecutionAttachmentEntity a
            WHERE a.productionExecution.id = :executionId AND a.clientId = :clientId
            """)
    Optional<ExecutionAttachmentView> findMetadataByExecutionIdAndClientId(
            @Param("executionId") UUID executionId, @Param("clientId") UUID clientId);

    @Modifying
    @Query("UPDATE ExecutionAttachmentEntity a SET a.deletedAt = CURRENT_TIMESTAMP WHERE a.id = :id")
    void softDeleteById(@Param("id") UUID id);
}
