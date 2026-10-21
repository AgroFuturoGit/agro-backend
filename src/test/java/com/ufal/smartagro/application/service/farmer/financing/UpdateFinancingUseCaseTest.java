package com.ufal.smartagro.application.service.farmer.financing;

import com.ufal.smartagro.adapters.in.web.dto.farmer.financing.FinancingUpdateDTO;
import com.ufal.smartagro.application.service.production.ProductionAccessValidator;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.Financing;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.FinancingStatus;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateFinancingUseCaseTest {

    @Mock
    private FinancingRepository financingRepository;

    @Mock
    private ProductionAccessValidator accessValidator;

    private UpdateFinancingUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateFinancingUseCase(financingRepository, accessValidator);
    }

    @Test
    @DisplayName("Atualiza financiamento com sucesso")
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
    }
}
