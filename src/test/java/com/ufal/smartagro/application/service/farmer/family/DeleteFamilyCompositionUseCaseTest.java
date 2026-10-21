package com.ufal.smartagro.application.service.farmer.family;

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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteFamilyCompositionUseCaseTest {

    @Mock
    private FamilyCompositionRepository familyCompositionRepository;

    @Mock
    private ProductionAccessValidator accessValidator;

    private DeleteFamilyCompositionUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteFamilyCompositionUseCase(familyCompositionRepository, accessValidator);
    }

    @Test
    @DisplayName("Exclui membro familiar com sucesso")
    void givenExistingId_whenDelete_thenDeletes() {
        User admin = UserTestFactory.user().admin().build();
        Farmer farmer = UserTestFactory.farmerProfile().build();
        UUID id = UUID.randomUUID();
        FamilyComposition existing = FamilyComposition.builder()
                .id(id)
                .farmer(farmer)
                .name("Maria Silva")
                .build();

        when(familyCompositionRepository.findById(id)).thenReturn(Optional.of(existing));

        useCase.delete(id, admin);

        verify(accessValidator).validateWriteAccess(farmer, admin);
        verify(familyCompositionRepository).delete(id);
    }

    @Test
    @DisplayName("Lança IllegalArgumentException quando membro não existe")
    void givenNonExistingId_whenDelete_thenThrowsIllegalArgumentException() {
        User admin = UserTestFactory.user().admin().build();
        UUID id = UUID.randomUUID();

        when(familyCompositionRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.delete(id, admin))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Membro da composição familiar não encontrado.");

        verify(familyCompositionRepository, never()).delete(any());
    }
}
