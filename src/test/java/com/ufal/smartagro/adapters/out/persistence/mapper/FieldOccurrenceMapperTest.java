package com.ufal.smartagro.adapters.out.persistence.mapper;

import com.ufal.smartagro.adapters.out.persistence.entity.FieldOccurrenceEntity;
import com.ufal.smartagro.domain.model.FieldOccurrence;
import com.ufal.smartagro.domain.model.enums.OccurrenceType;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class FieldOccurrenceMapperTest {
    private final FieldOccurrenceMapper mapper = new FieldOccurrenceMapper(mock(ProductionExecutionMapper.class));
    @Test void mapsOccurrenceEnumAndOptionalFields() {
        FieldOccurrence domain = FieldOccurrence.builder().id(UUID.randomUUID()).logType(OccurrenceType.PEST).description("Pulgões").evidenceUrl("url").build();
        FieldOccurrenceEntity entity = mapper.toEntity(domain);
        assertThat(entity.getLogType()).isEqualTo(OccurrenceType.PEST); assertThat(mapper.toDomain(entity).getEvidenceUrl()).isEqualTo("url"); assertThat(mapper.toEntity(null)).isNull();
    }
}
