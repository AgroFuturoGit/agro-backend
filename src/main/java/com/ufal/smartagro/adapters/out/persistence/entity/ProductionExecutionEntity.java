package com.ufal.smartagro.adapters.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "production_executions")
@EntityListeners(AuditingEntityListener.class)
@SQLRestriction("deleted_at IS NULL")
public class ProductionExecutionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_plan_id", nullable = false)
    private ProductionPlanEntity productionPlan;

    @Column(name = "actual_yield", nullable = false, precision = 10, scale = 2)
    private BigDecimal actualYield;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;
}
