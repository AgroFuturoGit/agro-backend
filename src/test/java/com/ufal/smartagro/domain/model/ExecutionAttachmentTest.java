package com.ufal.smartagro.domain.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ExecutionAttachmentTest {
    @Test void preservesFileMetadataAndNullableCreator() {
        ExecutionAttachment value = ExecutionAttachment.builder().fileUrl("https://files/a.jpg").mimeType("image/jpeg").fileSize(42L).build();
        assertThat(value.getFileSize()).isEqualTo(42L); assertThat(value.getCreatedBy()).isNull();
    }
}
