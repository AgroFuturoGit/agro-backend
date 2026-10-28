package com.ufal.smartagro.application.service.production.occurrence;

import com.ufal.smartagro.adapters.in.web.dto.production.occurrence.FieldOccurrenceRegisterDTO;
import com.ufal.smartagro.application.service.production.ProductionAccessValidator;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.FieldOccurrence;
import com.ufal.smartagro.domain.model.ProductionExecution;
import com.ufal.smartagro.domain.model.ProductionPlan;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.OccurrenceType;
import com.ufal.smartagro.domain.port.out.FieldOccurrenceRepository;
import com.ufal.smartagro.domain.port.out.ProductionExecutionRepository;
import com.ufal.smartagro.testsupport.UserTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterFieldOccurrenceUseCaseTest {

    @Mock
    private FieldOccurrenceRepository fieldOccurrenceRepository;

    @Mock
    private ProductionExecutionRepository productionExecutionRepository;

    @Mock
    private ProductionAccessValidator accessValidator;

    private RegisterFieldOccurrenceUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RegisterFieldOccurrenceUseCase(fieldOccurrenceRepository, productionExecutionRepository, accessValidator);
    }

    @Test
    @DisplayName("Registra ocorrência com sucesso")
    void givenValidData_whenRegister_thenSucceeds() {
        User admin = UserTestFactory.user().admin().build();
        Farmer farmer = UserTestFactory.farmerProfile().build();
        ProductionPlan plan = UserTestFactory.productionPlan().farmer(farmer).build();
        ProductionExecution execution = ProductionExecution.builder().id(UUID.randomUUID()).productionPlan(plan).build();

        FieldOccurrenceRegisterDTO dto = new FieldOccurrenceRegisterDTO(
                OccurrenceType.PEST, "Lagarta detectada", "Tempo seco", null
        );

        when(productionExecutionRepository.findById(execution.getId())).thenReturn(Optional.of(execution));
        when(fieldOccurrenceRepository.save(any(FieldOccurrence.class))).thenAnswer(i -> i.getArgument(0));

        FieldOccurrence result = useCase.register(execution.getId(), dto, admin);

        assertThat(result).isNotNull();
        assertThat(result.getLogType()).isEqualTo(OccurrenceType.PEST);
        assertThat(result.getDescription()).isEqualTo("Lagarta detectada");
        assertThat(result.getWeatherObservation()).isEqualTo("Tempo seco");
        assertThat(result.getExecution()).isEqualTo(execution);

        verify(accessValidator).validateWriteAccess(farmer, admin);
        verify(fieldOccurrenceRepository).save(any(FieldOccurrence.class));
    }

    @Test
    @DisplayName("Lança IllegalArgumentException quando execução não existe")
    void givenNonExistingExecution_whenRegister_thenThrowsIllegalArgumentException() {
        User admin = UserTestFactory.user().admin().build();
        UUID executionId = UUID.randomUUID();
        FieldOccurrenceRegisterDTO dto = new FieldOccurrenceRegisterDTO(
                OccurrenceType.CLIMATE, "Chuva forte", null, null
        );

        when(productionExecutionRepository.findById(executionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.register(executionId, dto, admin))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Execução de produção não encontrada.");

        verify(fieldOccurrenceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Quando clientId já existe para o apontamento, retorna ocorrência existente sem salvar novamente (idempotência)")
    void givenExistingClientId_whenRegister_thenReturnsExistingOccurrenceWithoutSaving() {
        User admin = UserTestFactory.user().admin().build();
        Farmer farmer = UserTestFactory.farmerProfile().build();
        ProductionPlan plan = UserTestFactory.productionPlan().farmer(farmer).build();
        UUID executionId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        ProductionExecution execution = ProductionExecution.builder().id(executionId).productionPlan(plan).build();

        FieldOccurrence existingOccurrence = FieldOccurrence.builder()
                .id(UUID.randomUUID())
                .clientId(clientId)
                .execution(execution)
                .logType(OccurrenceType.PEST)
                .description("Lagarta detectada anteriormente")
                .build();

        when(productionExecutionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(fieldOccurrenceRepository.findByExecutionIdAndClientId(executionId, clientId))
                .thenReturn(Optional.of(existingOccurrence));

        FieldOccurrenceRegisterDTO dto = new FieldOccurrenceRegisterDTO(
                clientId, OccurrenceType.PEST, "Lagarta detectada", "Tempo seco", null
        );

        FieldOccurrence result = useCase.register(executionId, dto, admin);

        assertThat(result).isSameAs(existingOccurrence);
        verify(accessValidator).validateWriteAccess(farmer, admin);
        verify(fieldOccurrenceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Quando clientId é novo, persiste ocorrência vinculando o clientId")
    void givenNewClientId_whenRegister_thenSavesOccurrenceWithClientId() {
        User admin = UserTestFactory.user().admin().build();
        Farmer farmer = UserTestFactory.farmerProfile().build();
        ProductionPlan plan = UserTestFactory.productionPlan().farmer(farmer).build();
        UUID executionId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        ProductionExecution execution = ProductionExecution.builder().id(executionId).productionPlan(plan).build();

        when(productionExecutionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(fieldOccurrenceRepository.findByExecutionIdAndClientId(executionId, clientId))
                .thenReturn(Optional.empty());
        when(fieldOccurrenceRepository.save(any(FieldOccurrence.class))).thenAnswer(i -> i.getArgument(0));

        FieldOccurrenceRegisterDTO dto = new FieldOccurrenceRegisterDTO(
                clientId, OccurrenceType.PEST, "Lagarta detectada", "Tempo seco", null
        );

        FieldOccurrence result = useCase.register(executionId, dto, admin);

        assertThat(result).isNotNull();
        assertThat(result.getClientId()).isEqualTo(clientId);
        verify(fieldOccurrenceRepository).save(any(FieldOccurrence.class));
    }
}
