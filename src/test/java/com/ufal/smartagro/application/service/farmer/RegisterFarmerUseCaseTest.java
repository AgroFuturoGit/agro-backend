package com.ufal.smartagro.application.service.farmer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.ufal.smartagro.adapters.in.web.dto.farmer.FarmerRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.user.UserRegisterDTO;
import com.ufal.smartagro.application.service.audit.AuditLogService;
import com.ufal.smartagro.application.service.user.UserRegisterUseCase;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.model.Community;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.Manager;
import com.ufal.smartagro.domain.model.Organization;
import com.ufal.smartagro.domain.model.TechnicalAssistance;
import com.ufal.smartagro.domain.model.Technician;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.AuditAction;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.CommunityRepository;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
import com.ufal.smartagro.domain.port.out.ManagerRepository;
import com.ufal.smartagro.domain.port.out.TechnicalAssistanceRepository;
import com.ufal.smartagro.domain.port.out.TechnicianRepository;
import com.ufal.smartagro.testsupport.UserTestFactory;
import java.util.Optional;
import java.util.UUID;
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
    private TechnicianRepository technicianRepository;

    @Mock
    private TechnicalAssistanceRepository technicalAssistanceRepository;

    @Mock
    private UserRegisterUseCase userRegisterUseCase;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private RegisterFarmerUseCase registerFarmerUseCase;

    @Nested
    @DisplayName("Happy path — ADMIN e MANAGER da própria organização")
    class HappyPath {

        @Test
        @DisplayName("Given ADMIN When register em qualquer community Then persiste Farmer e grava auditoria")
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
            verify(auditLogService).record(eq(admin), eq(AuditAction.CREATE), eq("farmers"), any(), any());
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
            verify(auditLogService).record(eq(managerUser), eq(AuditAction.CREATE), eq("farmers"), any(), any());
        }
    }

    @Nested
    @DisplayName("RBAC — FARMER não cadastra")
    class RbacBlocking {

        @ParameterizedTest(name = "Given Role.{0} When register Then AccessDeniedException")
        @EnumSource(value = Role.class, names = {"FARMER"})
        void givenFarmer_whenRegister_thenAccessDenied(Role actorRole) {
            User loggedUser = UserTestFactory.user().role(actorRole).build();
            FarmerRegisterDTO dto = UserTestFactory.farmerRegisterDto().build();
            UUID communityId = UUID.randomUUID();

            assertThatThrownBy(() -> registerFarmerUseCase.register(communityId, dto, loggedUser))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage(ACCESS_DENIED_MESSAGE);

            verifyNoInteractions(communityRepository, managerRepository, userRegisterUseCase, farmerRepository, auditLogService);
        }
    }

    @Nested
    @DisplayName("Cadastro por TECHNICIAN")
    class TechnicianRegistration {

        @Test
        @DisplayName("Given TECHNICIAN com assistência ativa na comunidade When register Then persiste Farmer com source TECHNICIAN")
        void givenTechnician_whenAssistingCommunity_thenPersistsFarmerWithTechnicianSource() {
            User techUser = UserTestFactory.user().technician().build();
            Technician technician = UserTestFactory.technicianProfile().user(techUser).build();
            Community community = UserTestFactory.community().build();
            FarmerRegisterDTO dto = UserTestFactory.farmerRegisterDto().build();
            TechnicalAssistance assistance = UserTestFactory.technicalAssistance().technician(technician).community(community).build();

            when(communityRepository.findById(community.getId())).thenReturn(Optional.of(community));
            when(technicianRepository.findByUserId(techUser.getId())).thenReturn(Optional.of(technician));
            when(technicalAssistanceRepository.findActiveByTechnicianAndCommunity(technician.getId(), community.getId())).thenReturn(Optional.of(assistance));
            stubSuccessfulPersist(community, dto);

            Farmer result = registerFarmerUseCase.register(community.getId(), dto, techUser);

            assertThat(result).isNotNull();
            ArgumentCaptor<Farmer> farmerCaptor = ArgumentCaptor.forClass(Farmer.class);
            verify(farmerRepository).save(farmerCaptor.capture());
            assertThat(farmerCaptor.getValue().getRegistrationSource()).isEqualTo(com.ufal.smartagro.domain.model.enums.RegistrationSource.TECHNICIAN);
            verify(auditLogService).record(eq(techUser), eq(AuditAction.CREATE), eq("farmers"), any(), any());
        }

        @Test
        @DisplayName("Given TECHNICIAN sem assistência ativa na comunidade When register Then AccessDeniedException")
        void givenTechnician_whenNotAssistingCommunity_thenAccessDenied() {
            User techUser = UserTestFactory.user().technician().build();
            Technician technician = UserTestFactory.technicianProfile().user(techUser).build();
            Community community = UserTestFactory.community().build();
            FarmerRegisterDTO dto = UserTestFactory.farmerRegisterDto().build();

            when(communityRepository.findById(community.getId())).thenReturn(Optional.of(community));
            when(technicianRepository.findByUserId(techUser.getId())).thenReturn(Optional.of(technician));
            when(technicalAssistanceRepository.findActiveByTechnicianAndCommunity(technician.getId(), community.getId())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> registerFarmerUseCase.register(community.getId(), dto, techUser))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("O técnico só pode cadastrar agricultores em comunidades sob sua assistência técnica.");

            verify(farmerRepository, never()).save(any());
            verifyNoInteractions(auditLogService);
        }

        @Test
        @DisplayName("Given TECHNICIAN sem comunidade informada When register Then AccessDeniedException")
        void givenTechnician_whenMissingCommunity_thenAccessDenied() {
            User techUser = UserTestFactory.user().technician().build();
            FarmerRegisterDTO dto = UserTestFactory.farmerRegisterDto().build();

            assertThatThrownBy(() -> registerFarmerUseCase.register(null, dto, techUser))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("O técnico precisa vincular o agricultor a uma comunidade.");

            verify(farmerRepository, never()).save(any());
            verifyNoInteractions(auditLogService);
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
            verifyNoInteractions(auditLogService);
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
            verifyNoInteractions(auditLogService);
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
            verifyNoInteractions(auditLogService);
        }

        @Test
        @DisplayName("Given CPF de agricultor já cadastrado When register Then CpfAlreadyExistsException")
        void givenDuplicateFarmerCpf_whenRegister_thenThrowsCpfAlreadyExistsException() {
            User admin = UserTestFactory.user().admin().build();
            FarmerRegisterDTO dto = UserTestFactory.farmerRegisterDto().build();
            when(farmerRepository.existsByCpf(dto.cpf())).thenReturn(true);

            assertThatThrownBy(() -> registerFarmerUseCase.register(UUID.randomUUID(), dto, admin))
                    .isInstanceOf(com.ufal.smartagro.domain.exception.CpfAlreadyExistsException.class)
                    .hasMessage("O CPF informado já está em uso.");

            verifyNoInteractions(communityRepository, managerRepository, userRegisterUseCase);
            verify(farmerRepository, never()).save(any());
            verifyNoInteractions(auditLogService);
        }
    }

    private void stubSuccessfulPersist(Community community, FarmerRegisterDTO dto) {
        User savedUser = UserTestFactory.user().farmer()
                .fullName(dto.fullName())
                .email(dto.email())
                .cpf(dto.cpf())
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
