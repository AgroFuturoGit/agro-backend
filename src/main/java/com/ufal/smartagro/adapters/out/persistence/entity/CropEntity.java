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
import java.math.BigDecimal;
import com.ufal.smartagro.domain.model.enums.HarvestType;
import com.ufal.smartagro.domain.model.enums.MeasurementUnit;

/** Entidade JPA da cultura e seus parâmetros produtivos persistidos na tabela crop. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "crop",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_crop_name_variety", columnNames = {"name", "variety"})
        })
@EntityListeners(AuditingEntityListener.class)
@SQLRestriction("deleted_at IS NULL")
public class CropEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String variety;

    @Column(nullable = false)
    private Boolean isPriority = false;

    /** Duração estimada do ciclo da cultura, em dias. */
    private Integer cycleDays;
    /** Produtividade de referência usada como padrão ao criar planos. */
    @Column(precision = 14, scale = 4)
    private BigDecimal expectedProductivity;
    /** Tipo de colheita e unidade comercial, persistidos pelo nome do enum. */
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private HarvestType harvestType;
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private MeasurementUnit unit;
    /** Conversão de uma unidade da cultura para quilogramas. */
    @Column(precision = 12, scale = 4)
    private BigDecimal unitWeightKg;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;
}
