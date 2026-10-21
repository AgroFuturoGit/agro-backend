package com.ufal.smartagro.application.service.production.occurrence;

import com.ufal.smartagro.application.service.production.ProductionAccessValidator;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.FieldOccurrence;
import com.ufal.smartagro.domain.model.ProductionExecution;
import com.ufal.smartagro.domain.model.ProductionPlan;
import com.ufal.smartagro.domain.model.User;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteFieldOccurrenceUseCaseTest {

    @Mock
    private FieldOccurrenceRepository fieldOccurrenceRepository;

    @Mock
    private ProductionAccessValidator accessValidator;

    private DeleteFieldOccurrenceUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteFieldOccurrenceUseCase(fieldOccurrenceRepository, accessValidator);
    }

    @Test
    @DisplayName("Exclui ocorrência com sucesso")
    void givenExistingId_whenDelete_thenDeletes() {
        User admin = UserTestFactory.user().admin().build();
        Farmer farmer = UserTestFactory.farmerProfile().build();
        ProductionPlan plan = UserTestFactory.productionPlan().farmer(farmer).build();
        ProductionExecution execution = ProductionExecution.builder().id(UUID.randomUUID()).productionPlan(plan).build();

        UUID id = UUID.randomUUID();
        FieldOccurrence existing = FieldOccurrence.builder()
                .id(id)
                .execution(execution)
                .description("Geada")
                .build();

        when(fieldOccurrenceRepository.findById(id)).thenReturn(Optional.of(existing));

        useCase.delete(id, admin);

        verify(accessValidator).validateWriteAccess(farmer, admin);
        verify(fieldOccurrenceRepository).delete(id);
    }

    @Test
    @DisplayName("Lança IllegalArgumentException quando ocorrência não existe")
    void givenNonExistingId_whenDelete_thenThrowsIllegalArgumentException() {
        User admin = UserTestFactory.user().admin().build();
        UUID id = UUID.randomUUID();

        when(fieldOccurrenceRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.delete(id, admin))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Ocorrência de campo não encontrada.");

        verify(fieldOccurrenceRepository, never()).delete(any());
    }
}
