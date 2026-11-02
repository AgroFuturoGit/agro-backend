package com.ufal.smartagro.application.service.farmer.financing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.ufal.smartagro.adapters.in.web.dto.farmer.financing.FinancingRegisterDTO;
import com.ufal.smartagro.application.service.audit.AuditLogService;
import com.ufal.smartagro.application.service.production.ProductionAccessValidator;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.Financing;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.AuditAction;
import com.ufal.smartagro.domain.model.enums.FinancingStatus;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
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
class RegisterFinancingUseCaseTest {

    @Mock
    private FinancingRepository financingRepository;

    @Mock
    private FarmerRepository farmerRepository;

    @Mock
    private ProductionAccessValidator accessValidator;

    @Mock
    private AuditLogService auditLogService;

    private RegisterFinancingUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RegisterFinancingUseCase(financingRepository, farmerRepository, accessValidator, auditLogService);
    }

    @Test
    @DisplayName("Cadastra financiamento com sucesso e grava auditoria")
    void givenValidData_whenRegister_thenSucceeds() {
        User admin = UserTestFactory.user().admin().build();
        Farmer farmer = UserTestFactory.farmerProfile().build();
        FinancingRegisterDTO dto = new FinancingRegisterDTO("PRONAF Custeio", FinancingStatus.APPROVED, new BigDecimal("25000.00"));

        when(farmerRepository.findById(farmer.getId())).thenReturn(Optional.of(farmer));
        when(financingRepository.save(any(Financing.class))).thenAnswer(i -> {
            Financing f = i.getArgument(0);
            return Financing.builder()
                    .id(UUID.randomUUID())
                    .farmer(f.getFarmer())
                    .description(f.getDescription())
                    .status(f.getStatus())
                    .amount(f.getAmount())
                    .build();
        });

        Financing result = useCase.register(farmer.getId(), dto, admin);

        assertThat(result).isNotNull();
        assertThat(result.getDescription()).isEqualTo("PRONAF Custeio");
        assertThat(result.getStatus()).isEqualTo(FinancingStatus.APPROVED);
        assertThat(result.getAmount()).isEqualByComparingTo("25000.00");
        assertThat(result.getFarmer()).isEqualTo(farmer);

        verify(accessValidator).validateWriteAccess(farmer, admin);
        verify(financingRepository).save(any(Financing.class));
        verify(auditLogService).record(eq(admin), eq(AuditAction.CREATE), eq("financings"), eq(result.getId()), any());
    }

    @Test
    @DisplayName("Lança AccessDeniedException quando MANAGER tenta cadastrar")
    void givenManager_whenRegister_thenThrowsAccessDeniedException() {
        User manager = UserTestFactory.user().manager().build();
        UUID farmerId = UUID.randomUUID();
        FinancingRegisterDTO dto = new FinancingRegisterDTO("PRONAF Custeio", FinancingStatus.APPROVED, new BigDecimal("25000.00"));

        assertThatThrownBy(() -> useCase.register(farmerId, dto, manager))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Apenas administradores, técnicos assistentes ou o próprio agricultor podem cadastrar financiamentos.");

        verifyNoInteractions(farmerRepository, financingRepository, accessValidator, auditLogService);
    }

    @Test
    @DisplayName("Lança EntityNotFoundException quando agricultor não existe")
    void givenNonExistingFarmer_whenRegister_thenThrowsEntityNotFoundException() {
        User admin = UserTestFactory.user().admin().build();
        UUID farmerId = UUID.randomUUID();
        FinancingRegisterDTO dto = new FinancingRegisterDTO("PRONAF", FinancingStatus.PENDING, new BigDecimal("10000.00"));

        when(farmerRepository.findById(farmerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.register(farmerId, dto, admin))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Agricultor não encontrado.");

        verify(financingRepository, never()).save(any());
        verifyNoInteractions(auditLogService);
    }
}
