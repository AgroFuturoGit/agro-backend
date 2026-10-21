package com.ufal.smartagro.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FamilyCompositionTest {

    @Test
    void deveCriarFamilyCompositionCorretamente() {
        UUID id = UUID.randomUUID();
        Farmer farmer = Farmer.builder().id(UUID.randomUUID()).build();
        LocalDateTime now = LocalDateTime.now();

        FamilyComposition composition = FamilyComposition.builder()
                .id(id)
                .farmer(farmer)
                .name("Maria Silva")
                .kinship("Esposa")
                .age(42)
                .createdAt(now)
                .build();

        assertThat(composition.getId()).isEqualTo(id);
        assertThat(composition.getFarmer()).isEqualTo(farmer);
        assertThat(composition.getName()).isEqualTo("Maria Silva");
        assertThat(composition.getKinship()).isEqualTo("Esposa");
        assertThat(composition.getAge()).isEqualTo(42);
        assertThat(composition.getCreatedAt()).isEqualTo(now);
    }
}
