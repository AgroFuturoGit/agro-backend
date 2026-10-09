package com.ufal.smartagro.adapters.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import com.ufal.smartagro.domain.model.enums.PlanStatus;

/** Persistência do plano, incluindo estado, produtividade congelada e localização do talhão. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "production_plans")
@EntityListeners(AuditingEntityListener.class)
@SQLRestriction("deleted_at IS NULL")
public class ProductionPlanEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farmer_id", nullable = false)
    private FarmerEntity farmer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "harvest_id", nullable = false)
    private HarvestEntity harvest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crop_id", nullable = false)
    private CropEntity crop;

    @Column(name = "planted_area", nullable = false, precision = 10, scale = 2)
    private BigDecimal plantedArea;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    /** Estado atual do planejamento. */
    private PlanStatus status;

    private LocalDate expectedHarvestStart;
    private LocalDate expectedHarvestEnd;
    @Column(precision = 14, scale = 4)
    /** Cópia da produtividade da cultura no momento em que o plano foi criado. */
    private BigDecimal expectedProductivity;
    /** Descrição livre e coordenadas do local de plantio. */
    private String locationDescription;
    @Column(precision = 9, scale = 6)
    private BigDecimal latitude;
    @Column(precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "planned_planting_date")
    private LocalDate plannedPlantingDate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "planned_calendar", columnDefinition = "jsonb")
    private Map<String, Object> plannedCalendar;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;
}
