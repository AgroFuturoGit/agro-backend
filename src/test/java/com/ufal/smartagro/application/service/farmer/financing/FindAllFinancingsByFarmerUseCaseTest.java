package com.ufal.smartagro.application.service.farmer.financing;

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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FindAllFinancingsByFarmerUseCaseTest {

    @Mock
    private FinancingRepository financingRepository;

    @Mock
    private FarmerRepository farmerRepository;

    @Mock
    private ProductionAccessValidator accessValidator;

    private FindAllFinancingsByFarmerUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FindAllFinancingsByFarmerUseCase(financingRepository, farmerRepository, accessValidator);
    }

    @Test
    @DisplayName("Lista financiamentos do agricultor com sucesso")
    void givenFarmer_whenFindAll_thenReturnsList() {
        User admin = UserTestFactory.user().admin().build();
        Farmer farmer = UserTestFactory.farmerProfile().build();
        Financing f1 = Financing.builder().description("F1").status(FinancingStatus.APPROVED).amount(BigDecimal.TEN).build();
        Financing f2 = Financing.builder().description("F2").status(FinancingStatus.PAID).amount(BigDecimal.ONE).build();

        when(farmerRepository.findById(farmer.getId())).thenReturn(Optional.of(farmer));
        when(financingRepository.findAllByFarmerId(farmer.getId())).thenReturn(List.of(f1, f2));

        List<Financing> result = useCase.findAll(farmer.getId(), admin);

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(f1, f2);
        verify(accessValidator).validateAccess(farmer, admin);
    }
}
