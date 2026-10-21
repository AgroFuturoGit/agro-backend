package com.ufal.smartagro.adapters.out.persistence.mapper;

import com.ufal.smartagro.adapters.out.persistence.entity.FieldOccurrenceEntity;
import com.ufal.smartagro.adapters.out.persistence.entity.ProductionExecutionEntity;
import com.ufal.smartagro.domain.model.FieldOccurrence;
import com.ufal.smartagro.domain.model.ProductionExecution;
import com.ufal.smartagro.domain.model.enums.OccurrenceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FieldOccurrenceMapperTest {

    @Mock
    private ProductionExecutionMapper executionMapper;

    private FieldOccurrenceMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new FieldOccurrenceMapper(executionMapper);
    }

    @Test
    void deveConverterEntityParaDomain() {
        UUID id = UUID.randomUUID();
        ProductionExecutionEntity execEntity = new ProductionExecutionEntity();
        execEntity.setId(UUID.randomUUID());
        ProductionExecution execDomain = ProductionExecution.builder().id(execEntity.getId()).build();

        FieldOccurrenceEntity entity = new FieldOccurrenceEntity();
        entity.setId(id);
        entity.setExecution(execEntity);
        entity.setLogType(OccurrenceType.CLIMATE);
        entity.setDescription("Granizo danificou folhas");
        entity.setWeatherObservation("Tempestade súbita");
        entity.setCreatedAt(LocalDateTime.now());

        when(executionMapper.toDomain(execEntity)).thenReturn(execDomain);

        FieldOccurrence domain = mapper.toDomain(entity);

        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isEqualTo(id);
        assertThat(domain.getLogType()).isEqualTo(OccurrenceType.CLIMATE);
        assertThat(domain.getDescription()).isEqualTo("Granizo danificou folhas");
        assertThat(domain.getWeatherObservation()).isEqualTo("Tempestade súbita");
    }

    @Test
    void deveConverterDomainParaEntity() {
        UUID id = UUID.randomUUID();
        ProductionExecutionEntity execEntity = new ProductionExecutionEntity();
        execEntity.setId(UUID.randomUUID());
        ProductionExecution execDomain = ProductionExecution.builder().id(execEntity.getId()).build();

        FieldOccurrence domain = FieldOccurrence.builder()
                .id(id)
                .execution(execDomain)
                .logType(OccurrenceType.DISEASE)
                .description("Ferrugem asiática detectada")
                .weatherObservation("Alta umidade")
                .build();

        when(executionMapper.toEntity(execDomain)).thenReturn(execEntity);

        FieldOccurrenceEntity entity = mapper.toEntity(domain);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getLogType()).isEqualTo(OccurrenceType.DISEASE);
        assertThat(entity.getDescription()).isEqualTo("Ferrugem asiática detectada");
    }
}
