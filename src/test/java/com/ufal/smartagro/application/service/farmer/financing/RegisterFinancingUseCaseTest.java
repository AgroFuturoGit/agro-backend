package com.ufal.smartagro.application.service.farmer.financing;

import com.ufal.smartagro.adapters.in.web.dto.farmer.financing.FinancingRegisterDTO;
import com.ufal.smartagro.application.service.production.ProductionAccessValidator;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.Financing;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.FinancingStatus;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
import com.ufal.smartagro.domain.port.out.FinancingRepository;
import com.ufal.smartagro.testsupport.UserTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterFinancingUseCaseTest {

    @Mock
    private FinancingRepository financingRepository;

    @Mock
    private FarmerRepository farmerRepository;

    @Mock
    private ProductionAccessValidator accessValidator;

    private RegisterFinancingUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RegisterFinancingUseCase(financingRepository, farmerRepository, accessValidator);
    }

    @Test
    @DisplayName("Cadastra financiamento com sucesso")
    void givenValidData_whenRegister_thenSucceeds() {
        User admin = UserTestFactory.user().admin().build();
        Farmer farmer = UserTestFactory.farmerProfile().build();
        FinancingRegisterDTO dto = new FinancingRegisterDTO("PRONAF Custeio", FinancingStatus.APPROVED, new BigDecimal("25000.00"));

        when(farmerRepository.findById(farmer.getId())).thenReturn(Optional.of(farmer));
        when(financingRepository.save(any(Financing.class))).thenAnswer(i -> i.getArgument(0));

        Financing result = useCase.register(farmer.getId(), dto, admin);

        assertThat(result).isNotNull();
        assertThat(result.getDescription()).isEqualTo("PRONAF Custeio");
        assertThat(result.getStatus()).isEqualTo(FinancingStatus.APPROVED);
        assertThat(result.getAmount()).isEqualByComparingTo("25000.00");
        assertThat(result.getFarmer()).isEqualTo(farmer);

        verify(accessValidator).validateWriteAccess(farmer, admin);
        verify(financingRepository).save(any(Financing.class));
    }

    @Test
    @DisplayName("Lança IllegalArgumentException quando agricultor não existe")
    void givenNonExistingFarmer_whenRegister_thenThrowsIllegalArgumentException() {
        User admin = UserTestFactory.user().admin().build();
        UUID farmerId = UUID.randomUUID();
        FinancingRegisterDTO dto = new FinancingRegisterDTO("PRONAF", FinancingStatus.PENDING, new BigDecimal("10000.00"));

        when(farmerRepository.findById(farmerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.register(farmerId, dto, admin))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Agricultor não encontrado.");

        verify(financingRepository, never()).save(any());
    }
}
