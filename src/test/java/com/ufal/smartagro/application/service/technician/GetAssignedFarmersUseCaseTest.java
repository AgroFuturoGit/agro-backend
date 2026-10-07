package com.ufal.smartagro.application.service.technician;

import com.ufal.smartagro.domain.model.Community;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
import com.ufal.smartagro.testsupport.UserTestFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAssignedFarmersUseCaseTest {

    @Mock
    private GetAssignedCommunitiesUseCase getAssignedCommunitiesUseCase;

    @Mock
    private FarmerRepository farmerRepository;

    @InjectMocks
    private GetAssignedFarmersUseCase getAssignedFarmersUseCase;

    @Test
    @DisplayName("Given technicianId When getAssignedFarmers Then returns flattened list of farmers from assigned communities")
    void givenTechnicianId_whenGetAssignedFarmers_thenFlattensFarmers() {
        User loggedUser = UserTestFactory.user().technician().build();
        UUID technicianId = UUID.randomUUID();

        Community community1 = UserTestFactory.community().name("Comunidade A").build();
        Community community2 = UserTestFactory.community().name("Comunidade B").build();

        Farmer farmer1 = UserTestFactory.farmerProfile().community(community1).build();
        Farmer farmer2 = UserTestFactory.farmerProfile().community(community1).build();
        Farmer farmer3 = UserTestFactory.farmerProfile().community(community2).build();

        when(getAssignedCommunitiesUseCase.getAssignedCommunities(technicianId, loggedUser))
                .thenReturn(List.of(community1, community2));
        when(farmerRepository.findAllByCommunityId(community1.getId())).thenReturn(List.of(farmer1, farmer2));
        when(farmerRepository.findAllByCommunityId(community2.getId())).thenReturn(List.of(farmer3));

        List<Farmer> result = getAssignedFarmersUseCase.getAssignedFarmers(technicianId, loggedUser);

        assertThat(result).containsExactly(farmer1, farmer2, farmer3);
        verify(getAssignedCommunitiesUseCase).getAssignedCommunities(technicianId, loggedUser);
        verify(farmerRepository).findAllByCommunityId(community1.getId());
        verify(farmerRepository).findAllByCommunityId(community2.getId());
    }

    @Test
    @DisplayName("Given loggedUser When getAssignedFarmers Then returns flattened list of farmers from assigned communities")
    void givenLoggedUser_whenGetAssignedFarmers_thenFlattensFarmers() {
        User loggedUser = UserTestFactory.user().technician().build();

        Community community = UserTestFactory.community().name("Comunidade A").build();
        Farmer farmer = UserTestFactory.farmerProfile().community(community).build();

        when(getAssignedCommunitiesUseCase.getAssignedCommunities(loggedUser))
                .thenReturn(List.of(community));
        when(farmerRepository.findAllByCommunityId(community.getId())).thenReturn(List.of(farmer));

        List<Farmer> result = getAssignedFarmersUseCase.getAssignedFarmers(loggedUser);

        assertThat(result).containsExactly(farmer);
        verify(getAssignedCommunitiesUseCase).getAssignedCommunities(loggedUser);
        verify(farmerRepository).findAllByCommunityId(community.getId());
    }
}
