package com.ufal.smartagro.application.service.farmer.financing;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.ufal.smartagro.application.service.audit.AuditLogService;
import com.ufal.smartagro.application.service.production.ProductionAccessValidator;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.Financing;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.AuditAction;
import com.ufal.smartagro.domain.port.out.FinancingRepository;
import com.ufal.smartagro.testsupport.UserTestFactory;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteFinancingUseCaseTest {

    @Mock
    private FinancingRepository financingRepository;

    @Mock
    private ProductionAccessValidator accessValidator;

    @Mock
    private AuditLogService auditLogService;

    private DeleteFinancingUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteFinancingUseCase(financingRepository, accessValidator, auditLogService);
    }

    @Test
    @DisplayName("Exclui financiamento com sucesso e grava auditoria")
    void givenExistingId_whenDelete_thenDeletes() {
        User admin = UserTestFactory.user().admin().build();
        Farmer farmer = UserTestFactory.farmerProfile().build();
        UUID id = UUID.randomUUID();
        Financing existing = Financing.builder()
                .id(id)
                .farmer(farmer)
                .description("PRONAF")
                .build();

        when(financingRepository.findById(id)).thenReturn(Optional.of(existing));

        useCase.delete(id, admin);

        verify(accessValidator).validateWriteAccess(farmer, admin);
        verify(financingRepository).delete(id);
        verify(auditLogService).record(eq(admin), eq(AuditAction.DELETE), eq("financings"), eq(id), any());
    }

    @Test
    @DisplayName("Lança AccessDeniedException quando MANAGER tenta excluir")
    void givenManager_whenDelete_thenThrowsAccessDeniedException() {
        User manager = UserTestFactory.user().manager().build();
        UUID id = UUID.randomUUID();

        assertThatThrownBy(() -> useCase.delete(id, manager))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Apenas administradores, técnicos assistentes ou o próprio agricultor podem excluir financiamentos.");

        verifyNoInteractions(financingRepository, accessValidator, auditLogService);
    }

    @Test
    @DisplayName("Lança EntityNotFoundException quando financiamento não existe")
    void givenNonExistingId_whenDelete_thenThrowsEntityNotFoundException() {
        User admin = UserTestFactory.user().admin().build();
        UUID id = UUID.randomUUID();

        when(financingRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.delete(id, admin))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Financiamento não encontrado.");

        verify(financingRepository, never()).delete(any());
        verifyNoInteractions(auditLogService);
    }
}
