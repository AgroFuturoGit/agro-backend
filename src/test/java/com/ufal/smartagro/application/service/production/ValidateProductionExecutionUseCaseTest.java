package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.model.*;
import com.ufal.smartagro.domain.model.enums.ExecutionStatus;
import com.ufal.smartagro.domain.port.out.*;
import com.ufal.smartagro.testsupport.UserTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ValidateProductionExecutionUseCase — aprovação de apontamentos")
class ValidateProductionExecutionUseCaseTest {

    @Mock
    private ProductionExecutionRepository productionExecutionRepository;
    @Mock
    private ManagerRepository managerRepository;
    @Mock
    private TechnicianRepository technicianRepository;
    @Mock
    private TechnicalAssistanceRepository technicalAssistanceRepository;

    private ValidateProductionExecutionUseCase useCase;

    @BeforeEach
    void setUp() {
        ProductionAccessValidator validator = new ProductionAccessValidator(
                managerRepository, technicianRepository, technicalAssistanceRepository);
        useCase = new ValidateProductionExecutionUseCase(productionExecutionRepository, validator);
    }

    private ProductionExecution createPendingExecution(Farmer farmer) {
        ProductionPlan plan = UserTestFactory.productionPlan().farmer(farmer).build();
        return new ProductionExecution(
                UUID.randomUUID(),
                plan,
                new BigDecimal("10.00"),
                new BigDecimal("10.00"),
                LocalDate.of(2026, 5, 10),
                ExecutionStatus.PENDING,
                null,
                null,
                "Observação do agricultor",
                null, null, null, null,
                null, null, null
        );
    }

    @Test
    @DisplayName("FARMER não pode validar apontamentos")
    void givenFarmer_whenValidate_thenThrowsAccessDeniedException() {
        User farmerUser = UserTestFactory.user().farmer().build();
        UUID executionId = UUID.randomUUID();

        assertThatThrownBy(() -> useCase.validate(executionId, farmerUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("O agricultor não tem permissão para validar apontamentos.");

        verify(productionExecutionRepository, never()).save(any());
    }

    @Test
    @DisplayName("ADMIN pode validar apontamento com sucesso")
    void givenAdmin_whenValidate_thenSucceeds() {
        User admin = UserTestFactory.user().admin().build();
        Farmer farmer = UserTestFactory.farmerProfile().build();
        ProductionExecution execution = createPendingExecution(farmer);

        when(productionExecutionRepository.findById(execution.getId())).thenReturn(Optional.of(execution));
        when(productionExecutionRepository.save(any(ProductionExecution.class))).thenAnswer(i -> i.getArgument(0));

        ProductionExecution result = useCase.validate(execution.getId(), admin);

        assertThat(result.getStatus()).isEqualTo(ExecutionStatus.VALIDATED);
        assertThat(result.getValidatedBy()).isEqualTo(admin);
        assertThat(result.getValidatedAt()).isNotNull();

        ArgumentCaptor<ProductionExecution> captor = ArgumentCaptor.forClass(ProductionExecution.class);
        verify(productionExecutionRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(ExecutionStatus.VALIDATED);
    }

    @Test
    @DisplayName("TECHNICIAN com assistência ativa pode validar apontamento")
    void givenTechnicianWithAssistance_whenValidate_thenSucceeds() {
        User techUser = UserTestFactory.user().technician().build();
        Technician technician = UserTestFactory.technicianProfile().user(techUser).build();
        Community community = UserTestFactory.community().build();
        Farmer farmer = UserTestFactory.farmerProfile().community(community).build();
        ProductionExecution execution = createPendingExecution(farmer);

        when(productionExecutionRepository.findById(execution.getId())).thenReturn(Optional.of(execution));
        when(technicianRepository.findByUserId(techUser.getId())).thenReturn(Optional.of(technician));
        TechnicalAssistance assistance = UserTestFactory.technicalAssistance()
                .technician(technician).community(community).build();
        when(technicalAssistanceRepository.findActiveByTechnicianAndCommunity(technician.getId(), community.getId()))
                .thenReturn(Optional.of(assistance));
        when(productionExecutionRepository.save(any(ProductionExecution.class))).thenAnswer(i -> i.getArgument(0));

        ProductionExecution result = useCase.validate(execution.getId(), techUser);

        assertThat(result.getStatus()).isEqualTo(ExecutionStatus.VALIDATED);
        assertThat(result.getValidatedBy()).isEqualTo(techUser);
        assertThat(result.getValidatedAt()).isNotNull();
    }

    @Test
    @DisplayName("TECHNICIAN sem assistência ativa é barrado")
    void givenTechnicianWithoutAssistance_whenValidate_thenThrowsAccessDeniedException() {
        User techUser = UserTestFactory.user().technician().build();
        Technician technician = UserTestFactory.technicianProfile().user(techUser).build();
        Community community = UserTestFactory.community().build();
        Farmer farmer = UserTestFactory.farmerProfile().community(community).build();
        ProductionExecution execution = createPendingExecution(farmer);

        when(productionExecutionRepository.findById(execution.getId())).thenReturn(Optional.of(execution));
        when(technicianRepository.findByUserId(techUser.getId())).thenReturn(Optional.of(technician));
        when(technicalAssistanceRepository.findActiveByTechnicianAndCommunity(technician.getId(), community.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.validate(execution.getId(), techUser))
                .isInstanceOf(AccessDeniedException.class);

        verify(productionExecutionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Execução inexistente lança IllegalArgumentException")
    void givenNonExistentExecution_whenValidate_thenThrowsException() {
        User admin = UserTestFactory.user().admin().build();
        UUID id = UUID.randomUUID();
        when(productionExecutionRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.validate(id, admin))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Execução de produção não encontrada.");
    }
}
