package com.ufal.smartagro.application.service.farmer.family;

import com.ufal.smartagro.adapters.in.web.dto.farmer.family.FamilyCompositionRegisterDTO;
import com.ufal.smartagro.application.service.production.ProductionAccessValidator;
import com.ufal.smartagro.domain.model.FamilyComposition;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.FamilyCompositionRepository;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
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
class RegisterFamilyCompositionUseCaseTest {

    @Mock
    private FamilyCompositionRepository familyCompositionRepository;

    @Mock
    private FarmerRepository farmerRepository;

    @Mock
    private ProductionAccessValidator accessValidator;

    private RegisterFamilyCompositionUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RegisterFamilyCompositionUseCase(familyCompositionRepository, farmerRepository, accessValidator);
    }

    @Test
    @DisplayName("Cadastra membro familiar com sucesso")
    void givenValidData_whenRegister_thenSucceeds() {
        User admin = UserTestFactory.user().admin().build();
        Farmer farmer = UserTestFactory.farmerProfile().build();
        FamilyCompositionRegisterDTO dto = new FamilyCompositionRegisterDTO("Maria Silva", "Esposa", 42);

        when(farmerRepository.findById(farmer.getId())).thenReturn(Optional.of(farmer));
        when(familyCompositionRepository.save(any(FamilyComposition.class))).thenAnswer(i -> i.getArgument(0));

        FamilyComposition result = useCase.register(farmer.getId(), dto, admin);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Maria Silva");
        assertThat(result.getKinship()).isEqualTo("Esposa");
        assertThat(result.getAge()).isEqualTo(42);
        assertThat(result.getFarmer()).isEqualTo(farmer);

        verify(accessValidator).validateWriteAccess(farmer, admin);
        verify(familyCompositionRepository).save(any(FamilyComposition.class));
    }

    @Test
    @DisplayName("Lança IllegalArgumentException quando agricultor não existe")
    void givenNonExistingFarmer_whenRegister_thenThrowsIllegalArgumentException() {
        User admin = UserTestFactory.user().admin().build();
        UUID farmerId = UUID.randomUUID();
        FamilyCompositionRegisterDTO dto = new FamilyCompositionRegisterDTO("Maria Silva", "Esposa", 42);

        when(farmerRepository.findById(farmerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.register(farmerId, dto, admin))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Agricultor não encontrado.");

        verify(familyCompositionRepository, never()).save(any());
    }
}
