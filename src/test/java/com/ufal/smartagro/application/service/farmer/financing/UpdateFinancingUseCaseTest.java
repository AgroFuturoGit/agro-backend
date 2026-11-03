package com.ufal.smartagro.application.service.farmer.financing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.ufal.smartagro.adapters.in.web.dto.farmer.financing.FinancingUpdateDTO;
import com.ufal.smartagro.application.service.audit.AuditLogService;
import com.ufal.smartagro.application.service.production.ProductionAccessValidator;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.Financing;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.AuditAction;
import com.ufal.smartagro.domain.model.enums.FinancingStatus;
import com.ufal.smartagro.domain.port.out.FinancingRepository;
import com.ufal.smartagro.testsupport.UserTestFactory;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateFinancingUseCaseTest {

    @Mock
    private FinancingRepository financingRepository;

    @Mock
    private ProductionAccessValidator accessValidator;

    @Mock
    private AuditLogService auditLogService;

    private UpdateFinancingUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateFinancingUseCase(financingRepository, accessValidator, auditLogService);
    }

    @Test
    @DisplayName("Atualiza financiamento com sucesso e grava auditoria")
    void givenValidUpdate_whenUpdate_thenSucceeds() {
        User admin = UserTestFactory.user().admin().build();
        Farmer farmer = UserTestFactory.farmerProfile().build();
        UUID id = UUID.randomUUID();
        Financing existing = Financing.builder()
                .id(id)
                .farmer(farmer)
                .description("PRONAF")
                .status(FinancingStatus.PENDING)
                .amount(new BigDecimal("10000.00"))
                .build();

        FinancingUpdateDTO updateDTO = new FinancingUpdateDTO("PRONAF Aprovado", FinancingStatus.APPROVED, new BigDecimal("12000.00"));

        when(financingRepository.findById(id)).thenReturn(Optional.of(existing));
        when(financingRepository.save(any(Financing.class))).thenAnswer(i -> i.getArgument(0));

        Financing result = useCase.update(id, updateDTO, admin);

        assertThat(result.getDescription()).isEqualTo("PRONAF Aprovado");
        assertThat(result.getStatus()).isEqualTo(FinancingStatus.APPROVED);
        assertThat(result.getAmount()).isEqualByComparingTo("12000.00");

        verify(accessValidator).validateWriteAccess(farmer, admin);
        verify(financingRepository).save(any(Financing.class));
        verify(auditLogService).record(eq(admin), eq(AuditAction.UPDATE), eq("financings"), eq(id), any());
    }

    @Test
    @DisplayName("Lança AccessDeniedException quando MANAGER tenta atualizar")
    void givenManager_whenUpdate_thenThrowsAccessDeniedException() {
        User manager = UserTestFactory.user().manager().build();
        UUID id = UUID.randomUUID();
        FinancingUpdateDTO updateDTO = new FinancingUpdateDTO("PRONAF Aprovado", FinancingStatus.APPROVED, new BigDecimal("12000.00"));

        assertThatThrownBy(() -> useCase.update(id, updateDTO, manager))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Apenas administradores, técnicos assistentes ou o próprio agricultor podem atualizar financiamentos.");

        verifyNoInteractions(financingRepository, accessValidator, auditLogService);
    }
}
