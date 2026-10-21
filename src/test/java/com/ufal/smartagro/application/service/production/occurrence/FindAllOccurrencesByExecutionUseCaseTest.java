package com.ufal.smartagro.application.service.production.occurrence;

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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FindAllOccurrencesByExecutionUseCaseTest {

    @Mock
    private FieldOccurrenceRepository fieldOccurrenceRepository;

    @Mock
    private ProductionExecutionRepository productionExecutionRepository;

    @Mock
    private ProductionAccessValidator accessValidator;

    private FindAllOccurrencesByExecutionUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FindAllOccurrencesByExecutionUseCase(fieldOccurrenceRepository, productionExecutionRepository, accessValidator);
    }

    @Test
    @DisplayName("Lista ocorrências da execução com sucesso")
    void givenExecution_whenFindAll_thenReturnsList() {
        User admin = UserTestFactory.user().admin().build();
        Farmer farmer = UserTestFactory.farmerProfile().build();
        ProductionPlan plan = UserTestFactory.productionPlan().farmer(farmer).build();
        ProductionExecution execution = ProductionExecution.builder().id(UUID.randomUUID()).productionPlan(plan).build();

        FieldOccurrence o1 = FieldOccurrence.builder().logType(OccurrenceType.CLIMATE).description("Granizo").build();
        FieldOccurrence o2 = FieldOccurrence.builder().logType(OccurrenceType.PEST).description("Lagarta").build();

        when(productionExecutionRepository.findById(execution.getId())).thenReturn(Optional.of(execution));
        when(fieldOccurrenceRepository.findAllByExecutionId(execution.getId())).thenReturn(List.of(o1, o2));

        List<FieldOccurrence> result = useCase.findAll(execution.getId(), admin);

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(o1, o2);
        verify(accessValidator).validateAccess(farmer, admin);
    }
}
