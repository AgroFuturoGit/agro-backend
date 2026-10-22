package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.adapters.in.web.dto.analytics.ManagementCropRankingItemDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.ManagementDashboardSummaryDTO;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.exception.HarvestNotFoundException;
import com.ufal.smartagro.domain.model.Community;
import com.ufal.smartagro.domain.model.Harvest;
import com.ufal.smartagro.domain.model.ManagementCommunityCensusRow;
import com.ufal.smartagro.domain.model.ManagementOrgHarvestSummaryRow;
import com.ufal.smartagro.domain.model.Manager;
import com.ufal.smartagro.domain.model.Organization;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.ManagementDashboardScope;
import com.ufal.smartagro.domain.model.enums.OrganizationType;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.CommunityRepository;
import com.ufal.smartagro.domain.port.out.HarvestRepository;
import com.ufal.smartagro.domain.port.out.ManagerRepository;
import com.ufal.smartagro.domain.port.out.OrganizationRepository;
import com.ufal.smartagro.domain.port.out.ProductionPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetManagementDashboardSummaryUseCaseTest {

    @Mock
    private ManagerRepository managerRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private CommunityRepository communityRepository;

    @Mock
    private HarvestRepository harvestRepository;

    @Mock
    private ProductionPlanRepository productionPlanRepository;

    @InjectMocks
    private GetManagementDashboardSummaryUseCase useCase;

    private UUID managerUserId;
    private UUID managerProfileId;
    private UUID orgIdA;
    private UUID orgIdB;
    private UUID commId1;
    private UUID commId2;
    private UUID harvestId;
    private UUID crop1Id;
    private UUID crop2Id;

    private User managerUser;
    private User adminUser;
    private User technicianUser;
    private Organization orgA;
    private Organization orgB;
    private Manager managerProfile;
    private Community comm1;
    private Community comm2;
    private Harvest harvest;

    private final LocalDate refDate = LocalDate.of(2026, 6, 15);

    @BeforeEach
    void setUp() {
        managerUserId = UUID.randomUUID();
        managerProfileId = UUID.randomUUID();
        orgIdA = UUID.randomUUID();
        orgIdB = UUID.randomUUID();
        commId1 = UUID.randomUUID();
        commId2 = UUID.randomUUID();
        harvestId = UUID.randomUUID();
        crop1Id = UUID.randomUUID();
        crop2Id = UUID.randomUUID();

        managerUser = User.builder()
                .id(managerUserId)
                .fullName("Gestor Silva")
                .role(Role.MANAGER)
                .build();

        adminUser = User.builder()
                .id(UUID.randomUUID())
                .fullName("Admin Geral")
                .role(Role.ADMIN)
                .build();

        technicianUser = User.builder()
                .id(UUID.randomUUID())
                .fullName("Técnico Santos")
                .role(Role.TECHNICIAN)
                .build();

        orgA = new Organization(orgIdA, "COOPAFAL", "12345678000199", OrganizationType.COOP, LocalDateTime.now(), null, null);
        orgB = new Organization(orgIdB, "COOPAL", "98765432000100", OrganizationType.ASSOC, LocalDateTime.now(), null, null);

        managerProfile = new Manager(managerProfileId, managerUser, orgA, LocalDateTime.now(), null, null);
        comm1 = new Community(commId1, "São José", orgA, LocalDateTime.now(), null, null);
        comm2 = new Community(commId2, "Santa Luzia", orgB, LocalDateTime.now(), null, null);

        harvest = new Harvest(harvestId, "Safra 2026", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
    }

    @Nested
    @DisplayName("RBAC and Access Control")
    class RbacAndAccessControl {

        @Test
        @DisplayName("executeForCurrentManager: deve falhar quando usuário não tem perfil de gestor")
        void currentManager_semPerfil_deveLancarAccessDenied() {
            when(managerRepository.findByUserId(managerUserId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.executeForCurrentManager(harvestId, null, managerUser, refDate))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("Perfil de gestor não encontrado");
        }

        @Test
        @DisplayName("executeForCurrentManager: deve falhar quando perfil de gestor está inativo/excluído")
        void currentManager_perfilExcluido_deveLancarAccessDenied() {
            Manager deletedManager = new Manager(managerProfileId, managerUser, orgA, LocalDateTime.now(), null, LocalDateTime.now());
            when(managerRepository.findByUserId(managerUserId)).thenReturn(Optional.of(deletedManager));

            assertThatThrownBy(() -> useCase.executeForCurrentManager(harvestId, null, managerUser, refDate))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("inativo ou excluído");
        }

        @Test
        @DisplayName("executeForCurrentManager: deve falhar quando gestor não tem organização vinculada")
        void currentManager_semOrganizacao_deveLancarAccessDenied() {
            Manager managerNoOrg = new Manager(managerProfileId, managerUser, null, LocalDateTime.now(), null, null);
            when(managerRepository.findByUserId(managerUserId)).thenReturn(Optional.of(managerNoOrg));

            assertThatThrownBy(() -> useCase.executeForCurrentManager(harvestId, null, managerUser, refDate))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("sem organização vinculada");
        }

        @Test
        @DisplayName("executeForOrganization: gestor tentando acessar outra organização deve tomar 403 Forbidden")
        void executeForOrganization_gestorOutraOrg_deveLancarAccessDenied() {
            when(managerRepository.findByUserId(managerUserId)).thenReturn(Optional.of(managerProfile));

            assertThatThrownBy(() -> useCase.executeForOrganization(orgIdB, harvestId, null, managerUser, refDate))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("não tem permissão para visualizar dados de outra organização");
        }

        @Test
        @DisplayName("executeForOrganization: usuário não gestor e não admin deve tomar 403 Forbidden")
        void executeForOrganization_usuarioInvalido_deveLancarAccessDenied() {
            assertThatThrownBy(() -> useCase.executeForOrganization(orgIdA, harvestId, null, technicianUser, refDate))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("Apenas Gestores e Administradores");
        }

        @Test
        @DisplayName("executeForOrganization: comunidade de outra organização deve tomar 403 Forbidden")
        void executeForOrganization_comunidadeOutraOrg_deveLancarAccessDenied() {
            when(managerRepository.findByUserId(managerUserId)).thenReturn(Optional.of(managerProfile));
            when(organizationRepository.findById(orgIdA)).thenReturn(Optional.of(orgA));
            when(communityRepository.findById(commId2)).thenReturn(Optional.of(comm2)); // comm2 pertence a orgB

            assertThatThrownBy(() -> useCase.executeForOrganization(orgIdA, harvestId, commId2, managerUser, refDate))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("não pertence");
        }

        @Test
        @DisplayName("executeForOrganization: organização inexistente deve lançar EntityNotFoundException (404)")
        void executeForOrganization_orgInexistente_deveLancarNotFound() {
            when(organizationRepository.findById(orgIdA)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.executeForOrganization(orgIdA, harvestId, null, adminUser, refDate))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Organização não encontrada");
        }

        @Test
        @DisplayName("executeForOrganization: comunidade inexistente deve lançar EntityNotFoundException (404)")
        void executeForOrganization_comunidadeInexistente_deveLancarNotFound() {
            when(organizationRepository.findById(orgIdA)).thenReturn(Optional.of(orgA));
            when(communityRepository.findById(commId1)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.executeForOrganization(orgIdA, harvestId, commId1, adminUser, refDate))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Comunidade não encontrada");
        }

        @Test
        @DisplayName("executeGlobal: gestor tentando acessar deve tomar 403 Forbidden")
        void executeGlobal_gestorTentandoAcessar_deveLancarAccessDenied() {
            assertThatThrownBy(() -> useCase.executeGlobal(harvestId, null, null, managerUser, refDate))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("Apenas Administradores");
        }
    }

    @Nested
    @DisplayName("Harvest Resolution and Fallback")
    class HarvestResolutionAndFallback {

        @Test
        @DisplayName("deve lançar HarvestNotFoundException quando safra explícita não existe")
        void safraExplicitaInexistente_deveLancarException() {
            when(organizationRepository.findById(orgIdA)).thenReturn(Optional.of(orgA));
            when(harvestRepository.findById(harvestId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.executeForOrganization(orgIdA, harvestId, null, adminUser, refDate))
                    .isInstanceOf(HarvestNotFoundException.class);
        }

        @Test
        @DisplayName("deve resolver safra ativa automaticamente quando harvestId for nulo")
        void safraAtivaAutomatica_quandoHarvestIdNulo() {
            when(organizationRepository.findById(orgIdA)).thenReturn(Optional.of(orgA));
            when(harvestRepository.findActiveHarvest(refDate)).thenReturn(Optional.of(harvest));
            when(productionPlanRepository.findCommunityCensus(orgIdA, null)).thenReturn(List.of(
                    new ManagementCommunityCensusRow(orgIdA, "COOPAFAL", commId1, "São José", 10, 8)
            ));
            when(productionPlanRepository.findManagementHarvestSummary(orgIdA, null, harvestId)).thenReturn(List.of());
            when(productionPlanRepository.findManagementHarvestSummary(orgIdA, null, null)).thenReturn(List.of());

            ManagementDashboardSummaryDTO result = useCase.executeForOrganization(orgIdA, null, null, adminUser, refDate);

            assertThat(result.currentHarvest()).isNotNull();
            assertThat(result.currentHarvest().id()).isEqualTo(harvestId);
            assertThat(result.currentHarvest().label()).isEqualTo("Safra 2026");
            verify(productionPlanRepository).findManagementHarvestSummary(orgIdA, null, harvestId);
        }

        @Test
        @DisplayName("deve aplicar fallback gracioso quando safra ativa não tem dados na MV")
        void fallbackGracioso_quandoSafraAtivaSemDados() {
            when(organizationRepository.findById(orgIdA)).thenReturn(Optional.of(orgA));
            when(harvestRepository.findActiveHarvest(refDate)).thenReturn(Optional.of(harvest));

            ManagementCommunityCensusRow censusRow = new ManagementCommunityCensusRow(orgIdA, "COOPAFAL", commId1, "São José", 10, 9);
            when(productionPlanRepository.findCommunityCensus(orgIdA, null)).thenReturn(List.of(censusRow));

            // Safra ativa retorna vazio
            when(productionPlanRepository.findManagementHarvestSummary(orgIdA, null, harvestId)).thenReturn(List.of());

            // Fallback sem safra traz registros históricos da MV
            ManagementOrgHarvestSummaryRow historicalRow = new ManagementOrgHarvestSummaryRow(
                    orgIdA, "COOPAFAL", commId1, "São José", crop1Id, "Milho", "Crioulo", "KG",
                    UUID.randomUUID(), 5, 4, new BigDecimal("10.00"), new BigDecimal("20000.00"),
                    new BigDecimal("18000.00"), LocalDate.of(2025, 11, 10)
            );
            when(productionPlanRepository.findManagementHarvestSummary(orgIdA, null, null)).thenReturn(List.of(historicalRow));

            ManagementDashboardSummaryDTO result = useCase.executeForOrganization(orgIdA, null, null, adminUser, refDate);

            assertThat(result.isEmpty()).isFalse();
            assertThat(result.totalPlansCount()).isEqualTo(5);
            assertThat(result.totalRealizedKg()).isEqualByComparingTo("18000.00");
        }
    }

    @Nested
    @DisplayName("Empty State")
    class EmptyState {

        @Test
        @DisplayName("deve retornar empty state quando não houver comunidades nem dados na MV")
        void emptyState_quandoSemComunidadesESemDados() {
            when(organizationRepository.findById(orgIdA)).thenReturn(Optional.of(orgA));
            when(harvestRepository.findById(harvestId)).thenReturn(Optional.of(harvest));
            when(productionPlanRepository.findCommunityCensus(orgIdA, null)).thenReturn(List.of());
            when(productionPlanRepository.findManagementHarvestSummary(orgIdA, null, harvestId)).thenReturn(List.of());

            ManagementDashboardSummaryDTO result = useCase.executeForOrganization(orgIdA, harvestId, null, adminUser, refDate);

            assertThat(result.isEmpty()).isTrue();
            assertThat(result.totalOrganizationsCount()).isZero();
            assertThat(result.totalCommunitiesCount()).isZero();
            assertThat(result.totalFarmersCount()).isZero();
            assertThat(result.totalPlansCount()).isZero();
            assertThat(result.totalPlantedAreaHa()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.totalTargetKg()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.totalRealizedKg()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.overallConversionRatePct()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.cropRanking()).isEmpty();
            assertThat(result.communities()).isEmpty();
            assertThat(result.organizations()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Aggregation, Ranking and Market Share")
    class AggregationAndRanking {

        @Test
        @DisplayName("deve calcular métricas, ranking de culturas, market share e comparativos corretamente")
        void calculoMetricasERankingCorretamente() {
            when(organizationRepository.findById(orgIdA)).thenReturn(Optional.of(orgA));
            when(harvestRepository.findById(harvestId)).thenReturn(Optional.of(harvest));

            // Censo de comunidades
            ManagementCommunityCensusRow census1 = new ManagementCommunityCensusRow(orgIdA, "COOPAFAL", commId1, "São José", 20, 18);
            ManagementCommunityCensusRow census2 = new ManagementCommunityCensusRow(orgIdA, "COOPAFAL", commId2, "Santa Luzia", 10, 7);
            when(productionPlanRepository.findCommunityCensus(orgIdA, null)).thenReturn(List.of(census1, census2));

            // MV: 2 culturas distribuídas entre as comunidades
            // Milho: 100.000 kg previsto, 80.000 kg realizado
            ManagementOrgHarvestSummaryRow rowCorn1 = new ManagementOrgHarvestSummaryRow(
                    orgIdA, "COOPAFAL", commId1, "São José", crop1Id, "Milho", "Crioulo", "KG",
                    harvestId, 15, 12, new BigDecimal("30.00"), new BigDecimal("60000.00"),
                    new BigDecimal("50000.00"), LocalDate.of(2026, 6, 1)
            );
            ManagementOrgHarvestSummaryRow rowCorn2 = new ManagementOrgHarvestSummaryRow(
                    orgIdA, "COOPAFAL", commId2, "Santa Luzia", crop1Id, "Milho", "Crioulo", "KG",
                    harvestId, 10, 8, new BigDecimal("20.00"), new BigDecimal("40000.00"),
                    new BigDecimal("30000.00"), LocalDate.of(2026, 6, 2)
            );

            // Mandioca: 40.000 kg previsto, 20.000 kg realizado
            ManagementOrgHarvestSummaryRow rowCassava = new ManagementOrgHarvestSummaryRow(
                    orgIdA, "COOPAFAL", commId1, "São José", crop2Id, "Mandioca", "Branca", "KG",
                    harvestId, 5, 5, new BigDecimal("10.00"), new BigDecimal("40000.00"),
                    new BigDecimal("20000.00"), LocalDate.of(2026, 6, 3)
            );

            when(productionPlanRepository.findManagementHarvestSummary(orgIdA, null, harvestId))
                    .thenReturn(List.of(rowCorn1, rowCorn2, rowCassava));

            ManagementDashboardSummaryDTO result = useCase.executeForOrganization(orgIdA, harvestId, null, adminUser, refDate);

            assertThat(result.isEmpty()).isFalse();
            assertThat(result.scope()).isEqualTo(ManagementDashboardScope.ORGANIZATION);
            assertThat(result.organizationId()).isEqualTo(orgIdA);
            assertThat(result.organizationName()).isEqualTo("COOPAFAL");

            // Cadastrais: 30 agricultores, 25 adimplentes => 25/30 = 83.33%
            assertThat(result.totalCommunitiesCount()).isEqualTo(2);
            assertThat(result.totalFarmersCount()).isEqualTo(30);
            assertThat(result.compliantFarmersCount()).isEqualTo(25);
            assertThat(result.complianceRatePct()).isEqualByComparingTo("83.33");

            // Produtivas: total plans = 30, área = 60 ha, meta = 140.000 kg, realizado = 100.000 kg => 100/140 = 71.43%
            assertThat(result.totalPlansCount()).isEqualTo(30);
            assertThat(result.totalPlantedAreaHa()).isEqualByComparingTo("60.00");
            assertThat(result.totalTargetKg()).isEqualByComparingTo("140000.00");
            assertThat(result.totalRealizedKg()).isEqualByComparingTo("100000.00");
            assertThat(result.overallConversionRatePct()).isEqualByComparingTo("71.43");

            // Ranking de Culturas
            // Milho: Realizado = 80.000 kg (80%), Conversão = 80.000 / 100.000 = 80.00%
            // Mandioca: Realizado = 20.000 kg (20%), Conversão = 20.000 / 40.000 = 50.00%
            assertThat(result.cropRanking()).hasSize(2);
            ManagementCropRankingItemDTO rank1 = result.cropRanking().get(0);
            assertThat(rank1.rankPosition()).isEqualTo(1);
            assertThat(rank1.cropName()).isEqualTo("Milho");
            assertThat(rank1.realizedKg()).isEqualByComparingTo("80000.00");
            assertThat(rank1.conversionRatePct()).isEqualByComparingTo("80.00");
            assertThat(rank1.productionSharePct()).isEqualByComparingTo("80.00");

            ManagementCropRankingItemDTO rank2 = result.cropRanking().get(1);
            assertThat(rank2.rankPosition()).isEqualTo(2);
            assertThat(rank2.cropName()).isEqualTo("Mandioca");
            assertThat(rank2.realizedKg()).isEqualByComparingTo("20000.00");
            assertThat(rank2.conversionRatePct()).isEqualByComparingTo("50.00");
            assertThat(rank2.productionSharePct()).isEqualByComparingTo("20.00");

            // Comunidades
            assertThat(result.communities()).hasSize(2);
            // São José teve 50.000 + 20.000 = 70.000 kg (deve vir primeiro pelo volume)
            assertThat(result.communities().get(0).communityName()).isEqualTo("São José");
            assertThat(result.communities().get(0).realizedKg()).isEqualByComparingTo("70000.00");
            assertThat(result.communities().get(1).communityName()).isEqualTo("Santa Luzia");
            assertThat(result.communities().get(1).realizedKg()).isEqualByComparingTo("30000.00");
        }

        @Test
        @DisplayName("deve calcular market share baseado na meta quando volume realizado for zero (início de safra)")
        void marketShare_comRealizadoZero_deveUsarTargetKg() {
            when(organizationRepository.findById(orgIdA)).thenReturn(Optional.of(orgA));
            when(harvestRepository.findById(harvestId)).thenReturn(Optional.of(harvest));

            ManagementCommunityCensusRow census = new ManagementCommunityCensusRow(orgIdA, "COOPAFAL", commId1, "São José", 10, 10);
            when(productionPlanRepository.findCommunityCensus(orgIdA, null)).thenReturn(List.of(census));

            // Culturas com realizado = 0
            ManagementOrgHarvestSummaryRow row1 = new ManagementOrgHarvestSummaryRow(
                    orgIdA, "COOPAFAL", commId1, "São José", crop1Id, "Milho", null, "KG",
                    harvestId, 5, 5, new BigDecimal("10.00"), new BigDecimal("75000.00"),
                    BigDecimal.ZERO, null
            );
            ManagementOrgHarvestSummaryRow row2 = new ManagementOrgHarvestSummaryRow(
                    orgIdA, "COOPAFAL", commId1, "São José", crop2Id, "Feijão", null, "KG",
                    harvestId, 5, 5, new BigDecimal("10.00"), new BigDecimal("25000.00"),
                    BigDecimal.ZERO, null
            );
            when(productionPlanRepository.findManagementHarvestSummary(orgIdA, null, harvestId))
                    .thenReturn(List.of(row1, row2));

            ManagementDashboardSummaryDTO result = useCase.executeForOrganization(orgIdA, harvestId, null, adminUser, refDate);

            assertThat(result.cropRanking()).hasSize(2);
            // Milho: 75.000 de 100.000 target => 75.00%
            assertThat(result.cropRanking().get(0).productionSharePct()).isEqualByComparingTo("75.00");
            // Feijão: 25.000 de 100.000 target => 25.00%
            assertThat(result.cropRanking().get(1).productionSharePct()).isEqualByComparingTo("25.00");
        }
    }

    @Nested
    @DisplayName("Global Dashboard (Admin)")
    class GlobalDashboard {

        @Test
        @DisplayName("executeGlobal: deve consolidar múltiplas organizações e preencher organizações no DTO")
        void executeGlobal_deveConsolidarMultiplasOrganizacoes() {
            when(harvestRepository.findById(harvestId)).thenReturn(Optional.of(harvest));

            ManagementCommunityCensusRow censusA = new ManagementCommunityCensusRow(orgIdA, "COOPAFAL", commId1, "São José", 20, 20);
            ManagementCommunityCensusRow censusB = new ManagementCommunityCensusRow(orgIdB, "COOPAL", commId2, "Santa Luzia", 15, 10);
            when(productionPlanRepository.findCommunityCensus(null, null)).thenReturn(List.of(censusA, censusB));

            ManagementOrgHarvestSummaryRow rowA = new ManagementOrgHarvestSummaryRow(
                    orgIdA, "COOPAFAL", commId1, "São José", crop1Id, "Milho", "Crioulo", "KG",
                    harvestId, 10, 10, new BigDecimal("20.00"), new BigDecimal("50000.00"),
                    new BigDecimal("40000.00"), LocalDate.of(2026, 5, 1)
            );
            ManagementOrgHarvestSummaryRow rowB = new ManagementOrgHarvestSummaryRow(
                    orgIdB, "COOPAL", commId2, "Santa Luzia", crop2Id, "Feijão", "Carioca", "KG",
                    harvestId, 5, 5, new BigDecimal("10.00"), new BigDecimal("20000.00"),
                    new BigDecimal("15000.00"), LocalDate.of(2026, 5, 2)
            );
            when(productionPlanRepository.findManagementHarvestSummary(null, null, harvestId))
                    .thenReturn(List.of(rowA, rowB));

            ManagementDashboardSummaryDTO result = useCase.executeGlobal(harvestId, null, null, adminUser, refDate);

            assertThat(result.scope()).isEqualTo(ManagementDashboardScope.GLOBAL);
            assertThat(result.totalOrganizationsCount()).isEqualTo(2);
            assertThat(result.totalCommunitiesCount()).isEqualTo(2);
            assertThat(result.totalFarmersCount()).isEqualTo(35);
            assertThat(result.compliantFarmersCount()).isEqualTo(30);
            assertThat(result.organizations()).hasSize(2);
            assertThat(result.organizations().get(0).organizationName()).isEqualTo("COOPAFAL");
            assertThat(result.organizations().get(1).organizationName()).isEqualTo("COOPAL");
        }
    }
}
