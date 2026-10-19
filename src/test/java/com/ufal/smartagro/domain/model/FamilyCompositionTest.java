package com.ufal.smartagro.domain.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

class FamilyCompositionTest {
    @Test void preservesValidFieldsIncludingAgeZero() {
        UUID id = UUID.randomUUID(); LocalDateTime createdAt = LocalDateTime.now();
        FamilyComposition value = FamilyComposition.builder().id(id).name("Ana").kinship("Filha").age(0).createdAt(createdAt).build();
        assertThat(value.getId()).isEqualTo(id); assertThat(value.getAge()).isZero(); assertThat(value.getCreatedAt()).isEqualTo(createdAt);
    }
}
