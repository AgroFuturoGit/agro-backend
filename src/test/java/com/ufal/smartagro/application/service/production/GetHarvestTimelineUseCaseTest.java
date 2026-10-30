package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.adapters.in.web.dto.analytics.HarvestTimelineDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.TimelinePointDTO;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.Crop;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.Manager;
import com.ufal.smartagro.domain.model.Organization;
import com.ufal.smartagro.domain.model.ProductionExecution;
import com.ufal.smartagro.domain.model.ProductionPlan;
import com.ufal.smartagro.domain.model.Technician;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.ExecutionStatus;
import com.ufal.smartagro.domain.model.enums.HarvestType;
import com.ufal.smartagro.domain.model.enums.PlanStatus;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.model.enums.TimelineGranularity;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetHarvestTimelineUseCase — evolução temporal analítica da colheita")
class GetHarvestTimelineUseCaseTest {

    @Mock
    private ProductionPlanRepository productionPlanRepository;
    @Mock
    private ProductionExecutionRepository productionExecutionRepository;
    @Mock
    private ManagerRepository managerRepository;
    @Mock
    private TechnicianRepository technicianRepository;
    @Mock
    private TechnicalAssistanceRepository technicalAssistanceRepository;

    private GetHarvestTimelineUseCase useCase;

    private User adminUser;
    private Farmer defaultFarmer;
    private Crop continuousCrop;
    private Crop singleCrop;

    @BeforeEach
    void setUp() {
        ProductionAccessValidator validator = new ProductionAccessValidator(
                managerRepository, technicianRepository, technicalAssistanceRepository);
        useCase = new GetHarvestTimelineUseCase(productionPlanRepository, productionExecutionRepository, validator);

        adminUser = UserTestFactory.user().role(Role.ADMIN).build();
        defaultFarmer = UserTestFactory.farmerProfile().build();

        continuousCrop = new Crop(UUID.randomUUID(), "Tomate", "Italiano", false, 90,
                new BigDecimal("10.00"), HarvestType.CONTINUOUS, null, null);
        singleCrop = new Crop(UUID.randomUUID(), "Milho", "Híbrido", false, 120,
                new BigDecimal("10.00"), HarvestType.SINGLE, null, null);
    }

    private ProductionPlan createPlan(Crop crop, LocalDate start, LocalDate end, BigDecimal area, BigDecimal productivity) {
        return createPlanWithPlantingDate(crop, start, end, area, productivity, null);
    }

    private ProductionPlan createPlanWithPlantingDate(Crop crop, LocalDate start, LocalDate end, BigDecimal area, BigDecimal productivity, LocalDate plantingDate) {
        return new ProductionPlan(
                UUID.randomUUID(),
                defaultFarmer,
                UserTestFactory.harvest(),
                crop,
                area,
                PlanStatus.ACTIVE,
                start,
                end,
                productivity,
                "Lote 1",
                null,
                null,
                plantingDate,
                null,
                null,
                null,
                null
        );
    }

