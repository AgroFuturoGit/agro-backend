package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.domain.model.Crop;
import com.ufal.smartagro.domain.model.enums.HarvestType;
import com.ufal.smartagro.domain.model.enums.MeasurementUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProductionQuantityCalculator — cálculo e normalização de quantityKg")
class ProductionQuantityCalculatorTest {

    private Crop cropWith(MeasurementUnit unit, BigDecimal unitWeightKg) {
        return new Crop(
                UUID.randomUUID(),
                "Tomate",
                "Italiano",
                false,
                90,
                new BigDecimal("30000.00"),
                HarvestType.CONTINUOUS,
                unit,
                unitWeightKg
        );
    }

    @Test
    @DisplayName("Quando unidade é KG, quantityKg é igual à quantity")
    void givenUnitKg_whenCalculate_thenReturnsSameQuantity() {
        Crop crop = cropWith(MeasurementUnit.KG, BigDecimal.ONE);
        BigDecimal result = ProductionQuantityCalculator.calculateQuantityKg(crop, new BigDecimal("25.50"), null);

        assertThat(result).isEqualByComparingTo(new BigDecimal("25.50"));
    }

    @Test
    @DisplayName("Quando unidade é TON, quantityKg é quantity multiplicada por 1000")
    void givenUnitTon_whenCalculate_thenMultipliesBy1000() {
        Crop crop = cropWith(MeasurementUnit.TON, new BigDecimal("1000"));
        BigDecimal result = ProductionQuantityCalculator.calculateQuantityKg(crop, new BigDecimal("2.5"), null);

        assertThat(result).isEqualByComparingTo(new BigDecimal("2500.0"));
    }

    @Test
    @DisplayName("Quando unidade é BOX com unitWeightKg = 15, quantityKg é 7 * 15 = 105")
    void givenUnitBoxWithUnitWeight_whenCalculate_thenMultipliesCorrectly() {
        Crop crop = cropWith(MeasurementUnit.BOX, new BigDecimal("15.00"));
        BigDecimal result = ProductionQuantityCalculator.calculateQuantityKg(crop, new BigDecimal("7"), null);

        assertThat(result).isEqualByComparingTo(new BigDecimal("105.00"));
    }

    @Test
    @DisplayName("Quando unidade é SACK com unitWeightKg = 60, quantityKg é 10 * 60 = 600")
    void givenUnitSackWithUnitWeight_whenCalculate_thenMultipliesCorrectly() {
        Crop crop = cropWith(MeasurementUnit.SACK, new BigDecimal("60.00"));
        BigDecimal result = ProductionQuantityCalculator.calculateQuantityKg(crop, new BigDecimal("10"), null);

        assertThat(result).isEqualByComparingTo(new BigDecimal("600.00"));
    }

    @Test
    @DisplayName("Quando cultura não tem unitWeightKg, utiliza fallbackKg se informado")
    void givenBoxWithoutUnitWeight_whenCalculate_thenUsesFallbackKg() {
        Crop crop = cropWith(MeasurementUnit.BOX, null);
        BigDecimal result = ProductionQuantityCalculator.calculateQuantityKg(crop, new BigDecimal("7"), new BigDecimal("105"));

        assertThat(result).isEqualByComparingTo(new BigDecimal("105"));
    }

    @Test
    @DisplayName("Quando crop é nulo, retorna fallbackKg ou quantity")
    void givenNullCrop_whenCalculate_thenReturnsFallbackOrQuantity() {
        BigDecimal withFallback = ProductionQuantityCalculator.calculateQuantityKg(null, new BigDecimal("5"), new BigDecimal("50"));
        assertThat(withFallback).isEqualByComparingTo(new BigDecimal("50"));

        BigDecimal withoutFallback = ProductionQuantityCalculator.calculateQuantityKg(null, new BigDecimal("5"), null);
        assertThat(withoutFallback).isEqualByComparingTo(new BigDecimal("5"));
    }
}
