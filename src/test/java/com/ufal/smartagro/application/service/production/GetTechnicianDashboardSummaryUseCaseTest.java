package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.adapters.in.web.dto.analytics.TechnicianDashboardSummaryDTO;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.CropNotFoundException;
import com.ufal.smartagro.domain.exception.HarvestNotFoundException;
import com.ufal.smartagro.domain.model.Crop;
import com.ufal.smartagro.domain.model.Harvest;
import com.ufal.smartagro.domain.model.TechnicalAssistance;
import com.ufal.smartagro.domain.model.Technician;
import com.ufal.smartagro.domain.model.TechnicianAssistedFarmerRow;
import com.ufal.smartagro.domain.model.TechnicianPlanSummary;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.HarvestType;
import com.ufal.smartagro.domain.model.enums.PlanProgressStatus;
import com.ufal.smartagro.domain.model.enums.PlanStatus;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.model.enums.TechnicianWarningType;
import com.ufal.smartagro.domain.model.enums.WarningSeverity;
import com.ufal.smartagro.domain.port.out.CropRepository;
import com.ufal.smartagro.domain.port.out.HarvestRepository;
import com.ufal.smartagro.domain.port.out.ProductionPlanRepository;
import com.ufal.smartagro.domain.port.out.TechnicalAssistanceRepository;
import com.ufal.smartagro.domain.port.out.TechnicianRepository;
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
class GetTechnicianDashboardSummaryUseCaseTest {

    @Mock
    private TechnicianRepository technicianRepository;

    @Mock
    private TechnicalAssistanceRepository technicalAssistanceRepository;

    @Mock
    private HarvestRepository harvestRepository;

    @Mock
    private CropRepository cropRepository;

    @Mock
    private ProductionPlanRepository productionPlanRepository;

    @InjectMocks
    private GetTechnicianDashboardSummaryUseCase useCase;

    private User technicianUser;
    private Technician technician;
    private UUID userId;
    private UUID technicianId;
    private UUID harvestId;
    private Harvest harvest;
    private UUID communityId1;
    private UUID communityId2;
    private UUID farmerId1;
    private UUID farmerId2;
    private UUID farmerId3;
    private UUID cropId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        technicianId = UUID.randomUUID();
        harvestId = UUID.randomUUID();
        communityId1 = UUID.randomUUID();
        communityId2 = UUID.randomUUID();
        farmerId1 = UUID.randomUUID();
        farmerId2 = UUID.randomUUID();
        farmerId3 = UUID.randomUUID();
        cropId = UUID.randomUUID();

        technicianUser = User.builder()
                .id(userId)
                .fullName("Carlos Eduardo Mendes")
                .email("carlos.tecnico@agro.com")
                .role(Role.TECHNICIAN)
                .build();

        technician = Technician.builder()
                .id(technicianId)
                .user(technicianUser)
                .specialty("Agronomia")
                .build();