    private ProductionExecution createExecution(ProductionPlan plan, LocalDate date, BigDecimal qtyKg, ExecutionStatus status) {
        return new ProductionExecution(
                UUID.randomUUID(),
                plan,
                qtyKg,
                qtyKg,
                date,
                status,
                null,
                null,
                "Apontamento teste",
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    @Test
    @DisplayName("1. Distribuição semanal contínua com divisão exata da meta")
    void distribuicaoSemanalContinuaComDivisaoExataDaMeta() {
        // 21 dias = exatamente 3 semanas (de 04/05/2026 segunda até 24/05/2026 domingo)
        LocalDate start = LocalDate.of(2026, 5, 4);
        LocalDate end = LocalDate.of(2026, 5, 24);
        // Meta: 30 hectares * 10.00 = 300.00 kg
        ProductionPlan plan = createPlan(continuousCrop, start, end, new BigDecimal("30.00"), new BigDecimal("10.00"));

        ProductionExecution e1 = createExecution(plan, LocalDate.of(2026, 5, 5), new BigDecimal("50.00"), ExecutionStatus.VALIDATED);
        ProductionExecution e2 = createExecution(plan, LocalDate.of(2026, 5, 6), new BigDecimal("30.00"), ExecutionStatus.PENDING);
        ProductionExecution e3 = createExecution(plan, LocalDate.of(2026, 5, 20), new BigDecimal("100.00"), ExecutionStatus.VALIDATED);

        when(productionPlanRepository.findById(plan.getId())).thenReturn(Optional.of(plan));
        when(productionExecutionRepository.findAllByProductionPlanId(plan.getId())).thenReturn(List.of(e1, e2, e3));

        HarvestTimelineDTO result = useCase.execute(plan.getId(), TimelineGranularity.WEEK, adminUser);

        assertThat(result.planId()).isEqualTo(plan.getId());
        assertThat(result.harvestType()).isEqualTo(HarvestType.CONTINUOUS);
        assertThat(result.granularity()).isEqualTo(TimelineGranularity.WEEK);
        assertThat(result.points()).hasSize(3);

        TimelinePointDTO p1 = result.points().get(0);
        assertThat(p1.date()).isEqualTo(LocalDate.of(2026, 5, 4));
        assertThat(p1.expectedValue()).isEqualByComparingTo("100.00");
        assertThat(p1.actualValue()).isEqualByComparingTo("50.00");
        assertThat(p1.pendingValue()).isEqualByComparingTo("30.00");

        TimelinePointDTO p2 = result.points().get(1);
        assertThat(p2.date()).isEqualTo(LocalDate.of(2026, 5, 11));
        assertThat(p2.expectedValue()).isEqualByComparingTo("100.00");
        assertThat(p2.actualValue()).isEqualByComparingTo("0.00");
        assertThat(p2.pendingValue()).isEqualByComparingTo("0.00");

        TimelinePointDTO p3 = result.points().get(2);
        assertThat(p3.date()).isEqualTo(LocalDate.of(2026, 5, 18));
        assertThat(p3.expectedValue()).isEqualByComparingTo("100.00");
        assertThat(p3.actualValue()).isEqualByComparingTo("100.00");
        assertThat(p3.pendingValue()).isEqualByComparingTo("0.00");

        BigDecimal sumExpected = result.points().stream()
                .map(TimelinePointDTO::expectedValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(sumExpected).isEqualByComparingTo("300.00");
    }

    @Test
    @DisplayName("2. Conservação exata sem perda de centavos em dízimas (meta 100 kg em 3 períodos)")
    void conservacaoExataSemPerdaDeCentavosEmDizimas() {
        LocalDate start = LocalDate.of(2026, 5, 4);
        LocalDate end = LocalDate.of(2026, 5, 24);
        // Meta total: 10 hectares * 10 = 100.00 kg
        ProductionPlan plan = createPlan(continuousCrop, start, end, new BigDecimal("10.00"), new BigDecimal("10.00"));

        when(productionPlanRepository.findById(plan.getId())).thenReturn(Optional.of(plan));
        when(productionExecutionRepository.findAllByProductionPlanId(plan.getId())).thenReturn(List.of());

        HarvestTimelineDTO result = useCase.execute(plan.getId(), TimelineGranularity.WEEK, adminUser);

        assertThat(result.points()).hasSize(3);
        assertThat(result.points().get(0).expectedValue()).isEqualByComparingTo("33.33");
        assertThat(result.points().get(1).expectedValue()).isEqualByComparingTo("33.34");
        assertThat(result.points().get(2).expectedValue()).isEqualByComparingTo("33.33");

        BigDecimal sum = result.points().stream()
                .map(TimelinePointDTO::expectedValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(sum).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("3. Agrupamento mensal em ano bissexto (fevereiro de 29 dias)")
    void agrupamentoMensalEmAnoBissexto() {
        LocalDate start = LocalDate.of(2024, 2, 1);
        LocalDate end = LocalDate.of(2024, 3, 31);
        // Fevereiro (29) + Março (31) = 60 dias totais. Meta: 60.00 kg
        ProductionPlan plan = createPlan(continuousCrop, start, end, new BigDecimal("6.00"), new BigDecimal("10.00"));

        when(productionPlanRepository.findById(plan.getId())).thenReturn(Optional.of(plan));
        when(productionExecutionRepository.findAllByProductionPlanId(plan.getId())).thenReturn(List.of());

        HarvestTimelineDTO result = useCase.execute(plan.getId(), TimelineGranularity.MONTH, adminUser);

        assertThat(result.points()).hasSize(2);
        assertThat(result.points().get(0).date()).isEqualTo(LocalDate.of(2024, 2, 1));
        assertThat(result.points().get(0).expectedValue()).isEqualByComparingTo("29.00");

        assertThat(result.points().get(1).date()).isEqualTo(LocalDate.of(2024, 3, 1));
        assertThat(result.points().get(1).expectedValue()).isEqualByComparingTo("31.00");

        BigDecimal sum = result.points().stream()
                .map(TimelinePointDTO::expectedValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(sum).isEqualByComparingTo("60.00");
    }

    @Test
    @DisplayName("4. Zero-filling de períodos intermediários sem colheita")
    void zeroFillingDePeriodosIntermediariosSemColheita() {
        LocalDate start = LocalDate.of(2026, 5, 4);
        LocalDate end = LocalDate.of(2026, 5, 24);
        ProductionPlan plan = createPlan(continuousCrop, start, end, new BigDecimal("10.00"), new BigDecimal("10.00"));

        // Apenas semana 1 e semana 3 têm apontamentos
        ProductionExecution e1 = createExecution(plan, LocalDate.of(2026, 5, 5), new BigDecimal("15.00"), ExecutionStatus.VALIDATED);
        ProductionExecution e3 = createExecution(plan, LocalDate.of(2026, 5, 20), new BigDecimal("25.00"), ExecutionStatus.VALIDATED);

        when(productionPlanRepository.findById(plan.getId())).thenReturn(Optional.of(plan));
        when(productionExecutionRepository.findAllByProductionPlanId(plan.getId())).thenReturn(List.of(e1, e3));

        HarvestTimelineDTO result = useCase.execute(plan.getId(), TimelineGranularity.WEEK, adminUser);

        assertThat(result.points()).hasSize(3);
        TimelinePointDTO p2 = result.points().get(1);
        assertThat(p2.actualValue()).isEqualByComparingTo("0.00");
        assertThat(p2.pendingValue()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("5. Colheita antecipada (antes do início previsto da janela)")
    void colheitaAntecipadaAntesDoInicioPrevisto() {
        LocalDate start = LocalDate.of(2026, 5, 11);
        LocalDate end = LocalDate.of(2026, 5, 24); // 2 semanas previstas
        ProductionPlan plan = createPlan(continuousCrop, start, end, new BigDecimal("20.00"), new BigDecimal("10.00"));

        // Colheita antecipada no dia 04/05 (1 semana antes)
        ProductionExecution early = createExecution(plan, LocalDate.of(2026, 5, 4), new BigDecimal("40.00"), ExecutionStatus.VALIDATED);

        when(productionPlanRepository.findById(plan.getId())).thenReturn(Optional.of(plan));
        when(productionExecutionRepository.findAllByProductionPlanId(plan.getId())).thenReturn(List.of(early));

        HarvestTimelineDTO result = useCase.execute(plan.getId(), TimelineGranularity.WEEK, adminUser);

        assertThat(result.points()).hasSize(3);
        TimelinePointDTO earlyPoint = result.points().get(0);
        assertThat(earlyPoint.date()).isEqualTo(LocalDate.of(2026, 5, 4));
        assertThat(earlyPoint.expectedValue()).isEqualByComparingTo("0.00");
        assertThat(earlyPoint.actualValue()).isEqualByComparingTo("40.00");
    }

    @Test
    @DisplayName("6. Colheita tardia (após o fim previsto da janela)")
    void colheitaTardiaAposOFimPrevisto() {
        LocalDate start = LocalDate.of(2026, 5, 4);
        LocalDate end = LocalDate.of(2026, 5, 17); // 2 semanas previstas
        ProductionPlan plan = createPlan(continuousCrop, start, end, new BigDecimal("20.00"), new BigDecimal("10.00"));

        // Colheita tardia no dia 20/05 (na 3ª semana)
        ProductionExecution late = createExecution(plan, LocalDate.of(2026, 5, 20), new BigDecimal("35.00"), ExecutionStatus.VALIDATED);

        when(productionPlanRepository.findById(plan.getId())).thenReturn(Optional.of(plan));
        when(productionExecutionRepository.findAllByProductionPlanId(plan.getId())).thenReturn(List.of(late));

        HarvestTimelineDTO result = useCase.execute(plan.getId(), TimelineGranularity.WEEK, adminUser);

        assertThat(result.points()).hasSize(3);
        TimelinePointDTO latePoint = result.points().get(2);
        assertThat(latePoint.date()).isEqualTo(LocalDate.of(2026, 5, 18));
        assertThat(latePoint.expectedValue()).isEqualByComparingTo("0.00");
        assertThat(latePoint.actualValue()).isEqualByComparingTo("35.00");
    }

    @Test
    @DisplayName("7. Descarte total de apontamentos com status REJECTED")
    void descarteTotalDeApontamentosComStatusRejected() {
        LocalDate start = LocalDate.of(2026, 5, 4);
        LocalDate end = LocalDate.of(2026, 5, 10);
        ProductionPlan plan = createPlan(continuousCrop, start, end, new BigDecimal("10.00"), new BigDecimal("10.00"));

        ProductionExecution rejected = createExecution(plan, LocalDate.of(2026, 5, 5), new BigDecimal("1000.00"), ExecutionStatus.REJECTED);

        when(productionPlanRepository.findById(plan.getId())).thenReturn(Optional.of(plan));
        when(productionExecutionRepository.findAllByProductionPlanId(plan.getId())).thenReturn(List.of(rejected));

        HarvestTimelineDTO result = useCase.execute(plan.getId(), TimelineGranularity.WEEK, adminUser);

        assertThat(result.points()).hasSize(1);
        assertThat(result.points().get(0).actualValue()).isEqualByComparingTo("0.00");
        assertThat(result.points().get(0).pendingValue()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("8. Degradação automática para ciclo único (SINGLE) com 1 ponto consolidado")
    void degradacaoAutomaticaParaCicloUnicoSingle() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 5, 30);
        ProductionPlan plan = createPlan(singleCrop, start, end, new BigDecimal("10.00"), new BigDecimal("10.00"));

        ProductionExecution e1 = createExecution(plan, LocalDate.of(2026, 5, 10), new BigDecimal("60.00"), ExecutionStatus.VALIDATED);
        ProductionExecution e2 = createExecution(plan, LocalDate.of(2026, 5, 12), new BigDecimal("25.00"), ExecutionStatus.PENDING);
        ProductionExecution e3 = createExecution(plan, LocalDate.of(2026, 5, 15), new BigDecimal("99.00"), ExecutionStatus.REJECTED);

        when(productionPlanRepository.findById(plan.getId())).thenReturn(Optional.of(plan));
        when(productionExecutionRepository.findAllByProductionPlanId(plan.getId())).thenReturn(List.of(e1, e2, e3));

        HarvestTimelineDTO result = useCase.execute(plan.getId(), TimelineGranularity.WEEK, adminUser);

        assertThat(result.harvestType()).isEqualTo(HarvestType.SINGLE);
        assertThat(result.points()).hasSize(1);
        TimelinePointDTO point = result.points().get(0);
        assertThat(point.date()).isEqualTo(end);
        assertThat(point.expectedValue()).isEqualByComparingTo("100.00");
        assertThat(point.actualValue()).isEqualByComparingTo("60.00");
        assertThat(point.pendingValue()).isEqualByComparingTo("25.00");
    }

    @Test
    @DisplayName("9. Plano sem datas de previsão (meta 0.00, apenas apontamentos reais)")
    void planoSemDatasDePrevisao() {
        ProductionPlan plan = createPlan(continuousCrop, null, null, new BigDecimal("10.00"), new BigDecimal("10.00"));

        ProductionExecution e1 = createExecution(plan, LocalDate.of(2026, 5, 4), new BigDecimal("45.00"), ExecutionStatus.VALIDATED);

        when(productionPlanRepository.findById(plan.getId())).thenReturn(Optional.of(plan));
        when(productionExecutionRepository.findAllByProductionPlanId(plan.getId())).thenReturn(List.of(e1));

        HarvestTimelineDTO result = useCase.execute(plan.getId(), TimelineGranularity.WEEK, adminUser);

        assertThat(result.points()).hasSize(1);
        assertThat(result.points().get(0).expectedValue()).isEqualByComparingTo("0.00");
        assertThat(result.points().get(0).actualValue()).isEqualByComparingTo("45.00");
    }

    @Test
    @DisplayName("10. Plano com datas invertidas ou janela inválida (guarda defensiva)")
    void planoComDatasInvertidasOuJanelaInvalida() {
        LocalDate start = LocalDate.of(2026, 5, 20);
        LocalDate end = LocalDate.of(2026, 5, 10); // Start > End
        ProductionPlan plan = createPlan(continuousCrop, start, end, new BigDecimal("10.00"), new BigDecimal("10.00"));

        ProductionExecution e1 = createExecution(plan, LocalDate.of(2026, 5, 15), new BigDecimal("30.00"), ExecutionStatus.VALIDATED);

        when(productionPlanRepository.findById(plan.getId())).thenReturn(Optional.of(plan));
        when(productionExecutionRepository.findAllByProductionPlanId(plan.getId())).thenReturn(List.of(e1));

        HarvestTimelineDTO result = useCase.execute(plan.getId(), TimelineGranularity.WEEK, adminUser);

        assertThat(result.points()).hasSize(1);
        assertThat(result.points().get(0).expectedValue()).isEqualByComparingTo("0.00");
        assertThat(result.points().get(0).actualValue()).isEqualByComparingTo("30.00");
    }

    @Test
    @DisplayName("11. Plano vazio (sem janela e sem apontamentos -> lista vazia)")
    void planoVazioSemJanelaESemApontamentos() {
        ProductionPlan plan = createPlan(continuousCrop, null, null, new BigDecimal("10.00"), new BigDecimal("10.00"));

        when(productionPlanRepository.findById(plan.getId())).thenReturn(Optional.of(plan));
        when(productionExecutionRepository.findAllByProductionPlanId(plan.getId())).thenReturn(List.of());

        HarvestTimelineDTO result = useCase.execute(plan.getId(), TimelineGranularity.WEEK, adminUser);

        assertThat(result.points()).isEmpty();
    }

    @Test
    @DisplayName("12. Matriz RBAC completa (FARMER alheio, TECHNICIAN sem assistência e MANAGER de outra organização barrados)")
    void matrizRbacCompleta() {
        ProductionPlan plan = createPlan(continuousCrop, LocalDate.of(2026, 5, 4), LocalDate.of(2026, 5, 24),
                new BigDecimal("10.00"), new BigDecimal("10.00"));

        when(productionPlanRepository.findById(plan.getId())).thenReturn(Optional.of(plan));

        // 12a. FARMER alheio
        User otherFarmerUser = UserTestFactory.user().role(Role.FARMER).build();
        assertThatThrownBy(() -> useCase.execute(plan.getId(), TimelineGranularity.WEEK, otherFarmerUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("O agricultor só tem acesso aos seus próprios dados de produção.");

        // 12b. TECHNICIAN sem assistência técnica ativa
        User techUser = UserTestFactory.user().role(Role.TECHNICIAN).build();
        Technician technician = UserTestFactory.technicianProfile().user(techUser).build();
        when(technicianRepository.findByUserId(techUser.getId())).thenReturn(Optional.of(technician));
        when(technicalAssistanceRepository.findActiveByTechnicianAndCommunity(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(plan.getId(), TimelineGranularity.WEEK, techUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("O técnico só pode visualizar agricultores das comunidades sob sua assistência.");

        // 12c. MANAGER de outra organização
        User managerUser = UserTestFactory.user().role(Role.MANAGER).build();
        Organization otherOrg = UserTestFactory.organization().build();
        Manager otherManager = UserTestFactory.managerProfile().user(managerUser).organization(otherOrg).build();
        when(managerRepository.findByUserId(managerUser.getId())).thenReturn(Optional.of(otherManager));

        assertThatThrownBy(() -> useCase.execute(plan.getId(), TimelineGranularity.WEEK, managerUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("O gestor só tem acesso aos agricultores da sua própria organização.");
    }

    @Test
    @DisplayName("13. Plano inexistente lança EntityNotFoundException (HTTP 404)")
    void planoInexistenteLancaEntityNotFoundException() {
        UUID nonExistentId = UUID.randomUUID();
        when(productionPlanRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(nonExistentId, TimelineGranularity.WEEK, adminUser))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Plano de produção não encontrado.");
    }

    @Test
    @DisplayName("14. Alinhamento semanal para Segunda-Feira quando janela inicia no meio da semana")
    void alinhamentoSemanalParaSegundaFeiraQuandoJanelaIniciaNoMeioDaSemana() {
        // Quarta-feira 06/05/2026 até Sexta-feira 15/05/2026
        LocalDate start = LocalDate.of(2026, 5, 6);
        LocalDate end = LocalDate.of(2026, 5, 15);
        ProductionPlan plan = createPlan(continuousCrop, start, end, new BigDecimal("10.00"), new BigDecimal("10.00"));

        when(productionPlanRepository.findById(plan.getId())).thenReturn(Optional.of(plan));
        when(productionExecutionRepository.findAllByProductionPlanId(plan.getId())).thenReturn(List.of());

        HarvestTimelineDTO result = useCase.execute(plan.getId(), TimelineGranularity.WEEK, adminUser);

        // Deve ter 2 semanas: 04/05/2026 (Segunda) e 11/05/2026 (Segunda)
        assertThat(result.points()).hasSize(2);
        assertThat(result.points().get(0).date()).isEqualTo(LocalDate.of(2026, 5, 4));
        assertThat(result.points().get(1).date()).isEqualTo(LocalDate.of(2026, 5, 11));

        // Meta total 100 kg distribuída proporcionalmente aos dias de sobreposição em cada semana
        // Semana 1: 06/05 a 10/05 (5 dias). Semana 2: 11/05 a 15/05 (5 dias). Total = 10 dias.
        // 50.00 kg em cada semana.
        assertThat(result.points().get(0).expectedValue()).isEqualByComparingTo("50.00");
        assertThat(result.points().get(1).expectedValue()).isEqualByComparingTo("50.00");
    }

    @Test
    @DisplayName("15. Fallback para SINGLE quando a cultura não possui harvestType definido (legada/null)")
    void fallbackParaHarvestTypeSingleQuandoCulturaNaoPossuiHarvestType() {
        Crop legacyCrop = new Crop(UUID.randomUUID(), "Cultura Antiga", "Variedade", false, 90,
                new BigDecimal("10.00"), null, null, null);
        ProductionPlan plan = createPlan(legacyCrop, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 30),
                new BigDecimal("10.00"), new BigDecimal("10.00"));

        when(productionPlanRepository.findById(plan.getId())).thenReturn(Optional.of(plan));
        when(productionExecutionRepository.findAllByProductionPlanId(plan.getId())).thenReturn(List.of());

        HarvestTimelineDTO result = useCase.execute(plan.getId(), TimelineGranularity.WEEK, adminUser);

        assertThat(result.harvestType()).isEqualTo(HarvestType.SINGLE);
        assertThat(result.points()).hasSize(1);
    }

    @Test
    @DisplayName("16. Plano SINGLE com meta mas sem datas de previsão faz fallback para data de plantio")
    void planoSingleComMetaSemDatasDePrevisaoFazFallbackParaDataDePlantio() {
        LocalDate plantingDate = LocalDate.of(2026, 3, 15);
        ProductionPlan plan = createPlanWithPlantingDate(singleCrop, null, null,
                new BigDecimal("10.00"), new BigDecimal("10.00"), plantingDate);

        when(productionPlanRepository.findById(plan.getId())).thenReturn(Optional.of(plan));
        when(productionExecutionRepository.findAllByProductionPlanId(plan.getId())).thenReturn(List.of());

        HarvestTimelineDTO result = useCase.execute(plan.getId(), TimelineGranularity.WEEK, adminUser);

        assertThat(result.harvestType()).isEqualTo(HarvestType.SINGLE);
        assertThat(result.points()).hasSize(1);
        TimelinePointDTO point = result.points().get(0);
        assertThat(point.date()).isEqualTo(plantingDate);
        assertThat(point.expectedValue()).isEqualByComparingTo("100.00");
        assertThat(point.actualValue()).isEqualByComparingTo("0.00");
        assertThat(point.pendingValue()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("17. Proteção contra datas anômalas (outlier) limita span temporal da timeline contínua")
    void protecaoContraDatasAnomalasLimitaSpanTemporal() {
        LocalDate start = LocalDate.of(2026, 5, 4);
        LocalDate end = LocalDate.of(2026, 5, 24);
        ProductionPlan plan = createPlan(continuousCrop, start, end, new BigDecimal("10.00"), new BigDecimal("10.00"));

        // Apontamento anômalo com ano 2226 (~200 anos no futuro)
        ProductionExecution outlier = createExecution(plan, LocalDate.of(2226, 5, 4), new BigDecimal("10.00"), ExecutionStatus.VALIDATED);

        when(productionPlanRepository.findById(plan.getId())).thenReturn(Optional.of(plan));
        when(productionExecutionRepository.findAllByProductionPlanId(plan.getId())).thenReturn(List.of(outlier));

        HarvestTimelineDTO result = useCase.execute(plan.getId(), TimelineGranularity.WEEK, adminUser);

        // Deve limitar a no máximo ~262 semanas (~5 anos) em vez de iterar por 200 anos (10.400 semanas)
        assertThat(result.points().size()).isLessThanOrEqualTo(265);
    }
}
