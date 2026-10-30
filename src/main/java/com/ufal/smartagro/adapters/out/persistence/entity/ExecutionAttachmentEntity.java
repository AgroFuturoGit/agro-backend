package com.ufal.smartagro.adapters.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "execution_attachments")
@EntityListeners(AuditingEntityListener.class)
@SQLRestriction("deleted_at IS NULL")
public class ExecutionAttachmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_execution_id", nullable = false)
    private ProductionExecutionEntity productionExecution;

    @Column(name = "client_id")
    private UUID clientId;

    @Column(nullable = false, length = 255)
    private String filename;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    /*
     * O binário fica na mesma linha dos metadados, mas nunca é lido junto com
     * eles: a listagem usa a projeção `ExecutionAttachmentView`, que seleciona
     * só as colunas de metadado. Marcar este campo como LAZY não bastaria —
     * atributo básico só carrega sob demanda com enhancement de bytecode
     * ligado, e sem isso o Hibernate traria a foto inteira em toda consulta.
     */
    @Column(nullable = false)
    private byte[] content;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
