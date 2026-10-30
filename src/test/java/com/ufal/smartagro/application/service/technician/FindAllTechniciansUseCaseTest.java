package com.ufal.smartagro.application.service.technician;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindAllTechniciansUseCaseTest {

    @Mock
    private TechnicianRepository technicianRepository;

    @Mock
    private ManagerRepository managerRepository;

    @InjectMocks
    private FindAllTechniciansUseCase findAllTechniciansUseCase;

    @Test
    @DisplayName("Given ADMIN When findAll Then retorna lista filtrando excluídos")
    void givenAdmin_whenFindAll_thenReturnsOnlyActiveTechnicians() {
        User admin = UserTestFactory.user().admin().build();
        Technician activeTech = UserTestFactory.technicianProfile().build();
        Technician deletedTech = new Technician(
                UUID.randomUUID(), null, "CRBio-9", "Agro",
                LocalDateTime.now().minusDays(5), null, LocalDateTime.now().minusDays(1)
        );

        when(technicianRepository.findAll()).thenReturn(List.of(activeTech, deletedTech));

        List<Technician> result = findAllTechniciansUseCase.findAll(admin);

        assertThat(result).containsExactly(activeTech);
    }

    @Test
    @DisplayName("Given MANAGER When findAll Then retorna técnicos da sua organização")
    void givenManager_whenFindAll_thenReturnsOrgTechnicians() {
        Organization org = UserTestFactory.organization().build();
        User managerUser = UserTestFactory.user().manager().build();
        Manager manager = UserTestFactory.managerProfile().user(managerUser).organization(org).build();
        Technician tech = UserTestFactory.technicianProfile().build();

        when(managerRepository.findByUserId(managerUser.getId())).thenReturn(Optional.of(manager));
        when(technicianRepository.findAllByOrganizationId(org.getId())).thenReturn(List.of(tech));

        List<Technician> result = findAllTechniciansUseCase.findAll(managerUser);

        assertThat(result).containsExactly(tech);
    }

    @Test
    @DisplayName("Given MANAGER sem organização When findAll Then retorna lista vazia")
    void givenManagerWithoutOrg_whenFindAll_thenReturnsEmptyList() {
        User managerUser = UserTestFactory.user().manager().build();
        Manager manager = UserTestFactory.managerProfile().user(managerUser).organization(null).build();

        when(managerRepository.findByUserId(managerUser.getId())).thenReturn(Optional.of(manager));

        List<Technician> result = findAllTechniciansUseCase.findAll(managerUser);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Given FARMER When findAll Then AccessDeniedException")
    void givenFarmer_whenFindAll_thenThrowsAccessDeniedException() {
        User farmer = UserTestFactory.user().farmer().build();

        assertThatThrownBy(() -> findAllTechniciansUseCase.findAll(farmer))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Apenas administradores e gestores podem listar técnicos.");
    }

    @Test
    @DisplayName("Given null user When findAll Then AccessDeniedException")
    void givenNullUser_whenFindAll_thenThrowsAccessDeniedException() {
        assertThatThrownBy(() -> findAllTechniciansUseCase.findAll(null))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Apenas administradores e gestores podem listar técnicos.");
    }
}
