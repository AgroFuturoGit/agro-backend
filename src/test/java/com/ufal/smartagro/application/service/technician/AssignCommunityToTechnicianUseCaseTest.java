package com.ufal.smartagro.application.service.technician;

import com.ufal.smartagro.adapters.in.web.dto.technicalassistance.TechnicalAssistanceRegisterDTO;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.Community;
import com.ufal.smartagro.domain.model.Manager;
import com.ufal.smartagro.domain.model.Organization;
import com.ufal.smartagro.domain.model.TechnicalAssistance;
import com.ufal.smartagro.domain.model.Technician;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.CommunityRepository;
import com.ufal.smartagro.domain.port.out.ManagerRepository;
import com.ufal.smartagro.domain.port.out.TechnicalAssistanceRepository;
import com.ufal.smartagro.domain.port.out.TechnicianRepository;
import com.ufal.smartagro.testsupport.UserTestFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
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
class AssignCommunityToTechnicianUseCaseTest {

    @Mock
    private TechnicianRepository technicianRepository;
    @Mock
    private CommunityRepository communityRepository;
    @Mock
    private TechnicalAssistanceRepository assistanceRepository;
    @Mock
    private ManagerRepository managerRepository;

    @InjectMocks
    private AssignCommunityToTechnicianUseCase assignCommunityToTechnicianUseCase;

    @Nested
    @DisplayName("Happy path")
    class HappyPath {

