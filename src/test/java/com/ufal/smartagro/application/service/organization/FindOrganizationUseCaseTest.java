package com.ufal.smartagro.application.service.organization;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.Manager;
import com.ufal.smartagro.domain.model.Organization;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.ManagerRepository;
import com.ufal.smartagro.domain.port.out.OrganizationRepository;
import com.ufal.smartagro.testsupport.UserTestFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindOrganizationUseCaseTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private ManagerRepository managerRepository;

    @InjectMocks
    private FindOrganizationByIdUseCase findOrganizationByIdUseCase;

    @InjectMocks
    private FindAllOrganizationsUseCase findAllOrganizationsUseCase;

    @Nested
    @DisplayName("FindAllOrganizationsUseCase")
    class FindAllTest {

        @Test
        @DisplayName("Given ADMIN When findAll Then retorna lista de organizações")
        void givenAdmin_whenFindAll_thenReturnsList() {
            User admin = UserTestFactory.user().admin().build();
            Organization org = UserTestFactory.organization().name("Coop Norte").build();

            when(organizationRepository.findAll()).thenReturn(List.of(org));

            List<Organization> result = findAllOrganizationsUseCase.findAll(admin);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Coop Norte");
        }

        @Test
        @DisplayName("Given não-ADMIN When findAll Then lança AccessDeniedException")
        void givenNonAdmin_whenFindAll_thenThrowsAccessDeniedException() {
            User manager = UserTestFactory.user().manager().build();

            assertThatThrownBy(() -> findAllOrganizationsUseCase.findAll(manager))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Apenas administradores podem listar todas as organizações.");
        }
    }

    @Nested
    @DisplayName("FindOrganizationByIdUseCase")
    class FindByIdTest {

        @Test
        @DisplayName("Given ADMIN When findById Then retorna organização")
        void givenAdmin_whenFindById_thenReturnsOrganization() {
            User admin = UserTestFactory.user().admin().build();
            Organization org = UserTestFactory.organization().name("Coop Norte").build();
            UUID orgId = org.getId();

            when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));

            Organization result = findOrganizationByIdUseCase.findById(orgId, admin);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(orgId);
        }

        @Test
        @DisplayName("Given MANAGER da própria organização When findById Then retorna organização com sucesso")
        void givenManager_whenOwnOrganization_thenReturnsOrganization() {
            Organization org = UserTestFactory.organization().name("Coop Própria").build();
            UUID orgId = org.getId();
            User managerUser = UserTestFactory.user().manager().build();
            Manager manager = UserTestFactory.managerProfile().user(managerUser).organization(org).build();

            when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));
            when(managerRepository.findByUserId(managerUser.getId())).thenReturn(Optional.of(manager));

            Organization result = findOrganizationByIdUseCase.findById(orgId, managerUser);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(orgId);
        }

        @Test
        @DisplayName("Given MANAGER de outra organização When findById Then lança AccessDeniedException (IDOR protegido)")
        void givenManager_whenOtherOrganization_thenThrowsAccessDeniedException() {
            Organization ownOrg = UserTestFactory.organization().name("Coop Própria").build();
            Organization targetOrg = UserTestFactory.organization().name("Coop Alheia").build();

            User managerUser = UserTestFactory.user().manager().build();
            Manager manager = UserTestFactory.managerProfile().user(managerUser).organization(ownOrg).build();

            when(organizationRepository.findById(targetOrg.getId())).thenReturn(Optional.of(targetOrg));
            when(managerRepository.findByUserId(managerUser.getId())).thenReturn(Optional.of(manager));

            assertThatThrownBy(() -> findOrganizationByIdUseCase.findById(targetOrg.getId(), managerUser))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("O gestor só tem acesso à sua própria organização.");
        }

        @Test
        @DisplayName("Given FARMER ou TECHNICIAN When findById Then lança AccessDeniedException")
        void givenFarmerOrTechnician_whenFindById_thenThrowsAccessDeniedException() {
            User farmer = UserTestFactory.user().farmer().build();
            Organization org = UserTestFactory.organization().build();
            UUID orgId = org.getId();

            when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));

            assertThatThrownBy(() -> findOrganizationByIdUseCase.findById(orgId, farmer))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Apenas administradores e gestores podem consultar os dados da organização.");
        }

        @Test
        @DisplayName("Given organização inexistente When findById Then lança EntityNotFoundException")
        void givenMissingOrganization_whenFindById_thenThrowsEntityNotFoundException() {
            User admin = UserTestFactory.user().admin().build();
            UUID missingId = UUID.randomUUID();

            when(organizationRepository.findById(missingId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> findOrganizationByIdUseCase.findById(missingId, admin))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Organização não encontrada.");
        }
    }
}
