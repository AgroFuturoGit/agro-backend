package com.ufal.smartagro.application.service.farmer;

import com.ufal.smartagro.application.service.production.ProductionAccessValidator;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
import com.ufal.smartagro.domain.port.out.UserRepository;
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
@DisplayName("DeleteFarmerUseCase — exclusão lógica de agricultor e desativação do usuário (API-11)")
class DeleteFarmerUseCaseTest {

    @Mock
    private FarmerRepository farmerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductionAccessValidator accessValidator;

    private DeleteFarmerUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteFarmerUseCase(farmerRepository, userRepository, accessValidator);
    }

    @Test
    @DisplayName("Quando agricultor possui User vinculado, exclui agricultor e faz soft-delete do User (API-11)")
    void givenFarmerWithUser_whenDelete_thenSoftDeletesFarmerAndUser() {
        User farmerUser = UserTestFactory.user().farmer().build();
        Farmer farmer = UserTestFactory.farmerProfile().user(farmerUser).build();
        User admin = UserTestFactory.user().admin().build();

        when(farmerRepository.findById(farmer.getId())).thenReturn(Optional.of(farmer));

        useCase.delete(farmer.getId(), admin);

        verify(accessValidator).validateAccess(farmer, admin);
        verify(farmerRepository).delete(farmer.getId());
        verify(userRepository).softDelete(farmerUser.getId());
    }

    @Test
    @DisplayName("Quando agricultor não possui User vinculado, exclui apenas o agricultor")
    void givenFarmerWithoutUser_whenDelete_thenDeletesOnlyFarmer() {
        Farmer farmer = UserTestFactory.farmerProfile().user(null).build();
        when(farmerRepository.findById(farmer.getId())).thenReturn(Optional.of(farmer));

        useCase.delete(farmer.getId());

        verify(farmerRepository).delete(farmer.getId());
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Quando agricultor não existe, lança EntityNotFoundException (404)")
    void givenNonExistentFarmer_whenDelete_thenThrowsEntityNotFoundException() {
        UUID missingId = UUID.randomUUID();
        when(farmerRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.delete(missingId))
                .isInstanceOf(EntityNotFoundException.class);

        verify(farmerRepository, never()).delete(any());
        verifyNoInteractions(userRepository);
    }
}
