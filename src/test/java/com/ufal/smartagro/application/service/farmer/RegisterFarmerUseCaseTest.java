package com.ufal.smartagro.application.service.farmer;

import com.ufal.smartagro.adapters.in.web.dto.farmer.FarmerRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.user.UserRegisterDTO;
import com.ufal.smartagro.application.service.user.UserRegisterUseCase;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.model.Community;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.Manager;
import com.ufal.smartagro.domain.model.Organization;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.CommunityRepository;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
import com.ufal.smartagro.domain.port.out.ManagerRepository;
import com.ufal.smartagro.testsupport.UserTestFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterFarmerUseCaseTest {

    private static final String ACCESS_DENIED_MESSAGE =
            "Acesso negado. Somente administradores podem realizar esta ação.";
    private static final String MANAGER_TENANT_DENIED =
            "O gestor só tem acesso aos agricultores da sua própria organização.";

    @Mock
    private FarmerRepository farmerRepository;

    @Mock
    private CommunityRepository communityRepository;

    @Mock
    private ManagerRepository managerRepository;

    @Mock
    private UserRegisterUseCase userRegisterUseCase;

    @InjectMocks
    private RegisterFarmerUseCase registerFarmerUseCase;

    @Nested
    @DisplayName("Happy path — ADMIN e MANAGER da própria organização")
    class HappyPath {

        @Test
        @DisplayName("Given ADMIN When register em qualquer community Then persiste Farmer")
        void givenAdmin_whenRegister_thenPersistsFarmer() {
            User admin = UserTestFactory.user().admin().build();
            Community community = UserTestFactory.community().build();
            FarmerRegisterDTO dto = UserTestFactory.farmerRegisterDto().build();
            stubSuccessfulPersist(community, dto);

            Farmer result = registerFarmerUseCase.register(community.getId(), dto, admin);

            assertThat(result.getCommunity()).isEqualTo(community);
            assertThat(result.getIsCompliant()).isTrue();
            verifyCapturedFarmerRoleAndCommunity(dto, community);
            verifyNoInteractions(managerRepository);
        }

        @Test
        @DisplayName("Given MANAGER When community da própria org Then persiste Farmer")
        void givenManager_whenCommunityOfOwnOrganization_thenPersistsFarmer() {
            Organization organization = UserTestFactory.organization().build();
            User managerUser = UserTestFactory.user().manager().build();
            Manager manager = UserTestFactory.managerProfile().user(managerUser).organization(organization).build();
            Community community = UserTestFactory.community().organization(organization).build();
            FarmerRegisterDTO dto = UserTestFactory.farmerRegisterDto().build();
            stubSuccessfulPersist(community, dto);
            when(managerRepository.findByUserId(managerUser.getId())).thenReturn(Optional.of(manager));

            Farmer result = registerFarmerUseCase.register(community.getId(), dto, managerUser);

            assertThat(result.getCommunity().getOrganization().getId()).isEqualTo(organization.getId());
            verifyCapturedFarmerRoleAndCommunity(dto, community);
        }
    }

    @Nested
    @DisplayName("RBAC — TECHNICIAN e FARMER (PRODUCER) não cadastram")
    class RbacBlocking {

        @ParameterizedTest(name = "Given Role.{0} When register Then AccessDeniedException")
        @EnumSource(value = Role.class, names = {"TECHNICIAN", "FARMER"})
        void givenTechnicianOrFarmer_whenRegister_thenAccessDenied(Role actorRole) {
            User loggedUser = UserTestFactory.user().role(actorRole).build();
            FarmerRegisterDTO dto = UserTestFactory.farmerRegisterDto().build();
            UUID communityId = UUID.randomUUID();

            assertThatThrownBy(() -> registerFarmerUseCase.register(communityId, dto, loggedUser))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage(ACCESS_DENIED_MESSAGE);

            verifyNoInteractions(communityRepository, managerRepository, userRegisterUseCase, farmerRepository);
        }
    }

    @Nested
    @DisplayName("Isolamento de tenant — MANAGER só na própria Organization")
    class TenantIsolation {

        @Test
        @DisplayName("Given MANAGER When community de outra org Then AccessDeniedException sem save")
        void givenManager_whenCommunityBelongsToAnotherOrganization_thenAccessDenied() {
            Organization managerOrg = UserTestFactory.organization().name("Org do Gestor").build();
            Organization otherOrg = UserTestFactory.organization().name("Org Alheia").build();
            User managerUser = UserTestFactory.user().manager().build();
            Manager manager = UserTestFactory.managerProfile().user(managerUser).organization(managerOrg).build();
            Community foreignCommunity = UserTestFactory.community().organization(otherOrg).build();
            FarmerRegisterDTO dto = UserTestFactory.farmerRegisterDto().build();

            when(communityRepository.findById(foreignCommunity.getId())).thenReturn(Optional.of(foreignCommunity));
            when(managerRepository.findByUserId(managerUser.getId())).thenReturn(Optional.of(manager));

            assertThatThrownBy(() -> registerFarmerUseCase.register(foreignCommunity.getId(), dto, managerUser))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage(MANAGER_TENANT_DENIED);

            verifyNoInteractions(userRegisterUseCase);
            verify(farmerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Given MANAGER When perfil de gestor não existe Then AccessDeniedException")
        void givenManager_whenManagerProfileMissing_thenAccessDenied() {
            User managerUser = UserTestFactory.user().manager().build();
            Community community = UserTestFactory.community().build();
            FarmerRegisterDTO dto = UserTestFactory.farmerRegisterDto().build();
            when(communityRepository.findById(community.getId())).thenReturn(Optional.of(community));
            when(managerRepository.findByUserId(managerUser.getId())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> registerFarmerUseCase.register(community.getId(), dto, managerUser))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Gestor não encontrado.");

            verify(farmerRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Exceções de domínio e dados")
    class DomainExceptions {

        @Test
        @DisplayName("Given comunidade inexistente When register Then IllegalArgumentException")
        void givenMissingCommunity_whenRegister_thenIllegalArgument() {
            User admin = UserTestFactory.user().admin().build();
            FarmerRegisterDTO dto = UserTestFactory.farmerRegisterDto().build();
            UUID missingId = UUID.randomUUID();
            when(communityRepository.findById(missingId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> registerFarmerUseCase.register(missingId, dto, admin))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Comunidade não encontrada");

            verifyNoInteractions(userRegisterUseCase);
            verify(farmerRepository, never()).save(any());
        }
    }

    private void stubSuccessfulPersist(Community community, FarmerRegisterDTO dto) {
        User savedUser = UserTestFactory.user().farmer()
                .fullName(dto.fullName())
                .email(dto.email())
                .cpf(dto.cpf())
                .dateOfBirth(dto.dateOfBirth())
                .build();
        Farmer persisted = UserTestFactory.farmerProfile()
                .user(savedUser)
                .community(community)
                .aliasName(dto.aliasName())
                .build();
        when(communityRepository.findById(community.getId())).thenReturn(Optional.of(community));
        when(userRegisterUseCase.createBaseUser(any(UserRegisterDTO.class))).thenReturn(savedUser);
        when(farmerRepository.save(any(Farmer.class))).thenReturn(persisted);
    }

    private void verifyCapturedFarmerRoleAndCommunity(FarmerRegisterDTO dto, Community community) {
        ArgumentCaptor<UserRegisterDTO> userDtoCaptor = ArgumentCaptor.forClass(UserRegisterDTO.class);
        verify(userRegisterUseCase).createBaseUser(userDtoCaptor.capture());
        assertThat(userDtoCaptor.getValue().role()).isEqualTo(Role.FARMER);
        assertThat(userDtoCaptor.getValue().email()).isEqualTo(dto.email());

        ArgumentCaptor<Farmer> farmerCaptor = ArgumentCaptor.forClass(Farmer.class);
        verify(farmerRepository).save(farmerCaptor.capture());
        assertThat(farmerCaptor.getValue().getId()).isNull();
        assertThat(farmerCaptor.getValue().getCommunity()).isEqualTo(community);
        assertThat(farmerCaptor.getValue().getIsCompliant()).isTrue();
    }
}
