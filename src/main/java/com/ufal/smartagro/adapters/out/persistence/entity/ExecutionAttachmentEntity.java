package com.ufal.smartagro.adapters.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "execution_attachments")
@EntityListeners(AuditingEntityListener.class)
@SQLRestriction("deleted_at IS NULL")
public class ExecutionAttachmentEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "execution_id", nullable = false) private ProductionExecutionEntity execution;
    @Column(name = "file_url", nullable = false, length = 500) private String fileUrl;
    @Column(name = "mime_type", nullable = false, length = 100) private String mimeType;
    @Column(name = "file_size", nullable = false) private Long fileSize;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "created_by") private UserEntity createdBy;
    @CreatedDate @Column(nullable = false, updatable = false) private LocalDateTime createdAt;
    @LastModifiedDate private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
