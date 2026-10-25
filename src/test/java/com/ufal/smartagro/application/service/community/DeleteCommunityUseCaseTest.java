package com.ufal.smartagro.application.service.community;

import com.ufal.smartagro.domain.exception.EntityInUseException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.Community;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.port.out.CommunityRepository;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteCommunityUseCaseTest {

    @Mock
    private CommunityRepository communityRepository;

    @Mock
    private FarmerRepository farmerRepository;

    private DeleteCommunityUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteCommunityUseCase(communityRepository, farmerRepository);
    }

    @Test
    @DisplayName("Quando existem agricultores vinculados à comunidade, lança EntityInUseException (409)")
    void givenCommunityWithFarmers_whenDelete_thenThrowsEntityInUseException() {
        UUID communityId = UUID.randomUUID();
        when(communityRepository.findById(communityId)).thenReturn(Optional.of(mock(Community.class)));
        when(farmerRepository.findAllByCommunityId(communityId)).thenReturn(List.of(mock(Farmer.class)));

        assertThatThrownBy(() -> useCase.delete(communityId))
                .isInstanceOf(EntityInUseException.class)
                .hasMessageContaining("agricultores vinculados");

        verify(communityRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Quando a comunidade não existe, lança EntityNotFoundException (404)")
    void givenNonExistentCommunity_whenDelete_thenThrowsEntityNotFoundException() {
        UUID communityId = UUID.randomUUID();
        when(communityRepository.findById(communityId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.delete(communityId))
                .isInstanceOf(EntityNotFoundException.class);

        verify(communityRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Quando não existem agricultores vinculados, deleta comunidade com sucesso")
    void givenCommunityWithoutFarmers_whenDelete_thenDeletesSuccessfully() {
        UUID communityId = UUID.randomUUID();
        when(communityRepository.findById(communityId)).thenReturn(Optional.of(mock(Community.class)));
        when(farmerRepository.findAllByCommunityId(communityId)).thenReturn(List.of());

        useCase.delete(communityId);

        verify(communityRepository).delete(communityId);
    }
}
