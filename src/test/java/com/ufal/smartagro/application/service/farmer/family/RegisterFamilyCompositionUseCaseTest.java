package com.ufal.smartagro.application.service.farmer.family;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.ufal.smartagro.adapters.in.web.dto.farmer.family.FamilyCompositionRegisterDTO;
import com.ufal.smartagro.application.service.audit.AuditLogService;
import com.ufal.smartagro.application.service.production.ProductionAccessValidator;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.FamilyComposition;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.AuditAction;
import com.ufal.smartagro.domain.port.out.FamilyCompositionRepository;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
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
class RegisterFamilyCompositionUseCaseTest {

    @Mock
    private FamilyCompositionRepository familyCompositionRepository;

    @Mock
    private FarmerRepository farmerRepository;

    @Mock
    private ProductionAccessValidator accessValidator;

    @Mock
    private AuditLogService auditLogService;

    private RegisterFamilyCompositionUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RegisterFamilyCompositionUseCase(familyCompositionRepository, farmerRepository, accessValidator, auditLogService);
    }

    @Test
    @DisplayName("Cadastra membro familiar com sucesso e grava auditoria")
    void givenValidData_whenRegister_thenSucceeds() {
        User admin = UserTestFactory.user().admin().build();
        Farmer farmer = UserTestFactory.farmerProfile().build();
        FamilyCompositionRegisterDTO dto = new FamilyCompositionRegisterDTO("Maria Silva", "Esposa", 42);

        when(farmerRepository.findById(farmer.getId())).thenReturn(Optional.of(farmer));
        when(familyCompositionRepository.save(any(FamilyComposition.class))).thenAnswer(i -> {
            FamilyComposition fc = i.getArgument(0);
            return FamilyComposition.builder()
                    .id(UUID.randomUUID())
                    .farmer(fc.getFarmer())
                    .name(fc.getName())
                    .kinship(fc.getKinship())
                    .age(fc.getAge())
                    .build();
        });

        FamilyComposition result = useCase.register(farmer.getId(), dto, admin);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Maria Silva");
        assertThat(result.getKinship()).isEqualTo("Esposa");
        assertThat(result.getAge()).isEqualTo(42);
        assertThat(result.getFarmer()).isEqualTo(farmer);

        verify(accessValidator).validateWriteAccess(farmer, admin);
        verify(familyCompositionRepository).save(any(FamilyComposition.class));
        verify(auditLogService).record(eq(admin), eq(AuditAction.CREATE), eq("family_compositions"), eq(result.getId()), any());
    }

    @Test
    @DisplayName("Lança AccessDeniedException quando MANAGER tenta cadastrar composição familiar")
    void givenManager_whenRegister_thenThrowsAccessDeniedException() {
        User manager = UserTestFactory.user().manager().build();
        UUID farmerId = UUID.randomUUID();
        FamilyCompositionRegisterDTO dto = new FamilyCompositionRegisterDTO("Maria Silva", "Esposa", 42);

        assertThatThrownBy(() -> useCase.register(farmerId, dto, manager))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Apenas administradores, técnicos assistentes ou o próprio agricultor podem cadastrar membros familiares.");

        verifyNoInteractions(farmerRepository, familyCompositionRepository, accessValidator, auditLogService);
    }

    @Test
    @DisplayName("Lança EntityNotFoundException quando agricultor não existe")
    void givenNonExistingFarmer_whenRegister_thenThrowsEntityNotFoundException() {
        User admin = UserTestFactory.user().admin().build();
        UUID farmerId = UUID.randomUUID();
        FamilyCompositionRegisterDTO dto = new FamilyCompositionRegisterDTO("Maria Silva", "Esposa", 42);

        when(farmerRepository.findById(farmerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.register(farmerId, dto, admin))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Agricultor não encontrado.");

        verify(familyCompositionRepository, never()).save(any());
        verifyNoInteractions(auditLogService);
    }
}
