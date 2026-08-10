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

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "technical_assistances")
@EntityListeners(AuditingEntityListener.class)
@SQLRestriction("deleted_at IS NULL")
public class TechnicalAssistanceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id", nullable = false)
    private TechnicianEntity technician;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producer_id", nullable = false)
    private ProducerEntity producer;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;
}