        @Test
        @DisplayName("Given ADMIN When assign Then persiste assistência")
        void givenAdmin_whenAssign_thenPersists() {
            User admin = UserTestFactory.user().admin().build();
            Technician technician = UserTestFactory.technicianProfile().build();
            Community community = UserTestFactory.community().build();
            TechnicalAssistanceRegisterDTO dto = dto(technician.getId(), community.getId());
            when(communityRepository.findById(community.getId())).thenReturn(Optional.of(community));
            when(technicianRepository.findById(technician.getId())).thenReturn(Optional.of(technician));
            when(assistanceRepository.findActiveByTechnicianAndCommunity(technician.getId(), community.getId()))
                    .thenReturn(Optional.empty());
            when(assistanceRepository.save(any(TechnicalAssistance.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            TechnicalAssistance result = assignCommunityToTechnicianUseCase.assign(dto, admin);

            assertThat(result.getTechnician()).isEqualTo(technician);
            assertThat(result.getCommunity()).isEqualTo(community);
            assertThat(result.getEndDate()).isNull();
        }

        @Test
        @DisplayName("Given MANAGER When community da própria org Then persiste")
        void givenManager_whenCommunityInOwnOrg_thenPersists() {
            Organization organization = UserTestFactory.organization().build();
            User managerUser = UserTestFactory.user().manager().build();
            Manager manager = UserTestFactory.managerProfile().user(managerUser).organization(organization).build();
            Community community = UserTestFactory.community().organization(organization).build();
            Technician technician = UserTestFactory.technicianProfile().build();
            TechnicalAssistanceRegisterDTO dto = dto(technician.getId(), community.getId());
            when(managerRepository.findByUserId(managerUser.getId())).thenReturn(Optional.of(manager));
            when(communityRepository.findById(community.getId())).thenReturn(Optional.of(community));
            when(technicianRepository.findById(technician.getId())).thenReturn(Optional.of(technician));
            when(assistanceRepository.findActiveByTechnicianAndCommunity(technician.getId(), community.getId()))
                    .thenReturn(Optional.empty());
            when(assistanceRepository.save(any(TechnicalAssistance.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            TechnicalAssistance result = assignCommunityToTechnicianUseCase.assign(dto, managerUser);

            assertThat(result.getCommunity().getOrganization().getId()).isEqualTo(organization.getId());
        }

        @Test
        @DisplayName("Given technicianId no path e null no DTO When assign Then persiste assistência com técnico do path")
        void givenTechnicianIdInPathAndNullInDto_whenAssign_thenPersists() {
            User admin = UserTestFactory.user().admin().build();
            Technician technician = UserTestFactory.technicianProfile().build();
            Community community = UserTestFactory.community().build();
            TechnicalAssistanceRegisterDTO dto = new TechnicalAssistanceRegisterDTO(null, community.getId(), LocalDateTime.of(2026, 3, 1, 10, 0));
            when(communityRepository.findById(community.getId())).thenReturn(Optional.of(community));
            when(technicianRepository.findById(technician.getId())).thenReturn(Optional.of(technician));
            when(assistanceRepository.findActiveByTechnicianAndCommunity(technician.getId(), community.getId()))
                    .thenReturn(Optional.empty());
            when(assistanceRepository.save(any(TechnicalAssistance.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            TechnicalAssistance result = assignCommunityToTechnicianUseCase.assign(technician.getId(), dto, admin);

            assertThat(result.getTechnician()).isEqualTo(technician);
            assertThat(result.getCommunity()).isEqualTo(community);
            assertThat(result.getEndDate()).isNull();
        }
    }

    @Nested
    @DisplayName("Negativos — org e role")
    class Negatives {

        @Test
        @DisplayName("Given MANAGER When community de outra org Then AccessDeniedException")
        void givenManager_whenCommunityInAnotherOrg_thenAccessDenied() {
            Organization managerOrg = UserTestFactory.organization().name("Org Gestor").build();
            Organization otherOrg = UserTestFactory.organization().name("Org Alheia").build();
            User managerUser = UserTestFactory.user().manager().build();
            Manager manager = UserTestFactory.managerProfile().user(managerUser).organization(managerOrg).build();
            Community community = UserTestFactory.community().organization(otherOrg).build();
            TechnicalAssistanceRegisterDTO dto = dto(UUID.randomUUID(), community.getId());
            when(managerRepository.findByUserId(managerUser.getId())).thenReturn(Optional.of(manager));
            when(communityRepository.findById(community.getId())).thenReturn(Optional.of(community));

            assertThatThrownBy(() -> assignCommunityToTechnicianUseCase.assign(dto, managerUser))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("O gestor só pode vincular comunidades da sua organização.");

            verify(assistanceRepository, never()).save(any());
        }

        @Test
        @DisplayName("Given TECHNICIAN When assign Then AccessDeniedException")
        void givenTechnician_whenAssign_thenAccessDenied() {
            User technician = UserTestFactory.user().technician().build();
            TechnicalAssistanceRegisterDTO dto = dto(UUID.randomUUID(), UUID.randomUUID());

            assertThatThrownBy(() -> assignCommunityToTechnicianUseCase.assign(dto, technician))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Apenas administradores ou gestores podem criar assistência técnica.");

            verifyNoInteractions(communityRepository, technicianRepository, assistanceRepository, managerRepository);
        }

        @Test
        @DisplayName("Given FARMER When assign Then AccessDeniedException")
        void givenFarmer_whenAssign_thenAccessDenied() {
            User farmer = UserTestFactory.user().farmer().build();
            TechnicalAssistanceRegisterDTO dto = dto(UUID.randomUUID(), UUID.randomUUID());

            assertThatThrownBy(() -> assignCommunityToTechnicianUseCase.assign(dto, farmer))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Apenas administradores ou gestores podem criar assistência técnica.");

            verifyNoInteractions(communityRepository, technicianRepository, assistanceRepository, managerRepository);
        }

        @Test
        @DisplayName("Given assistência ativa existente When assign Then IllegalArgumentException")
        void givenActiveAssistanceExists_whenAssign_thenThrows() {
            User admin = UserTestFactory.user().admin().build();
            Technician technician = UserTestFactory.technicianProfile().build();
            Community community = UserTestFactory.community().build();
            TechnicalAssistanceRegisterDTO dto = dto(technician.getId(), community.getId());
            when(communityRepository.findById(community.getId())).thenReturn(Optional.of(community));
            when(technicianRepository.findById(technician.getId())).thenReturn(Optional.of(technician));
            when(assistanceRepository.findActiveByTechnicianAndCommunity(technician.getId(), community.getId()))
                    .thenReturn(Optional.of(UserTestFactory.technicalAssistance().technician(technician).community(community).build()));

            assertThatThrownBy(() -> assignCommunityToTechnicianUseCase.assign(dto, admin))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Já existe uma assistência ativa entre este técnico e comunidade.");

            verify(assistanceRepository, never()).save(any());
        }

        @Test
        @DisplayName("Given comunidade inexistente When assign Then EntityNotFoundException")
        void givenMissingCommunity_whenAssign_thenEntityNotFound() {
            User admin = UserTestFactory.user().admin().build();
            TechnicalAssistanceRegisterDTO dto = dto(UUID.randomUUID(), UUID.randomUUID());
            when(communityRepository.findById(dto.communityId())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> assignCommunityToTechnicianUseCase.assign(dto, admin))
                    .isInstanceOf(com.ufal.smartagro.domain.exception.EntityNotFoundException.class)
                    .hasMessage("Comunidade não encontrada.");
        }

        @Test
        @DisplayName("Given técnico inexistente When assign Then EntityNotFoundException")
        void givenMissingTechnician_whenAssign_thenEntityNotFound() {
            User admin = UserTestFactory.user().admin().build();
            Community community = UserTestFactory.community().build();
            TechnicalAssistanceRegisterDTO dto = dto(UUID.randomUUID(), community.getId());
            when(communityRepository.findById(community.getId())).thenReturn(Optional.of(community));
            when(technicianRepository.findById(dto.technicianId())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> assignCommunityToTechnicianUseCase.assign(dto, admin))
                    .isInstanceOf(com.ufal.smartagro.domain.exception.EntityNotFoundException.class)
                    .hasMessage("Técnico não encontrado.");
        }

        @Test
        @DisplayName("Given DTO sem técnico When assign Then IllegalArgumentException")
        void givenMissingTechnicianIdInDto_whenAssign_thenThrows() {
            User admin = UserTestFactory.user().admin().build();
            TechnicalAssistanceRegisterDTO dto = new TechnicalAssistanceRegisterDTO(null, UUID.randomUUID(), LocalDateTime.now());

            assertThatThrownBy(() -> assignCommunityToTechnicianUseCase.assign(dto, admin))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("O ID do técnico é obrigatório.");
        }

        @Test
        @DisplayName("Given technicianId divergente entre path e body When assign Then IllegalArgumentException")
        void givenMismatchedTechnicianId_whenAssign_thenThrows() {
            User admin = UserTestFactory.user().admin().build();
            UUID pathTechnicianId = UUID.randomUUID();
            UUID bodyTechnicianId = UUID.randomUUID();
            TechnicalAssistanceRegisterDTO dto = dto(bodyTechnicianId, UUID.randomUUID());

            assertThatThrownBy(() -> assignCommunityToTechnicianUseCase.assign(pathTechnicianId, dto, admin))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("O ID do técnico informado na URL não coincide com o do corpo da requisição.");

            verifyNoInteractions(communityRepository, technicianRepository, assistanceRepository, managerRepository);
        }

        @Test
        @DisplayName("Given DTO nulo When assign Then IllegalArgumentException")
        void givenNullDto_whenAssign_thenThrows() {
            User admin = UserTestFactory.user().admin().build();

            assertThatThrownBy(() -> assignCommunityToTechnicianUseCase.assign(UUID.randomUUID(), null, admin))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Dados de assistência técnica são obrigatórios.");
        }

        @Test
        @DisplayName("Given técnico excluído (soft-delete) When assign Then EntityNotFoundException")
        void givenSoftDeletedTechnician_whenAssign_thenEntityNotFound() {
            User admin = UserTestFactory.user().admin().build();
            Community community = UserTestFactory.community().build();
            UUID technicianId = UUID.randomUUID();
            Technician deletedTechnician = new Technician(
                    technicianId, null, "CRBio-1", "Agro",
                    LocalDateTime.now().minusDays(5), null, LocalDateTime.now().minusDays(1)
            );
            TechnicalAssistanceRegisterDTO dto = dto(technicianId, community.getId());
            when(communityRepository.findById(community.getId())).thenReturn(Optional.of(community));
            when(technicianRepository.findById(technicianId)).thenReturn(Optional.of(deletedTechnician));

            assertThatThrownBy(() -> assignCommunityToTechnicianUseCase.assign(dto, admin))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Técnico não encontrado.");
        }

        @Test
        @DisplayName("Given comunidade excluída (soft-delete) When assign Then EntityNotFoundException")
        void givenSoftDeletedCommunity_whenAssign_thenEntityNotFound() {
            User admin = UserTestFactory.user().admin().build();
            UUID communityId = UUID.randomUUID();
            Community deletedCommunity = new Community(
                    communityId, "Comunidade Antiga", null,
                    LocalDateTime.now().minusDays(10), null, LocalDateTime.now().minusDays(2)
            );
            TechnicalAssistanceRegisterDTO dto = dto(UUID.randomUUID(), communityId);
            when(communityRepository.findById(communityId)).thenReturn(Optional.of(deletedCommunity));

            assertThatThrownBy(() -> assignCommunityToTechnicianUseCase.assign(dto, admin))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Comunidade não encontrada.");
        }

        @Test
        @DisplayName("Given null user When assign Then AccessDeniedException")
        void givenNullUser_whenAssign_thenAccessDenied() {
            TechnicalAssistanceRegisterDTO dto = dto(UUID.randomUUID(), UUID.randomUUID());

            assertThatThrownBy(() -> assignCommunityToTechnicianUseCase.assign(dto, null))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Apenas administradores ou gestores podem criar assistência técnica.");
        }
    }

    private TechnicalAssistanceRegisterDTO dto(UUID technicianId, UUID communityId) {
        return new TechnicalAssistanceRegisterDTO(technicianId, communityId, LocalDateTime.of(2026, 1, 10, 8, 0));
    }
}
