package com.ufal.smartagro.domain.model;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.UUID;
import java.math.BigDecimal;
import com.ufal.smartagro.domain.model.enums.HarvestType;
import com.ufal.smartagro.domain.model.enums.MeasurementUnit;

/** Modelo de domínio da cultura; reúne ciclo, rendimento de referência e forma de colheita. */
@Getter
@AllArgsConstructor
public class Crop {
    private UUID id;
    private String name;
    private String variety;
    private Boolean isPriority = false;
    /** Ciclo estimado entre plantio e maturação, em dias. */
    private Integer cycleDays;
    /** Produtividade estimada por unidade de área, usada como padrão dos planos. */
    private BigDecimal expectedProductivity;
    /** Tipo de colheita, unidade informada e peso de referência dessa unidade. */
    private HarvestType harvestType;
    private MeasurementUnit unit;
    private BigDecimal unitWeightKg;

    public Crop(UUID id, String name, String variety, Boolean isPriority) {
        this(id, name, variety, isPriority, null, null, null, null, null);
    }
}