        harvest = new Harvest(
                harvestId,
                "Safra 2026",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31)
        );
    }

    @Nested
    @DisplayName("Validações de RBAC e Perfil do Técnico")
    class RbacAndProfileTests {

        @Test
        @DisplayName("Deve lançar AccessDeniedException quando loggedUser for nulo")
        void execute_quandoUsuarioNulo_deveLancarAccessDeniedException() {
            assertThatThrownBy(() -> useCase.execute(harvestId, null, null, null))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("Acesso negado");
        }

        @Test
        @DisplayName("Deve lançar AccessDeniedException quando role não for TECHNICIAN")
        void execute_quandoRoleNaoForTechnician_deveLancarAccessDeniedException() {
            User farmerUser = User.builder()
                    .id(userId)
                    .role(Role.FARMER)
                    .build();

            assertThatThrownBy(() -> useCase.execute(harvestId, null, null, farmerUser))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("Apenas técnicos");
        }

        @Test
        @DisplayName("Deve lançar AccessDeniedException quando técnico não for encontrado")
        void execute_quandoTecnicoNaoEncontrado_deveLancarAccessDeniedException() {
            when(technicianRepository.findByUserId(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(harvestId, null, null, technicianUser))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("Perfil de técnico não encontrado");
        }

        @Test
        @DisplayName("Deve lançar AccessDeniedException quando técnico estiver deletado")
        void execute_quandoTecnicoDeletado_deveLancarAccessDeniedException() {
            Technician deleted = Technician.builder()
                    .id(technicianId)
                    .user(technicianUser)
                    .deletedAt(LocalDateTime.now())
                    .build();

            when(technicianRepository.findByUserId(userId)).thenReturn(Optional.of(deleted));

            assertThatThrownBy(() -> useCase.execute(harvestId, null, null, technicianUser))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("inativo ou excluído");
        }

        @Test
        @DisplayName("Deve retornar Empty State quando técnico não tiver vínculos em technical_assistances")
        void execute_quandoTecnicoSemVinculosEmTechnicalAssistances_deveRetornarEmptyState() {
            when(technicianRepository.findByUserId(userId)).thenReturn(Optional.of(technician));
            when(harvestRepository.findById(harvestId)).thenReturn(Optional.of(harvest));
            when(productionPlanRepository.findAssistedFarmersByTechnician(technicianId, null))
                    .thenReturn(List.of());

            TechnicianDashboardSummaryDTO result = useCase.execute(harvestId, null, null, technicianUser);

            assertThat(result.isEmpty()).isTrue();
            assertThat(result.technicianId()).isEqualTo(technicianId);
            assertThat(result.technicianName()).isEqualTo("Carlos Eduardo Mendes");
            assertThat(result.totalAssistedCommunitiesCount()).isZero();
            assertThat(result.totalAssistedFarmersCount()).isZero();
            assertThat(result.totalPlansCount()).isZero();
            assertThat(result.warnings()).isEmpty();
            assertThat(result.communities()).isEmpty();
            assertThat(result.farmers()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Validações de Filtros (communityId, cropId, harvestId)")
    class FilterValidationTests {

        @Test
        @DisplayName("Deve lançar AccessDeniedException quando communityId não for assistida pelo técnico")
        void execute_quandoComunidadeNaoAssistida_deveLancarAccessDeniedException() {
            when(technicianRepository.findByUserId(userId)).thenReturn(Optional.of(technician));
            when(technicalAssistanceRepository.findActiveByTechnicianAndCommunity(technicianId, communityId1))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(harvestId, communityId1, null, technicianUser))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("Comunidade não assistida");
        }

        @Test
        @DisplayName("Deve lançar CropNotFoundException quando cropId for inexistente")
        void execute_quandoCulturaInexistente_deveLancarCropNotFoundException() {
            when(technicianRepository.findByUserId(userId)).thenReturn(Optional.of(technician));
            when(cropRepository.findById(cropId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(harvestId, null, cropId, technicianUser))
                    .isInstanceOf(CropNotFoundException.class)
                    .hasMessageContaining("Cultura não encontrada");
        }

        @Test
        @DisplayName("Deve lançar HarvestNotFoundException quando harvestId for inexistente")
        void execute_quandoSafraInexistente_deveLancarHarvestNotFoundException() {
            when(technicianRepository.findByUserId(userId)).thenReturn(Optional.of(technician));
            when(harvestRepository.findById(harvestId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(harvestId, null, null, technicianUser))
                    .isInstanceOf(HarvestNotFoundException.class)
                    .hasMessageContaining("Safra não encontrada");
        }

        @Test
        @DisplayName("Deve aplicar filtro de comunidade e cultura válidos e repassar aos repositórios")
        void execute_quandoFiltrosValidos_deveRepassarCorretamente() {
            when(technicianRepository.findByUserId(userId)).thenReturn(Optional.of(technician));
            when(technicalAssistanceRepository.findActiveByTechnicianAndCommunity(technicianId, communityId1))
                    .thenReturn(Optional.of(mock(TechnicalAssistance.class)));
            when(cropRepository.findById(cropId)).thenReturn(Optional.of(mock(Crop.class)));
            when(harvestRepository.findById(harvestId)).thenReturn(Optional.of(harvest));

            TechnicianAssistedFarmerRow row = new TechnicianAssistedFarmerRow(
                    communityId1, "Comunidade São José", farmerId1, "João da Silva", true
            );
            when(productionPlanRepository.findAssistedFarmersByTechnician(technicianId, communityId1))
                    .thenReturn(List.of(row));
            when(productionPlanRepository.findPlanSummariesByTechnician(technicianId, communityId1, cropId, harvestId, false))
                    .thenReturn(List.of());

            TechnicianDashboardSummaryDTO result = useCase.execute(harvestId, communityId1, cropId, technicianUser);

            assertThat(result.appliedCommunityId()).isEqualTo(communityId1);
            assertThat(result.appliedCropId()).isEqualTo(cropId);
            assertThat(result.totalAssistedCommunitiesCount()).isEqualTo(1);
            assertThat(result.totalAssistedFarmersCount()).isEqualTo(1);
            verify(productionPlanRepository).findAssistedFarmersByTechnician(technicianId, communityId1);
            verify(productionPlanRepository).findPlanSummariesByTechnician(technicianId, communityId1, cropId, harvestId, false);
        }
    }

    @Nested
    @DisplayName("Resolução de Safra e Fallback Gracioso")
    class HarvestResolutionAndFallbackTests {

        @Test
        @DisplayName("Quando harvestId for omitido, deve resolver safra ativa na data de referência")
        void execute_quandoSafraOmitida_deveResolverSafraAtiva() {
            LocalDate referenceDate = LocalDate.of(2026, 6, 15);
            when(technicianRepository.findByUserId(userId)).thenReturn(Optional.of(technician));
            when(harvestRepository.findActiveHarvest(referenceDate)).thenReturn(Optional.of(harvest));

            TechnicianAssistedFarmerRow row = new TechnicianAssistedFarmerRow(
                    communityId1, "Comunidade São José", farmerId1, "João", true
            );
            when(productionPlanRepository.findAssistedFarmersByTechnician(technicianId, null))
                    .thenReturn(List.of(row));
            when(productionPlanRepository.findPlanSummariesByTechnician(technicianId, null, null, harvestId, true))
                    .thenReturn(List.of());

            TechnicianDashboardSummaryDTO result = useCase.execute(null, null, null, technicianUser, referenceDate);

            assertThat(result.currentHarvest()).isNotNull();
            assertThat(result.currentHarvest().id()).isEqualTo(harvestId);
            verify(productionPlanRepository).findPlanSummariesByTechnician(technicianId, null, null, harvestId, true);
        }

        @Test
        @DisplayName("Fallback gracioso: quando safra ativa não tiver planos, deve buscar planos ACTIVE e PLANNED de outras safras")
        void execute_quandoSafraAtivaSemPlanos_deveUsarFallbackParaPlanosEmAndamento() {
            LocalDate referenceDate = LocalDate.of(2026, 6, 15);
            when(technicianRepository.findByUserId(userId)).thenReturn(Optional.of(technician));
            when(harvestRepository.findActiveHarvest(referenceDate)).thenReturn(Optional.of(harvest));

            TechnicianAssistedFarmerRow row = new TechnicianAssistedFarmerRow(
                    communityId1, "Comunidade São José", farmerId1, "João", true
            );
            when(productionPlanRepository.findAssistedFarmersByTechnician(technicianId, null))
                    .thenReturn(List.of(row));

            // Safra ativa vazia
            when(productionPlanRepository.findPlanSummariesByTechnician(technicianId, null, null, harvestId, true))
                    .thenReturn(List.of());

            TechnicianPlanSummary activePlan = new TechnicianPlanSummary(
                    UUID.randomUUID(), farmerId1, "João", communityId1, "Comunidade São José",
                    cropId, "Milho", "Híbrido", HarvestType.SINGLE, "KG", PlanStatus.ACTIVE,
                    new BigDecimal("2.00"), new BigDecimal("4000.00"), new BigDecimal("2000.00"), BigDecimal.ZERO,
                    0L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 5, 1), LocalDate.of(2026, 7, 1),
                    LocalDate.of(2026, 6, 1)
            );
            TechnicianPlanSummary finishedPlan = new TechnicianPlanSummary(
                    UUID.randomUUID(), farmerId1, "João", communityId1, "Comunidade São José",
                    cropId, "Feijão", "Preto", HarvestType.SINGLE, "KG", PlanStatus.FINISHED,
                    new BigDecimal("1.00"), new BigDecimal("2000.00"), new BigDecimal("2000.00"), BigDecimal.ZERO,
                    0L, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 4, 1), LocalDate.of(2025, 5, 1),
                    LocalDate.of(2025, 5, 1)
            );

            // Consulta sem filtro de safra
            when(productionPlanRepository.findPlanSummariesByTechnician(technicianId, null, null, null, false))
                    .thenReturn(List.of(activePlan, finishedPlan));

            TechnicianDashboardSummaryDTO result = useCase.execute(null, null, null, technicianUser, referenceDate);

            assertThat(result.isEmpty()).isFalse();
            assertThat(result.totalPlansCount()).isEqualTo(1);
            assertThat(result.farmers().get(0).plansCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Motor de Alertas (warnings)")
    class WarningEngineTests {

        private LocalDate refDate;

        @BeforeEach
        void init() {
            refDate = LocalDate.of(2026, 10, 21);
            when(technicianRepository.findByUserId(userId)).thenReturn(Optional.of(technician));
            when(harvestRepository.findById(harvestId)).thenReturn(Optional.of(harvest));

            TechnicianAssistedFarmerRow row = new TechnicianAssistedFarmerRow(
                    communityId1, "Comunidade São José", farmerId1, "Severino", true
            );
            when(productionPlanRepository.findAssistedFarmersByTechnician(technicianId, null))
                    .thenReturn(List.of(row));
        }

        @Test
        @DisplayName("Deve gerar alerta INACTIVE_EXECUTIONS quando último apontamento foi há mais de 15 dias (severidade MEDIUM)")
        void deveGerarAlertaInactiveExecutionsQuandoUltimoApontamentoMaisDe15Dias() {
            LocalDate lastExec = refDate.minusDays(18); // 18 dias sem apontamento -> MEDIUM
            TechnicianPlanSummary plan = new TechnicianPlanSummary(
                    UUID.randomUUID(), farmerId1, "Severino", communityId1, "Comunidade São José",
                    cropId, "Tomate", "Cereja", HarvestType.SINGLE, "KG", PlanStatus.ACTIVE,
                    new BigDecimal("1.00"), new BigDecimal("2000.00"), new BigDecimal("1000.00"), BigDecimal.ZERO,
                    0L, refDate.minusDays(60), refDate.minusDays(30), refDate.plusDays(30),
                    lastExec
            );

            when(productionPlanRepository.findPlanSummariesByTechnician(technicianId, null, null, harvestId, false))
                    .thenReturn(List.of(plan));

            TechnicianDashboardSummaryDTO result = useCase.execute(harvestId, null, null, technicianUser, refDate);

            assertThat(result.warnings()).hasSize(1);
            var warning = result.warnings().get(0);
            assertThat(warning.warningTypes()).containsExactly(TechnicianWarningType.INACTIVE_EXECUTIONS);
            assertThat(warning.severity()).isEqualTo(WarningSeverity.MEDIUM);
            assertThat(warning.daysWithoutExecution()).isEqualTo(18);
            assertThat(warning.message()).contains("Sem apontamentos há 18 dias");
        }

        @Test
        @DisplayName("Deve gerar alerta INACTIVE_EXECUTIONS com severidade HIGH quando sem apontamentos há mais de 30 dias")
        void deveGerarAlertaInactiveExecutionsSeveridadeHighQuandoSemApontamentoMaisDe30Dias() {
            LocalDate lastExec = refDate.minusDays(35); // 35 dias -> HIGH
            TechnicianPlanSummary plan = new TechnicianPlanSummary(
                    UUID.randomUUID(), farmerId1, "Severino", communityId1, "Comunidade São José",
                    cropId, "Tomate", "Cereja", HarvestType.SINGLE, "KG", PlanStatus.ACTIVE,
                    new BigDecimal("1.00"), new BigDecimal("2000.00"), new BigDecimal("1000.00"), BigDecimal.ZERO,
                    0L, refDate.minusDays(60), refDate.minusDays(40), refDate.plusDays(20),
                    lastExec
            );

            when(productionPlanRepository.findPlanSummariesByTechnician(technicianId, null, null, harvestId, false))
                    .thenReturn(List.of(plan));

            TechnicianDashboardSummaryDTO result = useCase.execute(harvestId, null, null, technicianUser, refDate);

            assertThat(result.warnings()).hasSize(1);
            var warning = result.warnings().get(0);
            assertThat(warning.severity()).isEqualTo(WarningSeverity.HIGH);
            assertThat(warning.daysWithoutExecution()).isEqualTo(35);
        }

        @Test
        @DisplayName("Não deve gerar alerta INACTIVE_EXECUTIONS quando último apontamento foi há 5 dias")
        void naoDeveGerarAlertaInactiveExecutionsQuandoApontamentoRecente() {
            LocalDate lastExec = refDate.minusDays(5); // 5 dias <= 15 dias -> sem alerta
            TechnicianPlanSummary plan = new TechnicianPlanSummary(
                    UUID.randomUUID(), farmerId1, "Severino", communityId1, "Comunidade São José",
                    cropId, "Tomate", "Cereja", HarvestType.SINGLE, "KG", PlanStatus.ACTIVE,
                    new BigDecimal("1.00"), new BigDecimal("2000.00"), new BigDecimal("1000.00"), BigDecimal.ZERO,
                    0L, refDate.minusDays(60), refDate.minusDays(30), refDate.plusDays(30),
                    lastExec
            );

            when(productionPlanRepository.findPlanSummariesByTechnician(technicianId, null, null, harvestId, false))
                    .thenReturn(List.of(plan));

            TechnicianDashboardSummaryDTO result = useCase.execute(harvestId, null, null, technicianUser, refDate);

            assertThat(result.warnings()).isEmpty();
        }

        @Test
        @DisplayName("Deve gerar alerta LOW_YIELD_RISK quando rendimento estiver mais de 30% abaixo da curva linear")
        void deveGerarAlertaLowYieldRiskQuandoRendimentoAbaixoDe70PorcentoDaCurva() {
            // Janela de colheita: 100 dias (refDate no dia 50, exatamente metade)
            // Esperado teórico na metade: 50% de 10.000 kg = 5.000 kg
            // Limite de 70%: 3.500 kg
            // Realizado: 3.000 kg (< 3.500 kg e >= 2.500 kg) -> MEDIUM (déficit de 40%)
            LocalDate start = refDate.minusDays(49);
            LocalDate end = refDate.plusDays(50);
            LocalDate lastExec = refDate.minusDays(2); // recente para não disparar inatividade

            TechnicianPlanSummary plan = new TechnicianPlanSummary(
                    UUID.randomUUID(), farmerId1, "Severino", communityId1, "Comunidade São José",
                    cropId, "Milho", "Híbrido", HarvestType.SINGLE, "KG", PlanStatus.ACTIVE,
                    new BigDecimal("5.00"), new BigDecimal("10000.00"), new BigDecimal("3000.00"), BigDecimal.ZERO,
                    0L, start.minusDays(30), start, end,
                    lastExec
            );

            when(productionPlanRepository.findPlanSummariesByTechnician(technicianId, null, null, harvestId, false))
                    .thenReturn(List.of(plan));

            TechnicianDashboardSummaryDTO result = useCase.execute(harvestId, null, null, technicianUser, refDate);

            assertThat(result.warnings()).hasSize(1);
            var warning = result.warnings().get(0);
            assertThat(warning.warningTypes()).containsExactly(TechnicianWarningType.LOW_YIELD_RISK);
            assertThat(warning.severity()).isEqualTo(WarningSeverity.MEDIUM);
            assertThat(warning.progressStatus()).isEqualTo(PlanProgressStatus.BELOW_EXPECTED);
            assertThat(warning.message()).contains("40,0% abaixo do esperado");
        }

        @Test
        @DisplayName("Deve gerar alerta LOW_YIELD_RISK com severidade HIGH quando rendimento estiver mais de 50% abaixo da curva")
        void deveGerarAlertaLowYieldRiskSeveridadeHighQuandoRendimentoAbaixoDe50PorcentoDaCurva() {
            LocalDate start = refDate.minusDays(49);
            LocalDate end = refDate.plusDays(50);
            LocalDate lastExec = refDate.minusDays(2);

            // Esperado: 5.000 kg. Realizado: 2.000 kg (< 50% de 5.000 = 2.500) -> HIGH
            TechnicianPlanSummary plan = new TechnicianPlanSummary(
                    UUID.randomUUID(), farmerId1, "Severino", communityId1, "Comunidade São José",
                    cropId, "Milho", "Híbrido", HarvestType.SINGLE, "KG", PlanStatus.ACTIVE,
                    new BigDecimal("5.00"), new BigDecimal("10000.00"), new BigDecimal("2000.00"), BigDecimal.ZERO,
                    0L, start.minusDays(30), start, end,
                    lastExec
            );

            when(productionPlanRepository.findPlanSummariesByTechnician(technicianId, null, null, harvestId, false))
                    .thenReturn(List.of(plan));

            TechnicianDashboardSummaryDTO result = useCase.execute(harvestId, null, null, technicianUser, refDate);

            assertThat(result.warnings()).hasSize(1);
            var warning = result.warnings().get(0);
            assertThat(warning.warningTypes()).containsExactly(TechnicianWarningType.LOW_YIELD_RISK);
            assertThat(warning.severity()).isEqualTo(WarningSeverity.HIGH);
        }

        @Test
        @DisplayName("Deve gerar alerta HARVEST_OVERDUE com severidade HIGH quando fim de ciclo ultrapassado sem atingir meta")
        void deveGerarAlertaHarvestOverdueQuandoFimDeCicloUltrapassado() {
            LocalDate harvestEnd = refDate.minusDays(10); // 10 dias atrasado
            LocalDate lastExec = refDate.minusDays(12);

            TechnicianPlanSummary plan = new TechnicianPlanSummary(
                    UUID.randomUUID(), farmerId1, "Severino", communityId1, "Comunidade São José",
                    cropId, "Feijão", "Carioca", HarvestType.SINGLE, "KG", PlanStatus.ACTIVE,
                    new BigDecimal("2.00"), new BigDecimal("3000.00"), new BigDecimal("2100.00"), BigDecimal.ZERO,
                    0L, harvestEnd.minusDays(90), harvestEnd.minusDays(30), harvestEnd,
                    lastExec
            );

            when(productionPlanRepository.findPlanSummariesByTechnician(technicianId, null, null, harvestId, false))
                    .thenReturn(List.of(plan));

            TechnicianDashboardSummaryDTO result = useCase.execute(harvestId, null, null, technicianUser, refDate);

            assertThat(result.warnings()).hasSize(1);
            var warning = result.warnings().get(0);
            assertThat(warning.warningTypes()).contains(TechnicianWarningType.HARVEST_OVERDUE);
            assertThat(warning.severity()).isEqualTo(WarningSeverity.HIGH);
            assertThat(warning.progressStatus()).isEqualTo(PlanProgressStatus.DELAYED);
            assertThat(warning.message()).contains("Janela de colheita encerrada há 10 dias");
        }

        @Test
        @DisplayName("Deve desduplicar alertas quando o mesmo plano infringir inatividade e quebra de safra, gerando 1 único cartão com HIGH")
        void deveDesduplicarAlertasQuandoMesmoPlanoInfringirMultiplasRegras() {
            LocalDate start = refDate.minusDays(49);
            LocalDate end = refDate.plusDays(50);
            LocalDate lastExec = refDate.minusDays(19); // > 15 dias sem apontamento (INACTIVE_EXECUTIONS)

            // Esperado teórico: 2.500 kg. Realizado: 1.450 kg (déficit de 42,0% abaixo do esperado)
            TechnicianPlanSummary plan = new TechnicianPlanSummary(
                    UUID.randomUUID(), farmerId1, "Severino", communityId1, "Comunidade São José",
                    cropId, "Tomate", "Cereja", HarvestType.SINGLE, "KG", PlanStatus.ACTIVE,
                    new BigDecimal("2.00"), new BigDecimal("5000.00"), new BigDecimal("1450.00"), new BigDecimal("200.00"),
                    0L, start.minusDays(30), start, end,
                    lastExec
            );

            when(productionPlanRepository.findPlanSummariesByTechnician(technicianId, null, null, harvestId, false))
                    .thenReturn(List.of(plan));

            TechnicianDashboardSummaryDTO result = useCase.execute(harvestId, null, null, technicianUser, refDate);

            // Apenas 1 cartão gerado para o plano
            assertThat(result.warnings()).hasSize(1);
            var warning = result.warnings().get(0);
            assertThat(warning.warningTypes()).containsExactly(
                    TechnicianWarningType.INACTIVE_EXECUTIONS,
                    TechnicianWarningType.LOW_YIELD_RISK
            );
            assertThat(warning.severity()).isEqualTo(WarningSeverity.HIGH);
            assertThat(warning.message()).contains("Sem apontamentos há 19 dias");
            assertThat(warning.message()).contains("Produção 42,0% abaixo do esperado para o período");
            assertThat(warning.message()).contains(" • ");
        }

        @Test
        @DisplayName("Não deve gerar alerta quando plano tiver atingido 100% da meta")
        void naoDeveGerarAlertaQuandoPlanoAtingiuMeta() {
            LocalDate harvestEnd = refDate.minusDays(10);
            TechnicianPlanSummary plan = new TechnicianPlanSummary(
                    UUID.randomUUID(), farmerId1, "Severino", communityId1, "Comunidade São José",
                    cropId, "Feijão", "Carioca", HarvestType.SINGLE, "KG", PlanStatus.ACTIVE,
                    new BigDecimal("2.00"), new BigDecimal("3000.00"), new BigDecimal("3000.00"), BigDecimal.ZERO,
                    0L, harvestEnd.minusDays(90), harvestEnd.minusDays(30), harvestEnd,
                    refDate.minusDays(40)
            );

            when(productionPlanRepository.findPlanSummariesByTechnician(technicianId, null, null, harvestId, false))
                    .thenReturn(List.of(plan));

            TechnicianDashboardSummaryDTO result = useCase.execute(harvestId, null, null, technicianUser, refDate);

            assertThat(result.warnings()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Consolidação Multi-Comunidade e Multi-Agricultor")
    class PortfolioConsolidationTests {

        @Test
        @DisplayName("Técnico que assiste 2 comunidades e 3 agricultores consolida métricas e inclui agricultores sem planos com 0.00")
        void deveConsolidarMultiplasComunidadesEAgricultoresCorretamente() {
            when(technicianRepository.findByUserId(userId)).thenReturn(Optional.of(technician));
            when(harvestRepository.findById(harvestId)).thenReturn(Optional.of(harvest));

            // Carteira do técnico: Comunidade A (João e Maria) e Comunidade B (Pedro)
            List<TechnicianAssistedFarmerRow> assistedRows = List.of(
                    new TechnicianAssistedFarmerRow(communityId1, "Comunidade A", farmerId1, "João", true),
                    new TechnicianAssistedFarmerRow(communityId1, "Comunidade A", farmerId2, "Maria", false),
                    new TechnicianAssistedFarmerRow(communityId2, "Comunidade B", farmerId3, "Pedro", true)
            );
            when(productionPlanRepository.findAssistedFarmersByTechnician(technicianId, null))
                    .thenReturn(assistedRows);

            // Apenas João e Maria têm planos. Pedro (Comunidade B) não tem planos cadastrados na safra!
            TechnicianPlanSummary planJoao = new TechnicianPlanSummary(
                    UUID.randomUUID(), farmerId1, "João", communityId1, "Comunidade A",
                    cropId, "Milho", "Híbrido", HarvestType.SINGLE, "KG", PlanStatus.ACTIVE,
                    new BigDecimal("3.00"), new BigDecimal("6000.00"), new BigDecimal("4000.00"), new BigDecimal("500.00"),
                    2L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 4, 1), LocalDate.of(2026, 7, 1),
                    LocalDate.of(2026, 5, 10)
            );
            TechnicianPlanSummary planMaria = new TechnicianPlanSummary(
                    UUID.randomUUID(), farmerId2, "Maria", communityId1, "Comunidade A",
                    cropId, "Tomate", "Cereja", HarvestType.SINGLE, "KG", PlanStatus.ACTIVE,
                    new BigDecimal("2.00"), new BigDecimal("4000.00"), new BigDecimal("2500.00"), new BigDecimal("300.00"),
                    1L, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 5, 1), LocalDate.of(2026, 8, 1),
                    LocalDate.of(2026, 6, 1)
            );

            when(productionPlanRepository.findPlanSummariesByTechnician(technicianId, null, null, harvestId, false))
                    .thenReturn(List.of(planJoao, planMaria));

            TechnicianDashboardSummaryDTO result = useCase.execute(harvestId, null, null, technicianUser);

            // Validações Macro
            assertThat(result.isEmpty()).isFalse();
            assertThat(result.totalAssistedCommunitiesCount()).isEqualTo(2);
            assertThat(result.totalAssistedFarmersCount()).isEqualTo(3);
            assertThat(result.totalPlansCount()).isEqualTo(2);
            assertThat(result.activePlansCount()).isEqualTo(2);
            assertThat(result.totalPlantedAreaHa()).isEqualByComparingTo("5.00");
            assertThat(result.totalTargetKg()).isEqualByComparingTo("10000.00");
            assertThat(result.totalRealizedKg()).isEqualByComparingTo("6500.00");
            assertThat(result.totalPendingKg()).isEqualByComparingTo("800.00");
            assertThat(result.pendingExecutionsCount()).isEqualTo(3);
            assertThat(result.overallCompletionPct()).isEqualByComparingTo("65.00");

            // Validações por Comunidade
            assertThat(result.communities()).hasSize(2);
            var commA = result.communities().stream().filter(c -> c.communityId().equals(communityId1)).findFirst().orElseThrow();
            assertThat(commA.assistedFarmersCount()).isEqualTo(2);
            assertThat(commA.farmersWithPlansCount()).isEqualTo(2);
            assertThat(commA.plansCount()).isEqualTo(2);
            assertThat(commA.targetKg()).isEqualByComparingTo("10000.00");
            assertThat(commA.realizedKg()).isEqualByComparingTo("6500.00");

            var commB = result.communities().stream().filter(c -> c.communityId().equals(communityId2)).findFirst().orElseThrow();
            assertThat(commB.assistedFarmersCount()).isEqualTo(1);
            assertThat(commB.farmersWithPlansCount()).isZero();
            assertThat(commB.plansCount()).isZero();
            assertThat(commB.plantedAreaHa()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(commB.targetKg()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(commB.realizedKg()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(commB.completionPct()).isEqualByComparingTo(BigDecimal.ZERO);

            // Validações por Agricultor
            assertThat(result.farmers()).hasSize(3);
            var farmerPedro = result.farmers().stream().filter(f -> f.farmerId().equals(farmerId3)).findFirst().orElseThrow();
            assertThat(farmerPedro.farmerName()).isEqualTo("Pedro");
            assertThat(farmerPedro.isCompliant()).isTrue();
            assertThat(farmerPedro.plansCount()).isZero();
            assertThat(farmerPedro.activePlansCount()).isZero();
            assertThat(farmerPedro.plantedAreaHa()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(farmerPedro.targetKg()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(farmerPedro.realizedKg()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(farmerPedro.lastExecutionDate()).isNull();
        }
        @Test
        @DisplayName("Quando cropId for informado, deve filtrar lista de agricultores trazendo apenas os que possuem planos da cultura (Secao 3.3)")
        void execute_quandoCropIdInformado_deveFiltrarListaFarmers() {
            when(technicianRepository.findByUserId(userId)).thenReturn(Optional.of(technician));
            when(harvestRepository.findById(harvestId)).thenReturn(Optional.of(harvest));
            when(cropRepository.findById(cropId)).thenReturn(Optional.of(mock(Crop.class)));

            List<TechnicianAssistedFarmerRow> assistedRows = List.of(
                    new TechnicianAssistedFarmerRow(communityId1, "Comunidade A", farmerId1, "João", true),
                    new TechnicianAssistedFarmerRow(communityId1, "Comunidade A", farmerId2, "Maria", false),
                    new TechnicianAssistedFarmerRow(communityId2, "Comunidade B", farmerId3, "Pedro", true)
            );
            when(productionPlanRepository.findAssistedFarmersByTechnician(technicianId, null))
                    .thenReturn(assistedRows);

            // Apenas João tem plano para este cropId
            TechnicianPlanSummary planJoao = new TechnicianPlanSummary(
                    UUID.randomUUID(), farmerId1, "João", communityId1, "Comunidade A",
                    cropId, "Milho", "Híbrido", HarvestType.SINGLE, "KG", PlanStatus.ACTIVE,
                    new BigDecimal("3.00"), new BigDecimal("6000.00"), new BigDecimal("4000.00"), new BigDecimal("500.00"),
                    2L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 4, 1), LocalDate.of(2026, 7, 1),
                    LocalDate.of(2026, 5, 10)
            );

            when(productionPlanRepository.findPlanSummariesByTechnician(technicianId, null, cropId, harvestId, false))
                    .thenReturn(List.of(planJoao));

            TechnicianDashboardSummaryDTO result = useCase.execute(harvestId, null, cropId, technicianUser);

            // Com o filtro de cropId, farmers[] deve conter apenas João (Secao 3.3)
            assertThat(result.farmers()).hasSize(1);
            assertThat(result.farmers().get(0).farmerId()).isEqualTo(farmerId1);
            assertThat(result.farmers().get(0).farmerName()).isEqualTo("João");
        }
    }
}
