package com.ufal.smartagro.adapters.out.persistence.mapper;

import com.ufal.smartagro.adapters.out.persistence.entity.ExecutionAttachmentEntity;
import com.ufal.smartagro.domain.model.ExecutionAttachment;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ExecutionAttachmentMapperTest {
    private final ExecutionAttachmentMapper mapper = new ExecutionAttachmentMapper(mock(ProductionExecutionMapper.class), mock(UserMapper.class));
    @Test void mapsFileMetadataAndNullCreator() {
        ExecutionAttachment domain = ExecutionAttachment.builder().id(UUID.randomUUID()).fileUrl("url").mimeType("image/png").fileSize(1024L).build();
        ExecutionAttachmentEntity entity = mapper.toEntity(domain);
        assertThat(entity.getFileSize()).isEqualTo(1024L); assertThat(mapper.toDomain(entity).getMimeType()).isEqualTo("image/png"); assertThat(mapper.toDomain(null)).isNull();
    }
}
