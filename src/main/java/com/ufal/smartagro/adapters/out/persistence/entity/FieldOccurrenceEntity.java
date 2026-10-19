package com.ufal.smartagro.adapters.out.persistence.entity;

import com.ufal.smartagro.domain.model.enums.OccurrenceType;
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
@Table(name = "field_occurrences")
@EntityListeners(AuditingEntityListener.class)
@SQLRestriction("deleted_at IS NULL")
public class FieldOccurrenceEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "execution_id", nullable = false) private ProductionExecutionEntity execution;
    @Enumerated(EnumType.STRING) @Column(name = "log_type", nullable = false, length = 30) private OccurrenceType logType;
    @Column(nullable = false, columnDefinition = "text") private String description;
    @Column(name = "weather_observation", length = 255) private String weatherObservation;
    @Column(name = "evidence_url", length = 500) private String evidenceUrl;
    @CreatedDate @Column(nullable = false, updatable = false) private LocalDateTime createdAt;
    @LastModifiedDate private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
