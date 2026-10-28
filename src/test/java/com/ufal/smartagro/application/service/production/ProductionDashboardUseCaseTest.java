package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.adapters.in.web.dto.production.ProductionDashboardDTO;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.model.Community;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.Manager;
import com.ufal.smartagro.domain.model.Organization;
import com.ufal.smartagro.domain.model.ProductionExecution;
import com.ufal.smartagro.domain.model.ProductionPlan;
import com.ufal.smartagro.domain.model.Technician;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.ExecutionStatus;
import com.ufal.smartagro.domain.model.enums.PlanStatus;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
import com.ufal.smartagro.domain.port.out.ManagerRepository;
import com.ufal.smartagro.domain.port.out.ProductionExecutionRepository;
import com.ufal.smartagro.domain.port.out.ProductionPlanRepository;
import com.ufal.smartagro.domain.port.out.TechnicalAssistanceRepository;
import com.ufal.smartagro.domain.port.out.TechnicianRepository;
import com.ufal.smartagro.testsupport.UserTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductionDashboardUseCaseTest {

    @Mock
    private FarmerRepository farmerRepository;
    @Mock
    private ManagerRepository managerRepository;
    @Mock
    private TechnicianRepository technicianRepository;
    @Mock
    private TechnicalAssistanceRepository assistanceRepository;
    @Mock
    private ProductionPlanRepository planRepository;
    @Mock
    private ProductionExecutionRepository executionRepository;

    private ProductionDashboardUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ProductionDashboardUseCase(farmerRepository, managerRepository, technicianRepository,
                assistanceRepository, planRepository, executionRepository);
    }

    @Nested
    @DisplayName("FARMER — visão restrita aos próprios dados")
    class FarmerScope {

        @Test
        @DisplayName("Given FARMER When dashboard Then totaliza só o próprio agricultor e só execuções VALIDATED")
        void givenFarmer_whenDashboard_thenOnlyOwnDataAndValidatedExecutions() {
            User farmerUser = UserTestFactory.user().farmer().build();
            Farmer farmer = UserTestFactory.farmerProfile().user(farmerUser).build();
            ProductionPlan plan = plan(farmer, "10", "10");
            when(farmerRepository.findByUserId(farmerUser.getId())).thenReturn(Optional.of(farmer));
            when(planRepository.findAllByFarmerId(farmer.getId())).thenReturn(List.of(plan));
            when(executionRepository.findAllByProductionPlanId(plan.getId())).thenReturn(List.of(
                    execution(plan, "40", ExecutionStatus.VALIDATED),
                    execution(plan, "30", ExecutionStatus.PENDING),
                    execution(plan, "20", ExecutionStatus.REJECTED)));

            ProductionDashboardDTO result = useCase.getDashboard(farmerUser);

            assertThat(result.farmerCount()).isEqualTo(1);
            assertThat(result.planCount()).isEqualTo(1);
            assertThat(result.expectedYield()).isEqualByComparingTo("100");
            assertThat(result.actualYield()).isEqualByComparingTo("40");
            assertThat(result.difference()).isEqualByComparingTo("-60");
            assertThat(result.percentageRealized()).isEqualByComparingTo("40.00");
            verify(farmerRepository, never()).findAll();
            verify(farmerRepository, never()).findAllByCommunityId(any());
        }

        @Test
        @DisplayName("Given FARMER sem perfil de agricultor When dashboard Then AccessDeniedException")
        void givenFarmerWithoutProfile_whenDashboard_thenAccessDenied() {
            User farmerUser = UserTestFactory.user().farmer().build();
            when(farmerRepository.findByUserId(farmerUser.getId())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.getDashboard(farmerUser))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Agricultor não encontrado.");
        }
    }

    @Nested
    @DisplayName("TECHNICIAN — agregação das comunidades com assistência ativa")
    class TechnicianScope {

        @Test
        @DisplayName("Given TECHNICIAN When dashboard Then agrega só comunidades ativas e ignora assistência encerrada")
        void givenTechnician_whenDashboard_thenOnlyActiveAssistanceCommunities() {
            User techUser = UserTestFactory.user().technician().build();
            Technician technician = UserTestFactory.technicianProfile().user(techUser).build();
            Community assisted = UserTestFactory.community().name("Atendida").build();
            Community ended = UserTestFactory.community().name("Encerrada").build();
            Farmer first = UserTestFactory.farmerProfile().community(assisted).build();
            Farmer second = UserTestFactory.farmerProfile().community(assisted).build();
            ProductionPlan plan = plan(first, "10", "10");
            when(technicianRepository.findByUserId(techUser.getId())).thenReturn(Optional.of(technician));
            when(assistanceRepository.findByTechnicianId(technician.getId())).thenReturn(List.of(
                    UserTestFactory.technicalAssistance().technician(technician).community(assisted).build(),
                    UserTestFactory.technicalAssistance().technician(technician).community(ended).ended().build()));
            when(farmerRepository.findAllByCommunityId(assisted.getId())).thenReturn(List.of(first, second));
            when(planRepository.findAllByFarmerId(first.getId())).thenReturn(List.of(plan));
            when(executionRepository.findAllByProductionPlanId(plan.getId()))
                    .thenReturn(List.of(execution(plan, "50", ExecutionStatus.VALIDATED)));

            ProductionDashboardDTO result = useCase.getDashboard(techUser);

            assertThat(result.farmerCount()).isEqualTo(2);
            assertThat(result.planCount()).isEqualTo(1);
            assertThat(result.communities()).hasSize(1);
            assertThat(result.communities().get(0).communityId()).isEqualTo(assisted.getId());
            assertThat(result.communities().get(0).farmerCount()).isEqualTo(2);
            assertThat(result.percentageRealized()).isEqualByComparingTo("50.00");
            verify(farmerRepository, never()).findAllByCommunityId(ended.getId());
            verify(farmerRepository, never()).findAll();
        }

        @Test
        @DisplayName("Given TECHNICIAN sem comunidades ativas When dashboard Then retorna painel vazio")
        void givenTechnicianWithoutActiveAssistance_whenDashboard_thenEmptyDashboard() {
            User techUser = UserTestFactory.user().technician().build();
            Technician technician = UserTestFactory.technicianProfile().user(techUser).build();
            Community ended = UserTestFactory.community().build();
            when(technicianRepository.findByUserId(techUser.getId())).thenReturn(Optional.of(technician));
            when(assistanceRepository.findByTechnicianId(technician.getId())).thenReturn(List.of(
                    UserTestFactory.technicalAssistance().technician(technician).community(ended).ended().build()));

            ProductionDashboardDTO result = useCase.getDashboard(techUser);

            assertThat(result.farmerCount()).isZero();
            assertThat(result.planCount()).isZero();
            assertThat(result.communities()).isEmpty();
            assertThat(result.percentageRealized()).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("Given TECHNICIAN sem perfil When dashboard Then AccessDeniedException")
        void givenTechnicianWithoutProfile_whenDashboard_thenAccessDenied() {
            User techUser = UserTestFactory.user().technician().build();
            when(technicianRepository.findByUserId(techUser.getId())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.getDashboard(techUser))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Técnico não encontrado.");
        }
    }

    @Nested
    @DisplayName("MANAGER — escopo restrito à própria organização")
    class ManagerScope {

        @Test
        @DisplayName("Given MANAGER When dashboard Then só agricultores da organização do gestor")
        void givenManager_whenDashboard_thenOnlyOwnOrganizationFarmers() {
            Organization ownOrg = UserTestFactory.organization().name("Org Gestor").build();
            Organization otherOrg = UserTestFactory.organization().name("Org Alheia").build();
            User managerUser = UserTestFactory.user().manager().build();
            Manager manager = UserTestFactory.managerProfile().user(managerUser).organization(ownOrg).build();
            Farmer own = UserTestFactory.farmerProfile()
                    .community(UserTestFactory.community().organization(ownOrg).build()).build();
            Farmer foreign = UserTestFactory.farmerProfile()
                    .community(UserTestFactory.community().organization(otherOrg).build()).build();
            Farmer withoutCommunity = UserTestFactory.farmerProfile().community(null).build();
            ProductionPlan plan = plan(own, "10", "10");
            when(managerRepository.findByUserId(managerUser.getId())).thenReturn(Optional.of(manager));
            when(farmerRepository.findAll()).thenReturn(List.of(own, foreign, withoutCommunity));
            when(planRepository.findAllByFarmerId(own.getId())).thenReturn(List.of(plan));
            when(executionRepository.findAllByProductionPlanId(plan.getId()))
                    .thenReturn(List.of(execution(plan, "100", ExecutionStatus.VALIDATED)));

            ProductionDashboardDTO result = useCase.getDashboard(managerUser);

            assertThat(result.farmerCount()).isEqualTo(1);
            assertThat(result.communities()).hasSize(1);
            assertThat(result.communities().get(0).communityId()).isEqualTo(own.getCommunity().getId());
            assertThat(result.percentageRealized()).isEqualByComparingTo("100.00");
            verify(planRepository, never()).findAllByFarmerId(foreign.getId());
            verify(planRepository, never()).findAllByFarmerId(withoutCommunity.getId());
        }

        @Test
        @DisplayName("Given MANAGER sem organização When dashboard Then AccessDeniedException")
        void givenManagerWithoutOrganization_whenDashboard_thenAccessDenied() {
            User managerUser = UserTestFactory.user().manager().build();
            Manager manager = UserTestFactory.managerProfile().user(managerUser).organization(null).build();
            when(managerRepository.findByUserId(managerUser.getId())).thenReturn(Optional.of(manager));

            assertThatThrownBy(() -> useCase.getDashboard(managerUser))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Gestor não possui organização vinculada.");
        }

        @Test
        @DisplayName("Given MANAGER sem perfil When dashboard Then AccessDeniedException")
        void givenManagerWithoutProfile_whenDashboard_thenAccessDenied() {
            User managerUser = UserTestFactory.user().manager().build();
            when(managerRepository.findByUserId(managerUser.getId())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.getDashboard(managerUser))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Gestor não encontrado.");
        }
    }

    @Nested
    @DisplayName("ADMIN — visão consolidada de todas as comunidades")
    class AdminScope {

        @Test
        @DisplayName("Given ADMIN When dashboard Then consolida todas as comunidades e ignora agricultores removidos")
        void givenAdmin_whenDashboard_thenConsolidatesAllCommunities() {
            User admin = UserTestFactory.user().admin().build();
            Community communityA = UserTestFactory.community().name("A").build();
            Community communityB = UserTestFactory.community().name("B").build();
            Farmer farmerA = UserTestFactory.farmerProfile().community(communityA).build();
            Farmer farmerB = UserTestFactory.farmerProfile().community(communityB).build();
            Farmer removed = Farmer.builder().id(UUID.randomUUID()).community(communityA)
                    .deletedAt(LocalDateTime.of(2026, 1, 1, 0, 0)).build();
            ProductionPlan planA = plan(farmerA, "10", "10");
            ProductionPlan planB = plan(farmerB, "10", "10");
            when(farmerRepository.findAll()).thenReturn(List.of(farmerA, farmerB, removed));
            when(planRepository.findAllByFarmerId(farmerA.getId())).thenReturn(List.of(planA));
            when(planRepository.findAllByFarmerId(farmerB.getId())).thenReturn(List.of(planB));
            when(executionRepository.findAllByProductionPlanId(planA.getId()))
                    .thenReturn(List.of(execution(planA, "100", ExecutionStatus.VALIDATED)));
            when(executionRepository.findAllByProductionPlanId(planB.getId()))
                    .thenReturn(List.of(execution(planB, "50", ExecutionStatus.VALIDATED)));

            ProductionDashboardDTO result = useCase.getDashboard(admin);

            assertThat(result.farmerCount()).isEqualTo(2);
            assertThat(result.planCount()).isEqualTo(2);
            assertThat(result.expectedYield()).isEqualByComparingTo("200");
            assertThat(result.actualYield()).isEqualByComparingTo("150");
            assertThat(result.difference()).isEqualByComparingTo("-50");
            assertThat(result.percentageRealized()).isEqualByComparingTo("75.00");
            assertThat(result.communities()).extracting(ProductionDashboardDTO.CommunityProductionDTO::communityName)
                    .containsExactly("A", "B");
            verify(planRepository, never()).findAllByFarmerId(removed.getId());
        }

        @Test
        @DisplayName("Given agricultor sem comunidade When dashboard Then agrupa em 'Sem comunidade' sem NPE")
        void givenFarmerWithoutCommunity_whenDashboard_thenGroupedAsWithoutCommunity() {
            User admin = UserTestFactory.user().admin().build();
            Farmer farmer = UserTestFactory.farmerProfile().community(null).build();
            ProductionPlan plan = plan(farmer, "10", "10");
            when(farmerRepository.findAll()).thenReturn(List.of(farmer));
            when(planRepository.findAllByFarmerId(farmer.getId())).thenReturn(List.of(plan));
            when(executionRepository.findAllByProductionPlanId(plan.getId())).thenReturn(List.of());

            ProductionDashboardDTO result = useCase.getDashboard(admin);

            assertThat(result.communities()).hasSize(1);
            assertThat(result.communities().get(0).communityId()).isNull();
            assertThat(result.communities().get(0).communityName()).isEqualTo("Sem comunidade");
            assertThat(result.communities().get(0).farmerCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Given meta zero When dashboard Then percentual é 0 sem divisão por zero")
        void givenZeroExpectedYield_whenDashboard_thenPercentageIsZero() {
            User admin = UserTestFactory.user().admin().build();
            Farmer farmer = UserTestFactory.farmerProfile().build();
            ProductionPlan zeroTarget = plan(farmer, "10", "0");
            when(farmerRepository.findAll()).thenReturn(List.of(farmer));
            when(planRepository.findAllByFarmerId(farmer.getId())).thenReturn(List.of(zeroTarget));
            when(executionRepository.findAllByProductionPlanId(zeroTarget.getId()))
                    .thenReturn(List.of(execution(zeroTarget, "10", ExecutionStatus.VALIDATED)));

            ProductionDashboardDTO result = useCase.getDashboard(admin);

            assertThat(result.expectedYield()).isEqualByComparingTo("0");
            assertThat(result.actualYield()).isEqualByComparingTo("10");
            assertThat(result.difference()).isEqualByComparingTo("10");
            assertThat(result.percentageRealized()).isEqualByComparingTo("0");
            assertThat(result.communities().get(0).percentageRealized()).isEqualByComparingTo("0");
        }
    }

    @Nested
    @DisplayName("Acesso negado sem usuário ou papel")
    class Unauthenticated {

        @Test
        @DisplayName("Given usuário nulo When dashboard Then AccessDeniedException")
        void givenNullUser_whenDashboard_thenAccessDenied() {
            assertThatThrownBy(() -> useCase.getDashboard(null))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Acesso negado ao painel de produção.");
        }

        @Test
        @DisplayName("Given usuário sem papel When dashboard Then AccessDeniedException")
        void givenUserWithoutRole_whenDashboard_thenAccessDenied() {
            User noRole = UserTestFactory.user().role(null).build();

            assertThatThrownBy(() -> useCase.getDashboard(noRole))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Acesso negado ao painel de produção.");
        }
    }

    private ProductionPlan plan(Farmer farmer, String area, String productivity) {
        return new ProductionPlan(UUID.randomUUID(), farmer, UserTestFactory.harvest(), UserTestFactory.crop(),
                new BigDecimal(area), PlanStatus.PLANNED, null, null, new BigDecimal(productivity),
                null, null, null, LocalDate.of(2026, 3, 1), null, null, null, null);
    }

    private ProductionExecution execution(ProductionPlan plan, String quantityKg, ExecutionStatus status) {
        return new ProductionExecution(UUID.randomUUID(), plan, new BigDecimal(quantityKg), new BigDecimal(quantityKg),
                LocalDate.of(2026, 6, 1), status, null, null, null, null, null, null, null, null, null, null);
    }
}
