package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.adapters.in.web.dto.production.ProductionExecutionRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.production.ProductionExecutionUpdateDTO;
import com.ufal.smartagro.adapters.in.web.dto.production.ProductionPlanRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.production.ProductionPlanUpdateDTO;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.model.Crop;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.Harvest;
import com.ufal.smartagro.domain.model.Manager;
import com.ufal.smartagro.domain.model.Organization;
import com.ufal.smartagro.domain.model.ProductionExecution;
import com.ufal.smartagro.domain.model.ProductionPlan;
import com.ufal.smartagro.domain.model.Technician;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.CropRepository;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
import com.ufal.smartagro.domain.port.out.HarvestRepository;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductionWriteUseCaseRbacTest {

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
    @Mock
    private HarvestRepository harvestRepository;
    @Mock
    private CropRepository cropRepository;
    @Mock
    private ProductionVersionGuard versionGuard;

    private CreateProductionPlanUseCase createPlan;
    private UpdateProductionPlanUseCase updatePlan;
    private DeleteProductionPlanUseCase deletePlan;
    private CreateProductionExecutionUseCase createExecution;
    private UpdateProductionExecutionUseCase updateExecution;
    private DeleteProductionExecutionUseCase deleteExecution;

    @BeforeEach
    void setUp() {
        ProductionAccessValidator validator = new ProductionAccessValidator(
                managerRepository, technicianRepository, technicalAssistanceRepository);
        createPlan = new CreateProductionPlanUseCase(
                productionPlanRepository, farmerRepository, harvestRepository, cropRepository, validator);
        updatePlan = new UpdateProductionPlanUseCase(productionPlanRepository, validator, versionGuard);
        deletePlan = new DeleteProductionPlanUseCase(productionPlanRepository, validator);
        createExecution = new CreateProductionExecutionUseCase(
                productionExecutionRepository, productionPlanRepository, validator);
        updateExecution = new UpdateProductionExecutionUseCase(productionExecutionRepository, validator, versionGuard);
        deleteExecution = new DeleteProductionExecutionUseCase(productionExecutionRepository, validator);
    }

    @Nested
    @DisplayName("Create plan — matriz de 4 roles")
    class CreatePlan {

        @Test
        @DisplayName("Given ADMIN When create plano Then persiste")
        void givenAdmin_whenCreatePlan_thenPersists() {
            User admin = UserTestFactory.user().admin().build();
            Farmer farmer = UserTestFactory.farmerProfile().build();
            stubCreatePlanDependencies(farmer);
            when(productionPlanRepository.save(any(ProductionPlan.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            ProductionPlan result = createPlan.create(farmer.getId(), planRegisterDto(), admin);

            assertThat(result.getFarmer()).isEqualTo(farmer);
            verify(productionPlanRepository).save(any(ProductionPlan.class));
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
            stubCreatePlanDependencies(farmer);
            when(managerRepository.findByUserId(managerUser.getId())).thenReturn(Optional.of(manager));
            when(productionPlanRepository.save(any(ProductionPlan.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            ProductionPlan result = createPlan.create(farmer.getId(), planRegisterDto(), managerUser);

            assertThat(result.getFarmer().getCommunity().getOrganization().getId()).isEqualTo(organization.getId());
        }

        @Test
        @DisplayName("Given MANAGER When farmer de outra org Then AccessDeniedException sem save")
        void givenManager_whenFarmerInAnotherOrg_thenAccessDenied() {
            Organization managerOrg = UserTestFactory.organization().name("Org Gestor").build();
            Organization otherOrg = UserTestFactory.organization().name("Org Alheia").build();
            User managerUser = UserTestFactory.user().manager().build();
            Manager manager = UserTestFactory.managerProfile().user(managerUser).organization(managerOrg).build();
            Farmer farmer = UserTestFactory.farmerProfile()
                    .community(UserTestFactory.community().organization(otherOrg).build())
                    .build();
            when(farmerRepository.findById(farmer.getId())).thenReturn(Optional.of(farmer));
            when(managerRepository.findByUserId(managerUser.getId())).thenReturn(Optional.of(manager));

            assertThatThrownBy(() -> createPlan.create(farmer.getId(), planRegisterDto(), managerUser))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("O gestor só tem acesso aos agricultores da sua própria organização.");

            verify(productionPlanRepository, never()).save(any());
        }

        @Test
        @DisplayName("Given FARMER When cria plano próprio Then persiste")
        void givenFarmer_whenCreateOwnPlan_thenPersists() {
            User farmerUser = UserTestFactory.user().farmer().build();
            Farmer farmer = UserTestFactory.farmerProfile().user(farmerUser).build();
            stubCreatePlanDependencies(farmer);
            when(productionPlanRepository.save(any(ProductionPlan.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            ProductionPlan result = createPlan.create(farmer.getId(), planRegisterDto(), farmerUser);

            assertThat(result.getFarmer().getUser().getId()).isEqualTo(farmerUser.getId());
        }

        @Test
        @DisplayName("Given FARMER When cria plano de terceiro Then AccessDeniedException")
        void givenFarmer_whenCreateThirdPartyPlan_thenAccessDenied() {
            User farmerUser = UserTestFactory.user().farmer().build();
            Farmer other = UserTestFactory.farmerProfile()
                    .user(UserTestFactory.user().farmer().email("outro@smartagro.test").build())
                    .build();
            when(farmerRepository.findById(other.getId())).thenReturn(Optional.of(other));

            assertThatThrownBy(() -> createPlan.create(other.getId(), planRegisterDto(), farmerUser))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("O agricultor só tem acesso aos seus próprios dados de produção.");

            verify(productionPlanRepository, never()).save(any());
        }

        @Test
        @DisplayName("Given TECHNICIAN When assistência ativa Then persiste")
        void givenTechnician_whenAssignedFarmer_thenPersists() {
            User techUser = UserTestFactory.user().technician().build();
            Technician technician = UserTestFactory.technicianProfile().user(techUser).build();
            Farmer farmer = UserTestFactory.farmerProfile().build();
            stubCreatePlanDependencies(farmer);
            when(technicianRepository.findByUserId(techUser.getId())).thenReturn(Optional.of(technician));
            when(technicalAssistanceRepository.findActiveByTechnicianAndFarmer(technician.getId(), farmer.getId()))
                    .thenReturn(Optional.of(UserTestFactory.technicalAssistance().technician(technician).farmer(farmer).build()));
            when(productionPlanRepository.save(any(ProductionPlan.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            ProductionPlan result = createPlan.create(farmer.getId(), planRegisterDto(), techUser);

            assertThat(result.getFarmer()).isEqualTo(farmer);
        }

        @Test
        @DisplayName("Given TECHNICIAN When farmer de terceiro sem assistência Then AccessDeniedException sem save")
        void givenTechnician_whenUnassignedFarmer_thenAccessDenied() {
            User techUser = UserTestFactory.user().technician().build();
            Technician technician = UserTestFactory.technicianProfile().user(techUser).build();
            Farmer farmer = UserTestFactory.farmerProfile().build();
            when(farmerRepository.findById(farmer.getId())).thenReturn(Optional.of(farmer));
            when(technicianRepository.findByUserId(techUser.getId())).thenReturn(Optional.of(technician));
            when(technicalAssistanceRepository.findActiveByTechnicianAndFarmer(technician.getId(), farmer.getId()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> createPlan.create(farmer.getId(), planRegisterDto(), techUser))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage(ProductionAccessValidator.TECHNICIAN_WRITE_DENIED);

            verify(productionPlanRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Update/delete plan e execution — TECHNICIAN sem vínculo")
    class TechnicianMutationsBlocked {

        @Test
        @DisplayName("Given TECHNICIAN When update plano de terceiro Then AccessDeniedException")
        void givenTechnician_whenUpdateUnassignedPlan_thenAccessDenied() {
            User techUser = UserTestFactory.user().technician().build();
            Technician technician = UserTestFactory.technicianProfile().user(techUser).build();
            ProductionPlan plan = UserTestFactory.productionPlan().build();
            when(productionPlanRepository.findById(plan.getId())).thenReturn(Optional.of(plan));
            stubTechnicianWithoutAssistance(techUser, technician, plan.getFarmer());

            assertThatThrownBy(() -> updatePlan.update(plan.getId(), planUpdateDto(), techUser))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage(ProductionAccessValidator.TECHNICIAN_WRITE_DENIED);

            verify(productionPlanRepository, never()).save(any());
        }

        @Test
        @DisplayName("Given TECHNICIAN When delete plano de terceiro Then AccessDeniedException")
        void givenTechnician_whenDeleteUnassignedPlan_thenAccessDenied() {
            User techUser = UserTestFactory.user().technician().build();
            Technician technician = UserTestFactory.technicianProfile().user(techUser).build();
            ProductionPlan plan = UserTestFactory.productionPlan().build();
            when(productionPlanRepository.findById(plan.getId())).thenReturn(Optional.of(plan));
            stubTechnicianWithoutAssistance(techUser, technician, plan.getFarmer());

            assertThatThrownBy(() -> deletePlan.delete(plan.getId(), techUser))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage(ProductionAccessValidator.TECHNICIAN_WRITE_DENIED);

            verify(productionPlanRepository, never()).delete(plan.getId());
        }

        @Test
        @DisplayName("Given TECHNICIAN When create execução de terceiro Then AccessDeniedException")
        void givenTechnician_whenCreateUnassignedExecution_thenAccessDenied() {
            User techUser = UserTestFactory.user().technician().build();
            Technician technician = UserTestFactory.technicianProfile().user(techUser).build();
            ProductionPlan plan = UserTestFactory.productionPlan().build();
            when(productionPlanRepository.findById(plan.getId())).thenReturn(Optional.of(plan));
            stubTechnicianWithoutAssistance(techUser, technician, plan.getFarmer());

            assertThatThrownBy(() -> createExecution.create(plan.getId(), executionRegisterDto(), techUser))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage(ProductionAccessValidator.TECHNICIAN_WRITE_DENIED);

            verify(productionExecutionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Given TECHNICIAN When update execução de terceiro Then AccessDeniedException")
        void givenTechnician_whenUpdateUnassignedExecution_thenAccessDenied() {
            User techUser = UserTestFactory.user().technician().build();
            Technician technician = UserTestFactory.technicianProfile().user(techUser).build();
            ProductionPlan plan = UserTestFactory.productionPlan().build();
            ProductionExecution execution = executionOf(plan);
            when(productionExecutionRepository.findById(execution.getId())).thenReturn(Optional.of(execution));
            stubTechnicianWithoutAssistance(techUser, technician, plan.getFarmer());

            assertThatThrownBy(() -> updateExecution.update(execution.getId(), executionUpdateDto(), techUser))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage(ProductionAccessValidator.TECHNICIAN_WRITE_DENIED);

            verify(productionExecutionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Given TECHNICIAN When delete execução de terceiro Then AccessDeniedException")
        void givenTechnician_whenDeleteUnassignedExecution_thenAccessDenied() {
            User techUser = UserTestFactory.user().technician().build();
            Technician technician = UserTestFactory.technicianProfile().user(techUser).build();
            ProductionPlan plan = UserTestFactory.productionPlan().build();
            ProductionExecution execution = executionOf(plan);
            when(productionExecutionRepository.findById(execution.getId())).thenReturn(Optional.of(execution));
            stubTechnicianWithoutAssistance(techUser, technician, plan.getFarmer());

            assertThatThrownBy(() -> deleteExecution.delete(execution.getId(), techUser))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage(ProductionAccessValidator.TECHNICIAN_WRITE_DENIED);

            verify(productionExecutionRepository, never()).delete(execution.getId());
        }
    }

    private void stubCreatePlanDependencies(Farmer farmer) {
        Harvest harvest = UserTestFactory.harvest();
        Crop crop = UserTestFactory.crop();
        when(farmerRepository.findById(farmer.getId())).thenReturn(Optional.of(farmer));
        when(harvestRepository.findById(any(UUID.class))).thenReturn(Optional.of(harvest));
        when(cropRepository.findById(any(UUID.class))).thenReturn(Optional.of(crop));
    }

    private void stubTechnicianWithoutAssistance(User techUser, Technician technician, Farmer farmer) {
        when(technicianRepository.findByUserId(techUser.getId())).thenReturn(Optional.of(technician));
        when(technicalAssistanceRepository.findActiveByTechnicianAndFarmer(technician.getId(), farmer.getId()))
                .thenReturn(Optional.empty());
    }

    private ProductionPlanRegisterDTO planRegisterDto() {
        return new ProductionPlanRegisterDTO(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("12.00"),
                new BigDecimal("80.00"),
                LocalDate.of(2026, 3, 1),
                null
        );
    }

    private ProductionPlanUpdateDTO planUpdateDto() {
        return new ProductionPlanUpdateDTO(new BigDecimal("15.00"), null, null, null, null);
    }

    private ProductionExecutionRegisterDTO executionRegisterDto() {
        return new ProductionExecutionRegisterDTO(new BigDecimal("50.00"), LocalDate.of(2026, 6, 1), null, null, null, null);
    }

    private ProductionExecutionUpdateDTO executionUpdateDto() {
        return new ProductionExecutionUpdateDTO(new BigDecimal("55.00"), null, null, null, null, null, null, null);
    }

    private ProductionExecution executionOf(ProductionPlan plan) {
        return new ProductionExecution(
                UUID.randomUUID(),
                plan,
                new BigDecimal("40.00"),
                LocalDate.of(2026, 6, 1),
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
}
