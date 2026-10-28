package com.ufal.smartagro.application.service.organization;

import com.ufal.smartagro.domain.exception.EntityInUseException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.Community;
import com.ufal.smartagro.domain.model.Organization;
import com.ufal.smartagro.domain.port.out.CommunityRepository;
import com.ufal.smartagro.domain.port.out.OrganizationRepository;
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
class DeleteOrganizationUseCaseTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private CommunityRepository communityRepository;

    private DeleteOrganizationUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteOrganizationUseCase(organizationRepository, communityRepository);
    }

    @Test
    @DisplayName("Quando existem comunidades vinculadas à organização, lança EntityInUseException (409)")
    void givenOrgWithCommunities_whenDelete_thenThrowsEntityInUseException() {
        UUID orgId = UUID.randomUUID();
        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(mock(Organization.class)));
        when(communityRepository.findAllByOrganizationId(orgId)).thenReturn(List.of(mock(Community.class)));

        assertThatThrownBy(() -> useCase.delete(orgId))
                .isInstanceOf(EntityInUseException.class)
                .hasMessageContaining("comunidades vinculadas");

        verify(organizationRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Quando a organização não existe, lança EntityNotFoundException (404)")
    void givenNonExistentOrg_whenDelete_thenThrowsEntityNotFoundException() {
        UUID orgId = UUID.randomUUID();
        when(organizationRepository.findById(orgId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.delete(orgId))
                .isInstanceOf(EntityNotFoundException.class);

        verify(organizationRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Quando não existem comunidades vinculadas, deleta organização com sucesso")
    void givenOrgWithoutCommunities_whenDelete_thenDeletesSuccessfully() {
        UUID orgId = UUID.randomUUID();
        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(mock(Organization.class)));
        when(communityRepository.findAllByOrganizationId(orgId)).thenReturn(List.of());

        useCase.delete(orgId);

        verify(organizationRepository).delete(orgId);
    }
}
