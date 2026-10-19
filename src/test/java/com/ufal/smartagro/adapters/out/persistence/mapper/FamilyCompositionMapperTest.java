package com.ufal.smartagro.adapters.out.persistence.mapper;

import com.ufal.smartagro.adapters.out.persistence.entity.FamilyCompositionEntity;
import com.ufal.smartagro.domain.model.FamilyComposition;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class FamilyCompositionMapperTest {
    private final FamilyCompositionMapper mapper = new FamilyCompositionMapper(mock(FarmerMapper.class));
    @Test void mapsBothDirectionsAndPreservesAuditFields() {
        UUID id = UUID.randomUUID(); LocalDateTime time = LocalDateTime.now();
        FamilyComposition domain = FamilyComposition.builder().id(id).name("Ana").kinship("Filha").age(8).createdAt(time).build();
        FamilyCompositionEntity entity = mapper.toEntity(domain);
        assertThat(entity.getId()).isEqualTo(id); assertThat(entity.getAge()).isEqualTo(8); assertThat(entity.getCreatedAt()).isEqualTo(time);
        assertThat(mapper.toDomain(entity).getKinship()).isEqualTo("Filha"); assertThat(mapper.toEntity(null)).isNull();
    }
}
