package com.ufal.smartagro.application.service.technician;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RemoveFarmerFromTechnicianUseCaseTest {

    @Mock
    private TechnicianRepository technicianRepository;
    @Mock
    private FarmerRepository farmerRepository;
    @Mock
    private TechnicalAssistanceRepository assistanceRepository;
    @Mock
    private ManagerRepository managerRepository;

    @InjectMocks
    private RemoveFarmerFromTechnicianUseCase removeFarmerFromTechnicianUseCase;

    @Test
    @DisplayName("Given ADMIN When remove Then encerra assistência")
    void givenAdmin_whenRemove_thenEndsAssistance() {
        User admin = UserTestFactory.user().admin().build();
        TechnicalAssistance assistance = UserTestFactory.technicalAssistance().build();
        when(assistanceRepository.findById(assistance.getId())).thenReturn(Optional.of(assistance));
        when(assistanceRepository.save(any(TechnicalAssistance.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TechnicalAssistance result = removeFarmerFromTechnicianUseCase.remove(assistance.getId(), admin);

        assertThat(result.getEndDate()).isNotNull();
        assertThat(result.getId()).isEqualTo(assistance.getId());
    }

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
        TechnicalAssistance assistance = UserTestFactory.technicalAssistance().farmer(farmer).build();
        when(assistanceRepository.findById(assistance.getId())).thenReturn(Optional.of(assistance));
        when(managerRepository.findByUserId(managerUser.getId())).thenReturn(Optional.of(manager));

        assertThatThrownBy(() -> removeFarmerFromTechnicianUseCase.remove(assistance.getId(), managerUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("O gestor só pode encerrar assistência de agricultores da sua organização.");

        verify(assistanceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Given FARMER When encerra assistência de terceiro Then AccessDeniedException")
    void givenFarmer_whenRemoveSomeoneElse_thenAccessDenied() {
        User farmerUser = UserTestFactory.user().farmer().build();
        Farmer other = UserTestFactory.farmerProfile()
                .user(UserTestFactory.user().farmer().email("outro@smartagro.test").build())
                .build();
        TechnicalAssistance assistance = UserTestFactory.technicalAssistance().farmer(other).build();
        when(assistanceRepository.findById(assistance.getId())).thenReturn(Optional.of(assistance));

        assertThatThrownBy(() -> removeFarmerFromTechnicianUseCase.remove(assistance.getId(), farmerUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Agricultor só pode encerrar sua própria assistência.");
    }

    @Test
    @DisplayName("Given TECHNICIAN When encerra assistência de outro técnico Then AccessDeniedException")
    void givenTechnician_whenRemoveOtherTechnicianAssistance_thenAccessDenied() {
        User techUser = UserTestFactory.user().technician().build();
        Technician otherTech = UserTestFactory.technicianProfile()
                .user(UserTestFactory.user().technician().email("outro.tech@smartagro.test").build())
                .build();
        TechnicalAssistance assistance = UserTestFactory.technicalAssistance().technician(otherTech).build();
        when(assistanceRepository.findById(assistance.getId())).thenReturn(Optional.of(assistance));

        assertThatThrownBy(() -> removeFarmerFromTechnicianUseCase.remove(assistance.getId(), techUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Técnico só pode encerrar sua própria assistência.");
    }

    @Test
    @DisplayName("Given assistência inexistente When remove Then EntityNotFoundException")
    void givenMissingAssistance_whenRemove_thenEntityNotFound() {
        User admin = UserTestFactory.user().admin().build();
        UUID missingId = UUID.randomUUID();
        when(assistanceRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> removeFarmerFromTechnicianUseCase.remove(missingId, admin))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Assistência não encontrada.");
    }
}
