package com.ufal.smartagro.adapters.out.persistence.mapper;

import com.ufal.smartagro.adapters.out.persistence.entity.ProductionExecutionEntity;
import com.ufal.smartagro.domain.model.ProductionExecution;
import com.ufal.smartagro.domain.model.enums.ExecutionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ProductionExecutionMapperTest {

    @Mock
    private ProductionPlanMapper productionPlanMapper;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private ProductionExecutionMapper mapper;

    @Test
    @DisplayName("Deve mapear rejectionReason para domínio")
    void toDomain_mapeiaRejectionReason() {
        ProductionExecutionEntity entity = new ProductionExecutionEntity();
        entity.setId(UUID.randomUUID());
        entity.setQuantity(new BigDecimal("15.5"));
        entity.setQuantityKg(new BigDecimal("155.0"));
        entity.setHarvestedAt(LocalDate.now());
        entity.setStatus(ExecutionStatus.REJECTED);
        entity.setNotes("Anotação inicial");
        entity.setRejectionReason("Fora do padrão");

        ProductionExecution domain = mapper.toDomain(entity);

        assertThat(domain).isNotNull();
        assertThat(domain.getRejectionReason()).isEqualTo("Fora do padrão");
        assertThat(domain.getNotes()).isEqualTo("Anotação inicial");
        assertThat(domain.getStatus()).isEqualTo(ExecutionStatus.REJECTED);
    }

    @Test
    @DisplayName("Deve mapear rejectionReason para entidade")
    void toEntity_mapeiaRejectionReason() {
        ProductionExecution domain = new ProductionExecution(
                UUID.randomUUID(), null, new BigDecimal("10.0"), new BigDecimal("100.0"),
                LocalDate.now(), ExecutionStatus.REJECTED, null, LocalDateTime.now(),
                "Anotação do produtor", "Foto borrada", null, null, null, null,
                LocalDateTime.now(), null, null
        );

        ProductionExecutionEntity entity = mapper.toEntity(domain);

        assertThat(entity).isNotNull();
        assertThat(entity.getRejectionReason()).isEqualTo("Foto borrada");
        assertThat(entity.getNotes()).isEqualTo("Anotação do produtor");
        assertThat(entity.getStatus()).isEqualTo(ExecutionStatus.REJECTED);
    }

    @Test
    @DisplayName("Deve retornar nulo para entradas nulas")
    void nullInputs_retornamNull() {
        assertThat(mapper.toDomain(null)).isNull();
        assertThat(mapper.toEntity(null)).isNull();
    }
}
