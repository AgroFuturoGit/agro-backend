package com.ufal.smartagro.domain.model;

import com.ufal.smartagro.domain.model.enums.HarvestType;
import com.ufal.smartagro.domain.model.enums.MeasurementUnit;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CropTest {
    @Test void deveManterCamposAgricolasInformados() {
        Crop crop = new Crop(UUID.randomUUID(), "Milho", "AG1051", true, 120,
                new BigDecimal("1200"), HarvestType.CONTINUOUS, MeasurementUnit.SACK, new BigDecimal("60"));
        assertThat(crop.getCycleDays()).isEqualTo(120);
        assertThat(crop.getExpectedProductivity()).isEqualByComparingTo("1200");
        assertThat(crop.getHarvestType()).isEqualTo(HarvestType.CONTINUOUS);
        assertThat(crop.getUnit()).isEqualTo(MeasurementUnit.SACK);
        assertThat(crop.getUnitWeightKg()).isEqualByComparingTo("60");
    }

    @Test void devePermitirCamposAgricolasOpcionaisNulos() {
        Crop legacy = new Crop(UUID.randomUUID(), "Milho", "Comum", false);
        Crop optionalNull = new Crop(UUID.randomUUID(), "Feijão", "Carioca", false, null, null, null, null, null);
        assertThat(legacy.getExpectedProductivity()).isNull();
        assertThat(optionalNull.getHarvestType()).isNull();
    }

    @Test void devePermitirCulturasDeColheitaUnicaEContinua() {
        for (HarvestType type : HarvestType.values()) {
            Crop crop = new Crop(UUID.randomUUID(), "Cultura", "Variedade", false, 90, BigDecimal.ONE, type, MeasurementUnit.KG, BigDecimal.ONE);
            assertThat(crop.getHarvestType()).isEqualTo(type);
        }
    }

    @Test void devePermitirCadaUnidadeDeMedidaSuportada() {
        for (MeasurementUnit unit : MeasurementUnit.values()) {
            Crop crop = new Crop(UUID.randomUUID(), "Cultura", "Variedade", false, 90, BigDecimal.ONE, HarvestType.SINGLE, unit, BigDecimal.ONE);
            assertThat(crop.getUnit()).isEqualTo(unit);
        }
    }

}
