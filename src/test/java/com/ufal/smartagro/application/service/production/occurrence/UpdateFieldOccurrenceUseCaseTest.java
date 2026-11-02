package com.ufal.smartagro.application.service.production.occurrence;

import com.ufal.smartagro.adapters.in.web.dto.production.occurrence.FieldOccurrenceUpdateDTO;
import com.ufal.smartagro.application.service.production.ProductionAccessValidator;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.FieldOccurrence;
import com.ufal.smartagro.domain.model.ProductionExecution;
import com.ufal.smartagro.domain.model.ProductionPlan;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.OccurrenceType;
import com.ufal.smartagro.domain.port.out.FieldOccurrenceRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateFieldOccurrenceUseCaseTest {

    @Mock
    private FieldOccurrenceRepository fieldOccurrenceRepository;

    @Mock
    private ProductionAccessValidator accessValidator;

    private UpdateFieldOccurrenceUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateFieldOccurrenceUseCase(fieldOccurrenceRepository, accessValidator);
    }

    @Test
    @DisplayName("Atualiza ocorrência com sucesso")
    void givenValidUpdate_whenUpdate_thenSucceeds() {
        User admin = UserTestFactory.user().admin().build();
        Farmer farmer = UserTestFactory.farmerProfile().build();
        ProductionPlan plan = UserTestFactory.productionPlan().farmer(farmer).build();
        ProductionExecution execution = ProductionExecution.builder().id(UUID.randomUUID()).productionPlan(plan).build();

        UUID id = UUID.randomUUID();
        FieldOccurrence existing = FieldOccurrence.builder()
                .id(id)
                .execution(execution)
                .logType(OccurrenceType.PEST)
                .description("Lagarta")
                .build();

        FieldOccurrenceUpdateDTO updateDTO = new FieldOccurrenceUpdateDTO(
                OccurrenceType.PEST, "Lagarta sob controle após aplicação", "Tempo ameno", "https://evidence.test/1.jpg"
        );

        when(fieldOccurrenceRepository.findById(id)).thenReturn(Optional.of(existing));
        when(fieldOccurrenceRepository.save(any(FieldOccurrence.class))).thenAnswer(i -> i.getArgument(0));

        FieldOccurrence result = useCase.update(id, updateDTO, admin);

        assertThat(result.getDescription()).isEqualTo("Lagarta sob controle após aplicação");
        assertThat(result.getWeatherObservation()).isEqualTo("Tempo ameno");
        assertThat(result.getEvidenceUrl()).isEqualTo("https://evidence.test/1.jpg");

        verify(accessValidator).validateWriteAccess(farmer, admin);
        verify(fieldOccurrenceRepository).save(any(FieldOccurrence.class));
    }

    @Test
    @DisplayName("Quando apontamento já validado e usuário é FARMER, lança AccessDeniedException ao atualizar ocorrência")
    void givenValidatedExecutionAndFarmer_thenThrowsAccessDeniedException() {
        UUID occurrenceId = UUID.randomUUID();
        User farmerUser = UserTestFactory.user().farmer().build();
        Farmer farmer = UserTestFactory.farmerProfile().user(farmerUser).build();
        ProductionPlan plan = org.mockito.Mockito.mock(ProductionPlan.class);
        org.mockito.Mockito.when(plan.getFarmer()).thenReturn(farmer);

        ProductionExecution execution = ProductionExecution.builder()
                .id(UUID.randomUUID())
                .productionPlan(plan)
                .status(com.ufal.smartagro.domain.model.enums.ExecutionStatus.VALIDATED)
                .build();

        FieldOccurrence occurrence = FieldOccurrence.builder()
                .id(occurrenceId)
                .execution(execution)
                .description("Descrição antiga")
                .build();

        org.mockito.Mockito.when(fieldOccurrenceRepository.findById(occurrenceId)).thenReturn(java.util.Optional.of(occurrence));

        FieldOccurrenceUpdateDTO dto = new FieldOccurrenceUpdateDTO(null, "Nova descrição", null, null);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> useCase.update(occurrenceId, dto, farmerUser))
                .isInstanceOf(com.ufal.smartagro.domain.exception.AccessDeniedException.class)
                .hasMessageContaining("já validado");
    }
}
