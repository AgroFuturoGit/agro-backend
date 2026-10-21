package com.ufal.smartagro.application.service.farmer.family;

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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FindAllFamilyCompositionsByFarmerUseCaseTest {

    @Mock
    private FamilyCompositionRepository familyCompositionRepository;

    @Mock
    private FarmerRepository farmerRepository;

    @Mock
    private ProductionAccessValidator accessValidator;

    private FindAllFamilyCompositionsByFarmerUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FindAllFamilyCompositionsByFarmerUseCase(familyCompositionRepository, farmerRepository, accessValidator);
    }

    @Test
    @DisplayName("Lista membros familiares do agricultor com sucesso")
    void givenFarmer_whenFindAll_thenReturnsList() {
        User admin = UserTestFactory.user().admin().build();
        Farmer farmer = UserTestFactory.farmerProfile().build();
        FamilyComposition comp1 = FamilyComposition.builder().name("Filho 1").build();
        FamilyComposition comp2 = FamilyComposition.builder().name("Filho 2").build();

        when(farmerRepository.findById(farmer.getId())).thenReturn(Optional.of(farmer));
        when(familyCompositionRepository.findAllByFarmerId(farmer.getId())).thenReturn(List.of(comp1, comp2));

        List<FamilyComposition> result = useCase.findAll(farmer.getId(), admin);

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(comp1, comp2);
        verify(accessValidator).validateAccess(farmer, admin);
    }
}
