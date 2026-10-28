package com.ufal.smartagro.application.service.farmer.family;

import com.ufal.smartagro.adapters.in.web.dto.farmer.family.FamilyCompositionUpdateDTO;
import com.ufal.smartagro.application.service.production.ProductionAccessValidator;
import com.ufal.smartagro.domain.model.FamilyComposition;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.FamilyCompositionRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateFamilyCompositionUseCaseTest {

    @Mock
    private FamilyCompositionRepository familyCompositionRepository;

    @Mock
    private ProductionAccessValidator accessValidator;

    private UpdateFamilyCompositionUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateFamilyCompositionUseCase(familyCompositionRepository, accessValidator);
    }

    @Test
    @DisplayName("Atualiza membro familiar com sucesso")
    void givenValidUpdate_whenUpdate_thenSucceeds() {
        User admin = UserTestFactory.user().admin().build();
        Farmer farmer = UserTestFactory.farmerProfile().build();
        UUID id = UUID.randomUUID();
        FamilyComposition existing = FamilyComposition.builder()
                .id(id)
                .farmer(farmer)
                .name("Maria Silva")
                .kinship("Esposa")
                .age(42)
                .build();

        FamilyCompositionUpdateDTO updateDTO = new FamilyCompositionUpdateDTO("Maria da Silva Santos", "Cônjuge", 43);

        when(familyCompositionRepository.findById(id)).thenReturn(Optional.of(existing));
        when(familyCompositionRepository.save(any(FamilyComposition.class))).thenAnswer(i -> i.getArgument(0));

        FamilyComposition result = useCase.update(id, updateDTO, admin);

        assertThat(result.getName()).isEqualTo("Maria da Silva Santos");
        assertThat(result.getKinship()).isEqualTo("Cônjuge");
        assertThat(result.getAge()).isEqualTo(43);

        verify(accessValidator).validateWriteAccess(farmer, admin);
        verify(familyCompositionRepository).save(any(FamilyComposition.class));
    }
}
