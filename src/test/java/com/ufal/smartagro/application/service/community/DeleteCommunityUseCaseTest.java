package com.ufal.smartagro.application.service.community;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityInUseException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.Community;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.Manager;
import com.ufal.smartagro.domain.model.Organization;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.OrganizationType;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.CommunityRepository;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
import com.ufal.smartagro.domain.port.out.ManagerRepository;
import com.ufal.smartagro.testsupport.UserTestFactory;
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

    @Mock
    private ManagerRepository managerRepository;

    private DeleteCommunityUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteCommunityUseCase(communityRepository, farmerRepository, managerRepository);
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
    @DisplayName("Quando MANAGER tenta excluir comunidade de outra organização, lança AccessDeniedException (403)")
    void givenManagerFromAnotherOrg_whenDelete_thenThrowsAccessDeniedException() {
        UUID communityId = UUID.randomUUID();
        User managerUser = UserTestFactory.user().role(Role.MANAGER).build();
        Organization orgA = new Organization(UUID.randomUUID(), "Org A", "11111111000111", OrganizationType.COOP, null, null, null);
        Organization orgB = new Organization(UUID.randomUUID(), "Org B", "22222222000122", OrganizationType.ASSOC, null, null, null);
        Community community = new Community(communityId, "Comunidade B", orgB, null, null, null);
        Manager manager = new Manager(UUID.randomUUID(), managerUser, orgA, "Gestor A", null, null, null, null, null);

        when(communityRepository.findById(communityId)).thenReturn(Optional.of(community));
        when(managerRepository.findByUserId(managerUser.getId())).thenReturn(Optional.of(manager));

        assertThatThrownBy(() -> useCase.delete(communityId, managerUser))
                .isInstanceOf(AccessDeniedException.class);

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
