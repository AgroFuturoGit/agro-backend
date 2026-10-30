package com.ufal.smartagro.application.service.farmer;

import com.ufal.smartagro.domain.model.Community;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.Manager;
import com.ufal.smartagro.domain.model.Organization;
import com.ufal.smartagro.domain.model.Technician;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.CommunityRepository;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
import com.ufal.smartagro.domain.port.out.ManagerRepository;
import com.ufal.smartagro.domain.port.out.TechnicalAssistanceRepository;
import com.ufal.smartagro.domain.port.out.TechnicianRepository;
import com.ufal.smartagro.testsupport.UserTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindAllFarmersUseCaseTest {

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

    @InjectMocks
    private FindAllFarmersUseCase useCase;

    private User managerUser;
    private Manager manager;
    private Organization organization;

    @BeforeEach
    void setUp() {
        organization = UserTestFactory.organization().name("Cooperativa Alagoas").build();
        managerUser = UserTestFactory.user().manager().build();
        manager = UserTestFactory.managerProfile().user(managerUser).organization(organization).build();
    }

    @Test
    @DisplayName("Given MANAGER sem communityId When findAll Then busca direto por organização no banco sem carregar tudo")
    void givenManagerWithoutCommunity_whenFindAll_thenQueriesByOrganizationDirectly() {
        Farmer farmer1 = UserTestFactory.farmerProfile().build();
        Farmer farmer2 = UserTestFactory.farmerProfile().build();

        when(managerRepository.findByUserId(managerUser.getId())).thenReturn(Optional.of(manager));
        when(farmerRepository.findAllByOrganizationId(organization.getId())).thenReturn(List.of(farmer1, farmer2));

        List<Farmer> result = useCase.findAll(null, managerUser);

        assertThat(result).hasSize(2).containsExactly(farmer1, farmer2);
        verify(farmerRepository, never()).findAll();
        verify(farmerRepository).findAllByOrganizationId(organization.getId());
    }

    @Test
    @DisplayName("Given TECHNICIAN sem communityId When findAll Then busca comunidades assistidas com findAllByCommunityIdIn")
    void givenTechnicianWithoutCommunity_whenFindAll_thenQueriesAssistedCommunitiesDirectly() {
        User techUser = UserTestFactory.user().technician().build();
        Technician technician = UserTestFactory.technicianProfile().user(techUser).build();
        Community commA = UserTestFactory.community().name("Comunidade A").build();
        Farmer farmerA = UserTestFactory.farmerProfile().community(commA).build();

        when(technicianRepository.findByUserId(techUser.getId())).thenReturn(Optional.of(technician));
        when(technicalAssistanceRepository.findByTechnicianId(technician.getId())).thenReturn(List.of(
                UserTestFactory.technicalAssistance().technician(technician).community(commA).build()
        ));
        when(farmerRepository.findAllByCommunityIdIn(Set.of(commA.getId()))).thenReturn(List.of(farmerA));

        List<Farmer> result = useCase.findAll(null, techUser);

        assertThat(result).hasSize(1).containsExactly(farmerA);
        verify(farmerRepository, never()).findAll();
        verify(farmerRepository).findAllByCommunityIdIn(Set.of(commA.getId()));
    }

    @Test
    @DisplayName("Given ADMIN When findAll Then devolve todos os agricultores")
    void givenAdmin_whenFindAll_thenReturnsAll() {
        User adminUser = UserTestFactory.user().admin().build();
        Farmer farmer = UserTestFactory.farmerProfile().build();
        when(farmerRepository.findAll()).thenReturn(List.of(farmer));

        List<Farmer> result = useCase.findAll(null, adminUser);

        assertThat(result).containsExactly(farmer);
        verify(farmerRepository).findAll();
    }
}
