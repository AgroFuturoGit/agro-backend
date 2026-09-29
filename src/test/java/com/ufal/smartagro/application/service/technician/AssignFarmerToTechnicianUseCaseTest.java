package com.ufal.smartagro.application.service.technician;

import com.ufal.smartagro.adapters.in.web.dto.technicalassistance.TechnicalAssistanceRegisterDTO;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.Manager;
import com.ufal.smartagro.domain.model.Organization;
import com.ufal.smartagro.domain.model.TechnicalAssistance;
import com.ufal.smartagro.domain.model.Technician;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
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
class AssignFarmerToTechnicianUseCaseTest {

    @Mock
    private TechnicianRepository technicianRepository;
    @Mock
    private FarmerRepository farmerRepository;
    @Mock
    private TechnicalAssistanceRepository assistanceRepository;
    @Mock
    private ManagerRepository managerRepository;

    @InjectMocks
    private AssignFarmerToTechnicianUseCase assignFarmerToTechnicianUseCase;

    @Nested
    @DisplayName("Happy path")
    class HappyPath {

        @Test
        @DisplayName("Given ADMIN When assign Then persiste assistência")
        void givenAdmin_whenAssign_thenPersists() {
            User admin = UserTestFactory.user().admin().build();
            Technician technician = UserTestFactory.technicianProfile().build();
            Farmer farmer = UserTestFactory.farmerProfile().build();
            TechnicalAssistanceRegisterDTO dto = dto(technician.getId(), farmer.getId());
            when(farmerRepository.findById(farmer.getId())).thenReturn(Optional.of(farmer));
            when(technicianRepository.findById(technician.getId())).thenReturn(Optional.of(technician));
            when(assistanceRepository.findActiveByTechnicianAndFarmer(technician.getId(), farmer.getId()))
                    .thenReturn(Optional.empty());
            when(assistanceRepository.save(any(TechnicalAssistance.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            TechnicalAssistance result = assignFarmerToTechnicianUseCase.assign(dto, admin);

            assertThat(result.getTechnician()).isEqualTo(technician);
            assertThat(result.getFarmer()).isEqualTo(farmer);
            assertThat(result.getEndDate()).isNull();
        }

        @Test
        @DisplayName("Given MANAGER When farmer da própria org Then persiste")
        void givenManager_whenFarmerInOwnOrg_thenPersists() {
            Organization organization = UserTestFactory.organization().build();
            User managerUser = UserTestFactory.user().manager().build();
            Manager manager = UserTestFactory.managerProfile().user(managerUser).organization(organization).build();
            Farmer farmer = UserTestFactory.farmerProfile()
                    .community(UserTestFactory.community().organization(organization).build())
                    .build();
            Technician technician = UserTestFactory.technicianProfile().build();
            TechnicalAssistanceRegisterDTO dto = dto(technician.getId(), farmer.getId());
            when(managerRepository.findByUserId(managerUser.getId())).thenReturn(Optional.of(manager));
            when(farmerRepository.findById(farmer.getId())).thenReturn(Optional.of(farmer));
            when(technicianRepository.findById(technician.getId())).thenReturn(Optional.of(technician));
            when(assistanceRepository.findActiveByTechnicianAndFarmer(technician.getId(), farmer.getId()))
                    .thenReturn(Optional.empty());
            when(assistanceRepository.save(any(TechnicalAssistance.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            TechnicalAssistance result = assignFarmerToTechnicianUseCase.assign(dto, managerUser);

            assertThat(result.getFarmer().getCommunity().getOrganization().getId()).isEqualTo(organization.getId());
        }

        @Test
        @DisplayName("Given FARMER When vincula a si mesmo Then persiste")
        void givenFarmer_whenSelfLink_thenPersists() {
            User farmerUser = UserTestFactory.user().farmer().build();
            Farmer farmer = UserTestFactory.farmerProfile().user(farmerUser).build();
            Technician technician = UserTestFactory.technicianProfile().build();
            TechnicalAssistanceRegisterDTO dto = dto(technician.getId(), farmer.getId());
            when(farmerRepository.findById(farmer.getId())).thenReturn(Optional.of(farmer));
            when(technicianRepository.findById(technician.getId())).thenReturn(Optional.of(technician));
            when(assistanceRepository.findActiveByTechnicianAndFarmer(technician.getId(), farmer.getId()))
                    .thenReturn(Optional.empty());
            when(assistanceRepository.save(any(TechnicalAssistance.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            TechnicalAssistance result = assignFarmerToTechnicianUseCase.assign(dto, farmerUser);

            assertThat(result.getFarmer().getUser().getId()).isEqualTo(farmerUser.getId());
        }
    }

    @Nested
    @DisplayName("Negativos — org, self-link e role")
    class Negatives {

        @Test
        @DisplayName("Given MANAGER When farmer de outra org Then AccessDeniedException")
        void givenManager_whenFarmerInAnotherOrg_thenAccessDenied() {
            Organization managerOrg = UserTestFactory.organization().name("Org Gestor").build();
            Organization otherOrg = UserTestFactory.organization().name("Org Alheia").build();
            User managerUser = UserTestFactory.user().manager().build();
            Manager manager = UserTestFactory.managerProfile().user(managerUser).organization(managerOrg).build();
            Farmer farmer = UserTestFactory.farmerProfile()
                    .community(UserTestFactory.community().organization(otherOrg).build())
                    .build();
            TechnicalAssistanceRegisterDTO dto = dto(UUID.randomUUID(), farmer.getId());
            when(managerRepository.findByUserId(managerUser.getId())).thenReturn(Optional.of(manager));
            when(farmerRepository.findById(farmer.getId())).thenReturn(Optional.of(farmer));

            assertThatThrownBy(() -> assignFarmerToTechnicianUseCase.assign(dto, managerUser))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("O gestor só pode vincular agricultores da sua organização.");

            verify(assistanceRepository, never()).save(any());
        }

        @Test
        @DisplayName("Given FARMER When vincula outro agricultor Then AccessDeniedException")
        void givenFarmer_whenLinkSomeoneElse_thenAccessDenied() {
            User farmerUser = UserTestFactory.user().farmer().build();
            Farmer other = UserTestFactory.farmerProfile()
                    .user(UserTestFactory.user().farmer().email("outro@smartagro.test").build())
                    .build();
            TechnicalAssistanceRegisterDTO dto = dto(UUID.randomUUID(), other.getId());
            when(farmerRepository.findById(other.getId())).thenReturn(Optional.of(other));

            assertThatThrownBy(() -> assignFarmerToTechnicianUseCase.assign(dto, farmerUser))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Agricultor só pode vincular a si mesmo.");

            verify(assistanceRepository, never()).save(any());
        }

        @Test
        @DisplayName("Given TECHNICIAN When assign Then AccessDeniedException")
        void givenTechnician_whenAssign_thenAccessDenied() {
            User technician = UserTestFactory.user().technician().build();
            TechnicalAssistanceRegisterDTO dto = dto(UUID.randomUUID(), UUID.randomUUID());

            assertThatThrownBy(() -> assignFarmerToTechnicianUseCase.assign(dto, technician))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Apenas administradores, gestores ou agricultores podem criar assistência técnica.");

            verifyNoInteractions(farmerRepository, technicianRepository, assistanceRepository, managerRepository);
        }
    }

    private TechnicalAssistanceRegisterDTO dto(UUID technicianId, UUID farmerId) {
        return new TechnicalAssistanceRegisterDTO(technicianId, farmerId, LocalDateTime.of(2026, 1, 10, 8, 0));
    }
}
