package com.ufal.smartagro.application.service.technician;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.Manager;
import com.ufal.smartagro.domain.model.Organization;
import com.ufal.smartagro.domain.model.Technician;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.ManagerRepository;
import com.ufal.smartagro.domain.port.out.TechnicianRepository;
import com.ufal.smartagro.testsupport.UserTestFactory;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindTechnicianByIdUseCaseTest {

    @Mock
    private TechnicianRepository technicianRepository;

    @Mock
    private ManagerRepository managerRepository;

    @InjectMocks
    private FindTechnicianByIdUseCase findTechnicianByIdUseCase;

    @Test
    @DisplayName("Given ADMIN When findById Then retorna técnico")
    void givenAdmin_whenFindById_thenReturnsTechnician() {
        User admin = UserTestFactory.user().admin().build();
        Technician technician = UserTestFactory.technicianProfile().build();

        when(technicianRepository.findById(technician.getId())).thenReturn(Optional.of(technician));

        Technician result = findTechnicianByIdUseCase.findById(technician.getId(), admin);

        assertThat(result).isEqualTo(technician);
    }

    @Test
    @DisplayName("Given MANAGER When técnico da mesma organização Then retorna técnico")
    void givenManager_whenTechnicianInSameOrg_thenReturnsTechnician() {
        Organization org = UserTestFactory.organization().build();
        User managerUser = UserTestFactory.user().manager().build();
        Manager manager = UserTestFactory.managerProfile().user(managerUser).organization(org).build();
        Technician technician = UserTestFactory.technicianProfile().build();

        when(technicianRepository.findById(technician.getId())).thenReturn(Optional.of(technician));
        when(managerRepository.findByUserId(managerUser.getId())).thenReturn(Optional.of(manager));
        when(technicianRepository.existsByIdAndOrganizationId(technician.getId(), org.getId())).thenReturn(true);

        Technician result = findTechnicianByIdUseCase.findById(technician.getId(), managerUser);

        assertThat(result).isEqualTo(technician);
    }

    @Test
    @DisplayName("Given MANAGER When técnico de outra organização Then AccessDeniedException")
    void givenManager_whenTechnicianNotInSameOrg_thenThrowsAccessDeniedException() {
        Organization org = UserTestFactory.organization().build();
        User managerUser = UserTestFactory.user().manager().build();
        Manager manager = UserTestFactory.managerProfile().user(managerUser).organization(org).build();
        Technician technician = UserTestFactory.technicianProfile().build();

        when(technicianRepository.findById(technician.getId())).thenReturn(Optional.of(technician));
        when(managerRepository.findByUserId(managerUser.getId())).thenReturn(Optional.of(manager));
        when(technicianRepository.existsByIdAndOrganizationId(technician.getId(), org.getId())).thenReturn(false);

        assertThatThrownBy(() -> findTechnicianByIdUseCase.findById(technician.getId(), managerUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("O gestor só tem acesso aos técnicos da sua própria organização.");
    }

    @Test
    @DisplayName("Given FARMER When findById Then AccessDeniedException")
    void givenFarmer_whenFindById_thenThrowsAccessDeniedException() {
        User farmer = UserTestFactory.user().farmer().build();
        Technician technician = UserTestFactory.technicianProfile().build();

        when(technicianRepository.findById(technician.getId())).thenReturn(Optional.of(technician));

        assertThatThrownBy(() -> findTechnicianByIdUseCase.findById(technician.getId(), farmer))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Apenas administradores e gestores podem consultar técnicos por ID.");
    }

    @Test
    @DisplayName("Given null user When findById Then AccessDeniedException")
    void givenNullUser_whenFindById_thenThrowsAccessDeniedException() {
        UUID id = UUID.randomUUID();

        assertThatThrownBy(() -> findTechnicianByIdUseCase.findById(id, null))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Apenas administradores e gestores podem consultar técnicos por ID.");
    }

    @Test
    @DisplayName("Given técnico inexistente When findById Then EntityNotFoundException")
    void givenMissingTechnician_whenFindById_thenThrowsEntityNotFoundException() {
        User admin = UserTestFactory.user().admin().build();
        UUID id = UUID.randomUUID();

        when(technicianRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> findTechnicianByIdUseCase.findById(id, admin))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Técnico não encontrado.");
    }

    @Test
    @DisplayName("Given técnico excluído (soft-delete) When findById Then EntityNotFoundException")
    void givenSoftDeletedTechnician_whenFindById_thenThrowsEntityNotFoundException() {
        User admin = UserTestFactory.user().admin().build();
        UUID id = UUID.randomUUID();
        Technician deletedTechnician = new Technician(
                id, null, "CRBio-1", "Agro",
                LocalDateTime.now().minusDays(5), null, LocalDateTime.now().minusDays(1)
        );

        when(technicianRepository.findById(id)).thenReturn(Optional.of(deletedTechnician));

        assertThatThrownBy(() -> findTechnicianByIdUseCase.findById(id, admin))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Técnico não encontrado.");
    }
}
