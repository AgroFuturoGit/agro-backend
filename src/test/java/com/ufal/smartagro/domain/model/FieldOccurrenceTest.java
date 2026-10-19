package com.ufal.smartagro.domain.model;

import com.ufal.smartagro.domain.model.enums.OccurrenceType;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class FieldOccurrenceTest {
    @Test void preservesOccurrenceTypeAndNullableEvidence() {
        FieldOccurrence value = FieldOccurrence.builder().logType(OccurrenceType.DISEASE).description("Ferrugem").build();
        assertThat(value.getLogType()).isEqualTo(OccurrenceType.DISEASE); assertThat(value.getEvidenceUrl()).isNull();
    }
}
