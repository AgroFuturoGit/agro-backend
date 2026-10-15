package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.domain.model.Crop;
import com.ufal.smartagro.domain.model.enums.MeasurementUnit;

import java.math.BigDecimal;

/**
 * Utilitário para cálculo e normalização de peso em quilogramas (quantityKg)
 * a partir da quantidade e da unidade de medida da cultura.
 */
public final class ProductionQuantityCalculator {

    private ProductionQuantityCalculator() {}

    public static BigDecimal calculateQuantityKg(Crop crop, BigDecimal quantity, BigDecimal fallbackKg) {
        if (quantity == null) {
            return fallbackKg;
        }

        if (crop == null || crop.getUnit() == null) {
            return fallbackKg != null ? fallbackKg : quantity;
        }

        MeasurementUnit unit = crop.getUnit();
        return switch (unit) {
            case KG -> quantity;
            case TON -> quantity.multiply(BigDecimal.valueOf(1000));
            case SACK, BOX -> {
                if (crop.getUnitWeightKg() != null && crop.getUnitWeightKg().compareTo(BigDecimal.ZERO) > 0) {
                    yield quantity.multiply(crop.getUnitWeightKg());
                }
                yield fallbackKg != null ? fallbackKg : quantity;
            }
        };
    }
}
