package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.adapters.in.web.dto.production.ProductionComparisonDTO;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.Manager;
import com.ufal.smartagro.domain.model.Organization;
import com.ufal.smartagro.domain.model.ProductionExecution;
import com.ufal.smartagro.domain.model.ProductionPlan;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
import com.ufal.smartagro.domain.port.out.ManagerRepository;
import com.ufal.smartagro.domain.port.out.ProductionExecutionRepository;
import com.ufal.smartagro.domain.port.out.ProductionPlanRepository;
import com.ufal.smartagro.domain.port.out.TechnicalAssistanceRepository;
import com.ufal.smartagro.domain.port.out.TechnicianRepository;
import com.ufal.smartagro.testsupport.UserTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductionReadUseCaseRbacTest {

    @Mock
    private ManagerRepository managerRepository;
    @Mock
    private TechnicianRepository technicianRepository;
    @Mock
    private TechnicalAssistanceRepository technicalAssistanceRepository;
    @Mock
    private ProductionPlanRepository productionPlanRepository;
    @Mock
    private ProductionExecutionRepository productionExecutionRepository;
    @Mock
    private FarmerRepository farmerRepository;

    private FindProductionPlanByIdUseCase findPlanById;
    private ListProductionPlansUseCase listPlans;
    private FindProductionExecutionByIdUseCase findExecutionById;
    private CompareProductionUseCase compareProduction;

    @BeforeEach
    void setUp() {
        ProductionAccessValidator validator = new ProductionAccessValidator(
                managerRepository, technicianRepository, technicalAssistanceRepository);
        findPlanById = new FindProductionPlanByIdUseCase(productionPlanRepository, validator);
        listPlans = new ListProductionPlansUseCase(productionPlanRepository, farmerRepository, validator);
        findExecutionById = new FindProductionExecutionByIdUseCase(productionExecutionRepository, validator);
        compareProduction = new CompareProductionUseCase(
                productionPlanRepository, productionExecutionRepository, validator);
    }

    @Test
    @DisplayName("Given TECHNICIAN When find plano de terceiro Then leitura é permitida")
    void givenTechnician_whenFindThirdPartyPlan_thenAllowsRead() {
        User technician = UserTestFactory.user().technician().build();
        ProductionPlan plan = UserTestFactory.productionPlan().build();
        when(productionPlanRepository.findById(plan.getId())).thenReturn(Optional.of(plan));

        ProductionPlan result = findPlanById.findById(plan.getId(), technician);

        assertThat(result.getId()).isEqualTo(plan.getId());
    }

    @Test
    @DisplayName("Given TECHNICIAN When list planos de farmer de terceiro Then leitura é permitida")
    void givenTechnician_whenListThirdPartyPlans_thenAllowsRead() {
        User technician = UserTestFactory.user().technician().build();
        Farmer farmer = UserTestFactory.farmerProfile().build();
        ProductionPlan plan = UserTestFactory.productionPlan().farmer(farmer).build();
        when(farmerRepository.findById(farmer.getId())).thenReturn(Optional.of(farmer));
        when(productionPlanRepository.findAllByFarmerId(farmer.getId())).thenReturn(List.of(plan));

        List<ProductionPlan> result = listPlans.listByFarmer(farmer.getId(), technician);

        assertThat(result).containsExactly(plan);
    }

    @Test
    @DisplayName("Given TECHNICIAN When find execução de terceiro Then leitura é permitida")
    void givenTechnician_whenFindThirdPartyExecution_thenAllowsRead() {
        User technician = UserTestFactory.user().technician().build();
        ProductionPlan plan = UserTestFactory.productionPlan().build();
        ProductionExecution execution = executionOf(plan);
        when(productionExecutionRepository.findById(execution.getId())).thenReturn(Optional.of(execution));

        ProductionExecution result = findExecutionById.findById(execution.getId(), technician);

        assertThat(result.getId()).isEqualTo(execution.getId());
    }

    @Test
    @DisplayName("Given TECHNICIAN When compare plano de terceiro Then leitura é permitida")
    void givenTechnician_whenCompareThirdPartyPlan_thenAllowsRead() {
        User technician = UserTestFactory.user().technician().build();
        ProductionPlan plan = UserTestFactory.productionPlan().build();
        when(productionPlanRepository.findById(plan.getId())).thenReturn(Optional.of(plan));
        when(productionExecutionRepository.findAllByProductionPlanId(plan.getId())).thenReturn(List.of());

        ProductionComparisonDTO result = compareProduction.compare(plan.getId(), technician);

        assertThat(result.productionPlanId()).isEqualTo(plan.getId());
        assertThat(result.expectedYield()).isEqualByComparingTo(plan.getExpectedYield());
    }

    @Test
    @DisplayName("Given FARMER When compare plano de terceiro Then AccessDeniedException")
    void givenFarmer_whenCompareThirdPartyPlan_thenAccessDenied() {
        User farmerUser = UserTestFactory.user().farmer().build();
        Farmer otherFarmer = UserTestFactory.farmerProfile()
                .user(UserTestFactory.user().farmer().email("outro@smartagro.test").build())
                .build();
        ProductionPlan plan = UserTestFactory.productionPlan().farmer(otherFarmer).build();
        when(productionPlanRepository.findById(plan.getId())).thenReturn(Optional.of(plan));

        assertThatThrownBy(() -> compareProduction.compare(plan.getId(), farmerUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("O agricultor só tem acesso aos seus próprios dados de produção.");
    }

    @Test
    @DisplayName("Given MANAGER When find plano de outra org Then AccessDeniedException")
    void givenManager_whenFindPlanOfAnotherOrganization_thenAccessDenied() {
        Organization managerOrg = UserTestFactory.organization().name("Org Gestor").build();
        Organization otherOrg = UserTestFactory.organization().name("Org Alheia").build();
        User managerUser = UserTestFactory.user().manager().build();
        Manager manager = UserTestFactory.managerProfile().user(managerUser).organization(managerOrg).build();
        Farmer farmer = UserTestFactory.farmerProfile()
                .community(UserTestFactory.community().organization(otherOrg).build())
                .build();
        ProductionPlan plan = UserTestFactory.productionPlan().farmer(farmer).build();
        when(productionPlanRepository.findById(plan.getId())).thenReturn(Optional.of(plan));
        when(managerRepository.findByUserId(managerUser.getId())).thenReturn(Optional.of(manager));

        assertThatThrownBy(() -> findPlanById.findById(plan.getId(), managerUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("O gestor só tem acesso aos agricultores da sua própria organização.");
    }

    private ProductionExecution executionOf(ProductionPlan plan) {
        return new ProductionExecution(
                UUID.randomUUID(),
                plan,
                new BigDecimal("40.00"),
                LocalDate.of(2026, 6, 1),
                null,
                null,
                null
        );
    }
}
