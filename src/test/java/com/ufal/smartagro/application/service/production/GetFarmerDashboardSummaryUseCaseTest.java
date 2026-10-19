package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.adapters.in.web.dto.analytics.FarmerDashboardSummaryDTO;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.HarvestNotFoundException;
import com.ufal.smartagro.domain.model.Community;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.FarmerPlanSummary;
import com.ufal.smartagro.domain.model.Harvest;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.HarvestType;
import com.ufal.smartagro.domain.model.enums.PlanProgressStatus;
import com.ufal.smartagro.domain.model.enums.PlanStatus;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
import com.ufal.smartagro.domain.port.out.HarvestRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetFarmerDashboardSummaryUseCaseTest {

    @Mock
    private FarmerRepository farmerRepository;

    @Mock
    private HarvestRepository harvestRepository;

    @Mock
    private ProductionPlanRepository productionPlanRepository;

    @InjectMocks
    private GetFarmerDashboardSummaryUseCase useCase;

    private User farmerUser;
    private Farmer farmer;
    private Community community;
    private UUID userId;
    private UUID farmerId;
    private UUID harvestId;
    private Harvest harvest;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        farmerId = UUID.randomUUID();
        harvestId = UUID.randomUUID();

        farmerUser = User.builder()
                .id(userId)
                .fullName("João Agricultor")
                .email("joao@agro.com")
                .role(Role.FARMER)
                .build();

        community = new Community(UUID.randomUUID(), "Comunidade Bela Vista", null, null, null, null);

        farmer = Farmer.builder()
                .id(farmerId)
                .user(farmerUser)
                .community(community)
                .fullName("João Agricultor")
                .build();

        harvest = new Harvest(
                harvestId,
                "Safra 2026",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31)
        );
    }

    @Nested
    @DisplayName("Validações de RBAC e Perfil")
    class RbacAndProfileTests {

        @Test
        @DisplayName("Deve lançar AccessDeniedException quando loggedUser for nulo")
        void execute_quandoUsuarioNulo_deveLancarAccessDeniedException() {
            assertThatThrownBy(() -> useCase.execute(harvestId, null))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("Acesso negado");
        }

        @Test
        @DisplayName("Deve lançar AccessDeniedException quando loggedUser não tiver papel FARMER")
        void execute_quandoUsuarioNaoForFarmer_deveLancarAccessDeniedException() {
            User techUser = User.builder().id(UUID.randomUUID()).role(Role.TECHNICIAN).build();

            assertThatThrownBy(() -> useCase.execute(harvestId, techUser))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("Acesso negado. Apenas agricultores podem acessar o resumo individual.");
        }

        @Test
        @DisplayName("Deve lançar AccessDeniedException quando perfil de agricultor não for encontrado")
        void execute_quandoPerfilNaoEncontrado_deveLancarAccessDeniedException() {
            when(farmerRepository.findByUserId(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(harvestId, farmerUser))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("Perfil de agricultor não encontrado");
        }

        @Test
        @DisplayName("Deve lançar AccessDeniedException quando agricultor estiver com soft-delete")
        void execute_quandoAgricultorExcluido_deveLancarAccessDeniedException() {
            Farmer deletedFarmer = Farmer.builder()
                    .id(farmerId)
                    .user(farmerUser)
                    .deletedAt(LocalDateTime.now())
                    .build();

            when(farmerRepository.findByUserId(userId)).thenReturn(Optional.of(deletedFarmer));

            assertThatThrownBy(() -> useCase.execute(harvestId, farmerUser))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("Perfil de agricultor inativo ou excluído.");
        }
    }

    @Nested
    @DisplayName("Resolução de Safra")
    class HarvestResolutionTests {

        @Test
        @DisplayName("Deve usar safra informada quando harvestId for passado")
        void execute_quandoHarvestIdInformado_deveUsarSafraInformada() {
            when(farmerRepository.findByUserId(userId)).thenReturn(Optional.of(farmer));
            when(harvestRepository.findById(harvestId)).thenReturn(Optional.of(harvest));
            when(productionPlanRepository.findPlanSummariesByFarmer(farmerId, harvestId, false)).thenReturn(List.of());

            FarmerDashboardSummaryDTO result = useCase.execute(harvestId, farmerUser);

            assertThat(result.currentHarvest()).isNotNull();
            assertThat(result.currentHarvest().id()).isEqualTo(harvestId);
            assertThat(result.currentHarvest().label()).isEqualTo("Safra 2026");
            verify(harvestRepository).findById(harvestId);
            verify(harvestRepository, never()).findActiveHarvest(any());
        }

        @Test
        @DisplayName("Deve lançar HarvestNotFoundException quando harvestId informado não existir")
        void execute_quandoHarvestIdNaoExiste_deveLancarExcecao() {
            when(farmerRepository.findByUserId(userId)).thenReturn(Optional.of(farmer));
            when(harvestRepository.findById(harvestId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(harvestId, farmerUser))
                    .isInstanceOf(HarvestNotFoundException.class)
                    .hasMessageContaining("Safra não encontrada.");
        }

        @Test
        @DisplayName("Deve buscar safra ativa quando harvestId for nulo e passar includeActiveContinuous = true")
        void execute_quandoHarvestIdNulo_deveBuscarSafraAtiva() {
            LocalDate referenceDate = LocalDate.of(2026, 5, 20);
            when(farmerRepository.findByUserId(userId)).thenReturn(Optional.of(farmer));
            when(harvestRepository.findActiveHarvest(referenceDate)).thenReturn(Optional.of(harvest));
            FarmerPlanSummary plan = new FarmerPlanSummary(
                    UUID.randomUUID(), UUID.randomUUID(), "Milho", "Híbrido",
                    HarvestType.CONTINUOUS, "KG", PlanStatus.ACTIVE, "Pomar 1",
                    new BigDecimal("1.00"), new BigDecimal("1000.00"), new BigDecimal("500.00"),
                    BigDecimal.ZERO, LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 4, 1), LocalDate.of(2026, 10, 1), null
            );
            when(productionPlanRepository.findPlanSummariesByFarmer(farmerId, harvestId, true)).thenReturn(List.of(plan));

            FarmerDashboardSummaryDTO result = useCase.execute(null, farmerUser, referenceDate);

            assertThat(result.currentHarvest()).isNotNull();
            assertThat(result.currentHarvest().id()).isEqualTo(harvestId);
            assertThat(result.isEmpty()).isFalse();
            verify(harvestRepository).findActiveHarvest(referenceDate);
            verify(productionPlanRepository).findPlanSummariesByFarmer(farmerId, harvestId, true);
        }

        @Test
        @DisplayName("Quando harvestId for nulo e não houver safra ativa, currentHarvest deve ser nulo e consultar planos sem filtro de safra")
        void execute_quandoSemSafraAtiva_deveRetornarCurrentHarvestNulo() {
            LocalDate referenceDate = LocalDate.of(2026, 5, 20);
            when(farmerRepository.findByUserId(userId)).thenReturn(Optional.of(farmer));
            when(harvestRepository.findActiveHarvest(referenceDate)).thenReturn(Optional.empty());
            when(productionPlanRepository.findPlanSummariesByFarmer(farmerId, null, false)).thenReturn(List.of());

            FarmerDashboardSummaryDTO result = useCase.execute(null, farmerUser, referenceDate);

            assertThat(result.currentHarvest()).isNull();
            verify(productionPlanRepository).findPlanSummariesByFarmer(farmerId, null, false);
        }
    }

    @Nested
    @DisplayName("Colheita Contínua e Fallback de Planos Ativos")
    class ContinuousHarvestAndFallbackTests {

        @Test
        @DisplayName("Ponto 1: Quando safra ativa não tem planos para o agricultor, faz fallback para planos ACTIVE/PLANNED de outras safras")
        void execute_quandoSafraAtivaSemPlanosMasHaPlanosAtivosAnteriores_deveFazerFallback() {
            LocalDate referenceDate = LocalDate.of(2026, 5, 20);
            when(farmerRepository.findByUserId(userId)).thenReturn(Optional.of(farmer));
            when(harvestRepository.findActiveHarvest(referenceDate)).thenReturn(Optional.of(harvest));

            // Safra ativa retorna vazio
            when(productionPlanRepository.findPlanSummariesByFarmer(farmerId, harvestId, true))
                    .thenReturn(List.of());

            // Fallback busca todos os planos sem filtro de safra
            FarmerPlanSummary ongoingPlan = new FarmerPlanSummary(
                    UUID.randomUUID(), UUID.randomUUID(), "Laranja", "Pera",
                    HarvestType.CONTINUOUS, "KG", PlanStatus.ACTIVE, "Pomar 2025",
                    new BigDecimal("3.00"), new BigDecimal("6000.00"), new BigDecimal("2000.00"),
                    BigDecimal.ZERO, LocalDate.of(2025, 6, 1),
                    LocalDate.of(2025, 9, 1), LocalDate.of(2026, 12, 31), null
            );
            FarmerPlanSummary finishedPlan = new FarmerPlanSummary(
                    UUID.randomUUID(), UUID.randomUUID(), "Milho", "Antigo",
                    HarvestType.SINGLE, "KG", PlanStatus.FINISHED, "Talhão Antigo",
                    new BigDecimal("1.00"), new BigDecimal("1000.00"), new BigDecimal("1000.00"),
                    BigDecimal.ZERO, LocalDate.of(2025, 1, 1),
                    LocalDate.of(2025, 4, 1), LocalDate.of(2025, 5, 1), null
            );

            when(productionPlanRepository.findPlanSummariesByFarmer(farmerId, null, false))
                    .thenReturn(List.of(ongoingPlan, finishedPlan));

            FarmerDashboardSummaryDTO result = useCase.execute(null, farmerUser, referenceDate);

            assertThat(result.isEmpty()).isFalse();
            // Somente o plano ACTIVE deve ter sido incluído pelo fallback, excluindo o FINISHED
            assertThat(result.plans()).hasSize(1);
            assertThat(result.plans().get(0).cropName()).isEqualTo("Laranja");
            assertThat(result.plans().get(0).status()).isEqualTo(PlanStatus.ACTIVE);
            verify(productionPlanRepository).findPlanSummariesByFarmer(farmerId, harvestId, true);
            verify(productionPlanRepository).findPlanSummariesByFarmer(farmerId, null, false);
        }

        @Test
        @DisplayName("Ponto 1: Quando nem a safra ativa nem outras safras têm planos ativos, retorna Empty State")
        void execute_quandoSafraAtivaSemPlanosENenhumPlanoAtivoEmOutrasSafras_deveRetornarEmptyState() {
            LocalDate referenceDate = LocalDate.of(2026, 5, 20);
            when(farmerRepository.findByUserId(userId)).thenReturn(Optional.of(farmer));
            when(harvestRepository.findActiveHarvest(referenceDate)).thenReturn(Optional.of(harvest));

            when(productionPlanRepository.findPlanSummariesByFarmer(farmerId, harvestId, true))
                    .thenReturn(List.of());
            when(productionPlanRepository.findPlanSummariesByFarmer(farmerId, null, false))
                    .thenReturn(List.of());

            FarmerDashboardSummaryDTO result = useCase.execute(null, farmerUser, referenceDate);

            assertThat(result.isEmpty()).isTrue();
            assertThat(result.totalPlansCount()).isZero();
            verify(productionPlanRepository).findPlanSummariesByFarmer(farmerId, harvestId, true);
            verify(productionPlanRepository).findPlanSummariesByFarmer(farmerId, null, false);
        }
    }

    @Nested
    @DisplayName("Empty State e Totais Gerais")
    class AggregationAndEmptyStateTests {

        @Test
        @DisplayName("Empty State: quando não houver planos, retorna DTO vazio com totais zerados")
        void execute_quandoNaoPossuiPlanos_deveRetornarEmptyState() {
            when(farmerRepository.findByUserId(userId)).thenReturn(Optional.of(farmer));
            when(harvestRepository.findById(harvestId)).thenReturn(Optional.of(harvest));
            when(productionPlanRepository.findPlanSummariesByFarmer(farmerId, harvestId, false)).thenReturn(List.of());

            FarmerDashboardSummaryDTO result = useCase.execute(harvestId, farmerUser);

            assertThat(result.isEmpty()).isTrue();
            assertThat(result.farmerId()).isEqualTo(farmerId);
            assertThat(result.farmerName()).isEqualTo("João Agricultor");
            assertThat(result.communityName()).isEqualTo("Comunidade Bela Vista");
            assertThat(result.totalPlansCount()).isZero();
            assertThat(result.activePlotsCount()).isZero();
            assertThat(result.totalPlantedAreaHa()).isEqualByComparingTo("0.00");
            assertThat(result.totalTargetKg()).isEqualByComparingTo("0.00");
            assertThat(result.totalRealizedKg()).isEqualByComparingTo("0.00");
            assertThat(result.totalPendingKg()).isEqualByComparingTo("0.00");
            assertThat(result.overallCompletionPct()).isEqualByComparingTo("0.00");
            assertThat(result.plans()).isEmpty();
        }

        @Test
        @DisplayName("Consolidação de Múltiplos Planos: totaliza áreas, metas, realizados e pendentes")
        void execute_comMultiplosPlanos_deveConsolidarTotaisCorretamente() {
            when(farmerRepository.findByUserId(userId)).thenReturn(Optional.of(farmer));
            when(harvestRepository.findById(harvestId)).thenReturn(Optional.of(harvest));

            LocalDate today = LocalDate.of(2026, 6, 1);

            FarmerPlanSummary plan1 = new FarmerPlanSummary(
                    UUID.randomUUID(), UUID.randomUUID(), "Mandioca", "Branca",
                    HarvestType.SINGLE, "KG", PlanStatus.ACTIVE, "Talhão 1",
                    new BigDecimal("2.50"), new BigDecimal("5000.00"), new BigDecimal("3500.00"),
                    new BigDecimal("500.00"), LocalDate.of(2026, 1, 10),
                    LocalDate.of(2026, 5, 1), LocalDate.of(2026, 7, 1), LocalDate.of(2026, 5, 20)
            );

            FarmerPlanSummary plan2 = new FarmerPlanSummary(
                    UUID.randomUUID(), UUID.randomUUID(), "Milho", "Híbrido",
                    HarvestType.CONTINUOUS, "KG", PlanStatus.PLANNED, "Talhão 2",
                    new BigDecimal("1.50"), new BigDecimal("3000.00"), new BigDecimal("0.00"),
                    new BigDecimal("0.00"), LocalDate.of(2026, 3, 1),
                    LocalDate.of(2026, 7, 1), LocalDate.of(2026, 9, 1), null
            );

            FarmerPlanSummary plan3 = new FarmerPlanSummary(
                    UUID.randomUUID(), UUID.randomUUID(), "Feijão", "Carioca",
                    HarvestType.SINGLE, "KG", PlanStatus.FINISHED, "Talhão 3",
                    new BigDecimal("1.00"), new BigDecimal("2000.00"), new BigDecimal("2000.00"),
                    new BigDecimal("0.00"), LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 4, 1), LocalDate.of(2026, 5, 1), LocalDate.of(2026, 4, 25)
            );

            when(productionPlanRepository.findPlanSummariesByFarmer(farmerId, harvestId, false))
                    .thenReturn(List.of(plan1, plan2, plan3));

            FarmerDashboardSummaryDTO result = useCase.execute(harvestId, farmerUser, today);

            assertThat(result.isEmpty()).isFalse();
            assertThat(result.totalPlansCount()).isEqualTo(3);
            // activePlotsCount conta ACTIVE e PLANNED (plan1 e plan2), excluindo FINISHED (plan3)
            assertThat(result.activePlotsCount()).isEqualTo(2);

            // Áreas: 2.50 + 1.50 + 1.00 = 5.00 ha
            assertThat(result.totalPlantedAreaHa()).isEqualByComparingTo("5.00");

            // Metas: 5000 + 3000 + 2000 = 10000.00 kg
            assertThat(result.totalTargetKg()).isEqualByComparingTo("10000.00");

            // Realizados: 3500 + 0 + 2000 = 5500.00 kg
            assertThat(result.totalRealizedKg()).isEqualByComparingTo("5500.00");

            // Pendentes: 500 + 0 + 0 = 500.00 kg
            assertThat(result.totalPendingKg()).isEqualByComparingTo("500.00");

            // Progresso global: 5500 * 100 / 10000 = 55.00%
            assertThat(result.overallCompletionPct()).isEqualByComparingTo("55.00");

            assertThat(result.plans()).hasSize(3);
            assertThat(result.plans().get(0).completionPct()).isEqualByComparingTo("70.00"); // 3500/5000 = 70.00%
            assertThat(result.plans().get(1).completionPct()).isEqualByComparingTo("0.00");
            assertThat(result.plans().get(2).completionPct()).isEqualByComparingTo("100.00");
        }
    }

    @Nested
    @DisplayName("Classificação de PlanProgressStatus")
    class ProgressStatusClassificationTests {

        private FarmerPlanSummary createSummary(
                PlanStatus status,
                BigDecimal targetKg,
                BigDecimal realizedKg,
                LocalDate harvestStart,
                LocalDate harvestEnd
        ) {
            return new FarmerPlanSummary(
                    UUID.randomUUID(), UUID.randomUUID(), "Cultura", "Variedade",
                    HarvestType.SINGLE, "KG", status, "Talhão",
                    new BigDecimal("1.00"), targetKg, realizedKg, BigDecimal.ZERO,
                    LocalDate.of(2026, 1, 1), harvestStart, harvestEnd, null
            );
        }

        @Test
        @DisplayName("GOAL_REACHED: quando completionPct >= 100%, mesmo que após a data final")
        void progressStatus_quandoAtingirMeta_deveSerGoalReached() {
            LocalDate today = LocalDate.of(2026, 10, 1);
            FarmerPlanSummary summary = createSummary(
                    PlanStatus.ACTIVE,
                    new BigDecimal("1000.00"),
                    new BigDecimal("1050.00"),
                    LocalDate.of(2026, 6, 1),
                    LocalDate.of(2026, 8, 1)
            );

            BigDecimal completionPct = new BigDecimal("105.00");
            PlanProgressStatus status = useCase.determineProgressStatus(
                    summary, completionPct, new BigDecimal("1050.00"), new BigDecimal("1000.00"), today
            );

            assertThat(status).isEqualTo(PlanProgressStatus.GOAL_REACHED);
        }

        @Test
        @DisplayName("DELAYED: quando completionPct < 100% e referenceDate for após expectedHarvestEnd")
        void progressStatus_quandoAtrasado_deveSerDelayed() {
            LocalDate today = LocalDate.of(2026, 8, 15);
            FarmerPlanSummary summary = createSummary(
                    PlanStatus.ACTIVE,
                    new BigDecimal("1000.00"),
                    new BigDecimal("700.00"),
                    LocalDate.of(2026, 6, 1),
                    LocalDate.of(2026, 8, 1)
            );

            BigDecimal completionPct = new BigDecimal("70.00");
            PlanProgressStatus status = useCase.determineProgressStatus(
                    summary, completionPct, new BigDecimal("700.00"), new BigDecimal("1000.00"), today
            );

            assertThat(status).isEqualTo(PlanProgressStatus.DELAYED);
        }

        @Test
        @DisplayName("NOT_STARTED: quando realizedKg == 0 e status == PLANNED")
        void progressStatus_quandoZeroRealizadoEPlanejado_deveSerNotStarted() {
            LocalDate today = LocalDate.of(2026, 6, 15);
            FarmerPlanSummary summary = createSummary(
                    PlanStatus.PLANNED,
                    new BigDecimal("1000.00"),
                    BigDecimal.ZERO,
                    LocalDate.of(2026, 6, 1),
                    LocalDate.of(2026, 8, 1)
            );

            BigDecimal completionPct = BigDecimal.ZERO;
            PlanProgressStatus status = useCase.determineProgressStatus(
                    summary, completionPct, BigDecimal.ZERO, new BigDecimal("1000.00"), today
            );

            assertThat(status).isEqualTo(PlanProgressStatus.NOT_STARTED);
        }

        @Test
        @DisplayName("NOT_STARTED: quando realizedKg == 0 e referenceDate for antes de expectedHarvestStart")
        void progressStatus_quandoAntesDoInicioEZeroRealizado_deveSerNotStarted() {
            LocalDate today = LocalDate.of(2026, 5, 15);
            FarmerPlanSummary summary = createSummary(
                    PlanStatus.ACTIVE,
                    new BigDecimal("1000.00"),
                    BigDecimal.ZERO,
                    LocalDate.of(2026, 6, 1),
                    LocalDate.of(2026, 8, 1)
            );

            BigDecimal completionPct = BigDecimal.ZERO;
            PlanProgressStatus status = useCase.determineProgressStatus(
                    summary, completionPct, BigDecimal.ZERO, new BigDecimal("1000.00"), today
            );

            assertThat(status).isEqualTo(PlanProgressStatus.NOT_STARTED);
        }

        @Test
        @DisplayName("BELOW_EXPECTED: em janela de colheita com realizado abaixo de 70% da curva esperada")
        void progressStatus_quandoAbaixoDoRitmoEsperado_deveSerBelowExpected() {
            // Janela: 01/06 a 30/06 (30 dias). Meta = 1000 kg.
            // No dia 15/06 (15 dias = 50% do tempo), ritmo teórico = 500 kg. Limiar de 70% = 350 kg.
            // Com realizado = 200 kg (< 350 kg) -> BELOW_EXPECTED.
            LocalDate today = LocalDate.of(2026, 6, 15);
            FarmerPlanSummary summary = createSummary(
                    PlanStatus.ACTIVE,
                    new BigDecimal("1000.00"),
                    new BigDecimal("200.00"),
                    LocalDate.of(2026, 6, 1),
                    LocalDate.of(2026, 6, 30)
            );

            BigDecimal completionPct = new BigDecimal("20.00");
            PlanProgressStatus status = useCase.determineProgressStatus(
                    summary, completionPct, new BigDecimal("200.00"), new BigDecimal("1000.00"), today
            );

            assertThat(status).isEqualTo(PlanProgressStatus.BELOW_EXPECTED);
        }

        @Test
        @DisplayName("ON_TRACK: em janela de colheita dentro do ritmo esperado (>= 70% da curva teórica)")
        void progressStatus_quandoDentroDoRitmoEsperado_deveSerOnTrack() {
            // No dia 15/06 (15 dias = 50% do tempo), ritmo teórico = 500 kg. Limiar de 70% = 350 kg.
            // Com realizado = 400 kg (>= 350 kg) -> ON_TRACK.
            LocalDate today = LocalDate.of(2026, 6, 15);
            FarmerPlanSummary summary = createSummary(
                    PlanStatus.ACTIVE,
                    new BigDecimal("1000.00"),
                    new BigDecimal("400.00"),
                    LocalDate.of(2026, 6, 1),
                    LocalDate.of(2026, 6, 30)
            );

            BigDecimal completionPct = new BigDecimal("40.00");
            PlanProgressStatus status = useCase.determineProgressStatus(
                    summary, completionPct, new BigDecimal("400.00"), new BigDecimal("1000.00"), today
            );

            assertThat(status).isEqualTo(PlanProgressStatus.ON_TRACK);
        }
    }
}
