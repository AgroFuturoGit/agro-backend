package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.adapters.in.web.dto.analytics.HarvestSummaryInfoDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.ManagementCommunityPerformanceDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.ManagementCropRankingItemDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.ManagementDashboardSummaryDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.ManagementOrganizationSummaryDTO;
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
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.CommunityRepository;
import com.ufal.smartagro.domain.port.out.HarvestRepository;
import com.ufal.smartagro.domain.port.out.ManagerRepository;
import com.ufal.smartagro.domain.port.out.OrganizationRepository;
import com.ufal.smartagro.domain.port.out.ProductionPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Caso de uso para consolidar e retornar o dashboard macro-gerencial (Gestor / Administrador).
 * Utiliza a Materialized View mv_org_harvest_summary e a vw_active_farmers.
 */
@Service
@RequiredArgsConstructor
public class GetManagementDashboardSummaryUseCase {

    private final ManagerRepository managerRepository;
    private final OrganizationRepository organizationRepository;
    private final CommunityRepository communityRepository;
    private final HarvestRepository harvestRepository;
    private final ProductionPlanRepository productionPlanRepository;

    @Transactional(readOnly = true)
    public ManagementDashboardSummaryDTO executeForCurrentManager(
            UUID harvestId,
            UUID communityId,
            User loggedUser
    ) {
        return executeForCurrentManager(harvestId, communityId, loggedUser, LocalDate.now());
    }

    @Transactional(readOnly = true)
    public ManagementDashboardSummaryDTO executeForCurrentManager(
            UUID harvestId,
            UUID communityId,
            User loggedUser,
            LocalDate referenceDate
    ) {
        Manager manager = getActiveManagerOrThrow(loggedUser);
        if (manager.getOrganization() == null || manager.getOrganization().getId() == null) {
            throw new AccessDeniedException("Gestor sem organização vinculada.");
        }
        UUID organizationId = manager.getOrganization().getId();

        return executeForOrganization(organizationId, harvestId, communityId, loggedUser, referenceDate);
    }

    @Transactional(readOnly = true)
    public ManagementDashboardSummaryDTO executeForOrganization(
            UUID organizationId,
            UUID harvestId,
            UUID communityId,
            User loggedUser
    ) {
        return executeForOrganization(organizationId, harvestId, communityId, loggedUser, LocalDate.now());
    }

    @Transactional(readOnly = true)
    public ManagementDashboardSummaryDTO executeForOrganization(
            UUID organizationId,
            UUID harvestId,
            UUID communityId,
            User loggedUser,
            LocalDate referenceDate
    ) {
        if (organizationId == null) {
            throw new IllegalArgumentException("organizationId é obrigatório para o escopo de organização.");
        }

        // Validação RBAC
        if (loggedUser.getRole() == Role.MANAGER) {
            Manager manager = getActiveManagerOrThrow(loggedUser);
            if (manager.getOrganization() == null
                    || !organizationId.equals(manager.getOrganization().getId())) {
                throw new AccessDeniedException("Acesso negado: gestor não tem permissão para visualizar dados de outra organização.");
            }
        } else if (loggedUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Apenas Gestores e Administradores podem acessar o dashboard gerencial.");
        }

        Organization organization = organizationRepository.findById(organizationId)
                .filter(org -> org.getDeletedAt() == null)
                .orElseThrow(() -> new EntityNotFoundException("Organização não encontrada."));

        if (communityId != null) {
            Community community = communityRepository.findById(communityId)
                    .filter(c -> c.getDeletedAt() == null)
                    .orElseThrow(() -> new EntityNotFoundException("Comunidade não encontrada."));

            if (community.getOrganization() == null
                    || !organizationId.equals(community.getOrganization().getId())) {
                if (loggedUser.getRole() == Role.MANAGER) {
                    throw new AccessDeniedException("Comunidade informada não pertence à organização do gestor.");
                }
                throw new AccessDeniedException("Comunidade informada não pertence à organização selecionada.");
            }
        }

        return buildDashboard(
                ManagementDashboardScope.ORGANIZATION,
                organization.getId(),
                organization.getName(),
                harvestId,
                communityId,
                referenceDate
        );
    }

    @Transactional(readOnly = true)
    public ManagementDashboardSummaryDTO executeGlobal(
            UUID harvestId,
            UUID organizationId,
            UUID communityId,
            User loggedUser
    ) {
        return executeGlobal(harvestId, organizationId, communityId, loggedUser, LocalDate.now());
    }

    @Transactional(readOnly = true)
    public ManagementDashboardSummaryDTO executeGlobal(
            UUID harvestId,
            UUID organizationId,
            UUID communityId,
            User loggedUser,
            LocalDate referenceDate
    ) {
        if (loggedUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Apenas Administradores do sistema podem acessar o dashboard global.");
        }

        String filterOrgName = null;
        if (organizationId != null) {
            Organization organization = organizationRepository.findById(organizationId)
                    .filter(org -> org.getDeletedAt() == null)
                    .orElseThrow(() -> new EntityNotFoundException("Organização não encontrada."));
            filterOrgName = organization.getName();
        }

        if (communityId != null) {
            Community community = communityRepository.findById(communityId)
                    .filter(c -> c.getDeletedAt() == null)
                    .orElseThrow(() -> new EntityNotFoundException("Comunidade não encontrada."));

            if (organizationId != null && (community.getOrganization() == null
                    || !organizationId.equals(community.getOrganization().getId()))) {
                throw new AccessDeniedException("Comunidade informada não pertence à organização selecionada.");
            }
        }

        return buildDashboard(
                ManagementDashboardScope.GLOBAL,
                organizationId,
                filterOrgName,
                harvestId,
                communityId,
                referenceDate
        );
    }

    private ManagementDashboardSummaryDTO buildDashboard(
            ManagementDashboardScope scope,
            UUID targetOrganizationId,
            String targetOrganizationName,
            UUID requestedHarvestId,
            UUID communityId,
            LocalDate referenceDate
    ) {
        LocalDate today = referenceDate != null ? referenceDate : LocalDate.now();

        // 1. Resolução da Safra
        HarvestSummaryInfoDTO currentHarvestDto = null;
        UUID resolvedHarvestId = requestedHarvestId;

        if (requestedHarvestId != null) {
            Harvest harvest = harvestRepository.findById(requestedHarvestId)
                    .orElseThrow(() -> new HarvestNotFoundException("Safra não encontrada."));
            currentHarvestDto = toHarvestSummaryInfoDTO(harvest);
        } else {
            Optional<Harvest> activeHarvest = harvestRepository.findActiveHarvest(today);
            if (activeHarvest.isPresent()) {
                Harvest harvest = activeHarvest.get();
                resolvedHarvestId = harvest.getId();
                currentHarvestDto = toHarvestSummaryInfoDTO(harvest);
            }
        }

        // 2. Query 1: Censo de comunidades e agricultores
        List<ManagementCommunityCensusRow> censusRows = productionPlanRepository
                .findCommunityCensus(targetOrganizationId, communityId);

        // 3. Query 2: Dados da view materializada mv_org_harvest_summary
        List<ManagementOrgHarvestSummaryRow> harvestRows = productionPlanRepository
                .findManagementHarvestSummary(targetOrganizationId, communityId, resolvedHarvestId);

        // Fallback gracioso de safra: se safra ativa não tem dados no escopo, busca dados gerais da MV
        if (requestedHarvestId == null && resolvedHarvestId != null && harvestRows.isEmpty()) {
            List<ManagementOrgHarvestSummaryRow> fallbackRows = productionPlanRepository
                    .findManagementHarvestSummary(targetOrganizationId, communityId, null);
            if (!fallbackRows.isEmpty()) {
                harvestRows = fallbackRows;
            }
        }

        // Empty state: sem comunidades e sem dados de produção
        if (censusRows.isEmpty() && harvestRows.isEmpty()) {
            return ManagementDashboardSummaryDTO.empty(
                    scope,
                    targetOrganizationId,
                    targetOrganizationName,
                    currentHarvestDto,
                    communityId
            );
        }

        // Se o nome da organização ainda for nulo mas censusRows contiver valor único
        if (targetOrganizationName == null && !censusRows.isEmpty() && targetOrganizationId != null) {
            targetOrganizationName = censusRows.get(0).organizationName();
        }

        return assembleSummary(
                scope,
                targetOrganizationId,
                targetOrganizationName,
                currentHarvestDto,
                communityId,
                censusRows,
                harvestRows
        );
    }

    private ManagementDashboardSummaryDTO assembleSummary(
            ManagementDashboardScope scope,
            UUID organizationId,
            String organizationName,
            HarvestSummaryInfoDTO currentHarvest,
            UUID appliedCommunityId,
            List<ManagementCommunityCensusRow> censusRows,
            List<ManagementOrgHarvestSummaryRow> harvestRows
    ) {
        // Métricas Cadastrais a partir de censusRows
        Set<UUID> orgIds = censusRows.stream().map(ManagementCommunityCensusRow::organizationId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<UUID> commIds = censusRows.stream().map(ManagementCommunityCensusRow::communityId).filter(Objects::nonNull).collect(Collectors.toSet());

        // Se censusRows estiver vazio mas tiver harvestRows (fallback extremo)
        if (orgIds.isEmpty()) {
            orgIds = harvestRows.stream().map(ManagementOrgHarvestSummaryRow::organizationId).filter(Objects::nonNull).collect(Collectors.toSet());
            commIds = harvestRows.stream().map(ManagementOrgHarvestSummaryRow::communityId).filter(Objects::nonNull).collect(Collectors.toSet());
        }

        int totalOrganizationsCount = orgIds.size();
        int totalCommunitiesCount = commIds.size();
        int totalFarmersCount = (int) censusRows.stream().mapToLong(ManagementCommunityCensusRow::activeFarmersCount).sum();
        int compliantFarmersCount = (int) censusRows.stream().mapToLong(ManagementCommunityCensusRow::compliantFarmersCount).sum();

        BigDecimal complianceRatePct = calculatePercentage(compliantFarmersCount, totalFarmersCount);

        // Métricas Produtivas a partir de harvestRows
        int totalPlansCount = (int) harvestRows.stream().mapToLong(ManagementOrgHarvestSummaryRow::planCount).sum();
        BigDecimal totalPlantedAreaHa = harvestRows.stream()
                .map(ManagementOrgHarvestSummaryRow::plantedAreaHa)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalTargetKg = harvestRows.stream()
                .map(ManagementOrgHarvestSummaryRow::targetKg)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalRealizedKg = harvestRows.stream()
                .map(ManagementOrgHarvestSummaryRow::realizedKg)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal overallConversionRatePct = calculatePercentage(totalRealizedKg, totalTargetKg);

        // 1. Ranking de Culturas
        List<ManagementCropRankingItemDTO> cropRanking = buildCropRanking(harvestRows, totalRealizedKg, totalTargetKg);

        // 2. Comparativo de Comunidades
        List<ManagementCommunityPerformanceDTO> communities = buildCommunityPerformances(censusRows, harvestRows);

        // 3. Comparativo de Organizações
        List<ManagementOrganizationSummaryDTO> organizations = buildOrganizationSummaries(censusRows, harvestRows);

        boolean isEmpty = (totalPlansCount == 0);

        return new ManagementDashboardSummaryDTO(
                scope,
                organizationId,
                organizationName,
                currentHarvest,
                appliedCommunityId,
                isEmpty,
                totalOrganizationsCount,
                totalCommunitiesCount,
                totalFarmersCount,
                compliantFarmersCount,
                complianceRatePct,
                totalPlansCount,
                totalPlantedAreaHa,
                totalTargetKg,
                totalRealizedKg,
                overallConversionRatePct,
                cropRanking,
                communities,
                organizations
        );
    }

    private List<ManagementCropRankingItemDTO> buildCropRanking(
            List<ManagementOrgHarvestSummaryRow> harvestRows,
            BigDecimal globalRealizedKg,
            BigDecimal globalTargetKg
    ) {
        if (harvestRows.isEmpty()) {
            return List.of();
        }

        // Agrupar por cropId
        Map<UUID, List<ManagementOrgHarvestSummaryRow>> rowsByCrop = harvestRows.stream()
                .filter(r -> r.cropId() != null)
                .collect(Collectors.groupingBy(ManagementOrgHarvestSummaryRow::cropId));

        List<CropAggregate> aggregates = new ArrayList<>();
        for (Map.Entry<UUID, List<ManagementOrgHarvestSummaryRow>> entry : rowsByCrop.entrySet()) {
            UUID cropId = entry.getKey();
            List<ManagementOrgHarvestSummaryRow> list = entry.getValue();
            ManagementOrgHarvestSummaryRow first = list.get(0);

            int plansCount = (int) list.stream().mapToLong(ManagementOrgHarvestSummaryRow::planCount).sum();
            BigDecimal plantedAreaHa = list.stream()
                    .map(ManagementOrgHarvestSummaryRow::plantedAreaHa)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal targetKg = list.stream()
                    .map(ManagementOrgHarvestSummaryRow::targetKg)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal realizedKg = list.stream()
                    .map(ManagementOrgHarvestSummaryRow::realizedKg)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal conversionRatePct = calculatePercentage(realizedKg, targetKg);

            // Participação na produção (market share sobre total realizado; 0 quando sem colheita - Seção 3.3)
            BigDecimal productionSharePct;
            if (globalRealizedKg.compareTo(BigDecimal.ZERO) > 0) {
                productionSharePct = calculatePercentage(realizedKg, globalRealizedKg);
            } else {
                productionSharePct = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            }

            aggregates.add(new CropAggregate(
                    cropId,
                    first.cropName(),
                    first.cropVariety(),
                    first.unit(),
                    plansCount,
                    plantedAreaHa,
                    targetKg,
                    realizedKg,
                    conversionRatePct,
                    productionSharePct
            ));
        }

        // Ordenação: 1. realizedKg desc, 2. conversionRatePct desc, 3. targetKg desc, 4. cropName asc
        aggregates.sort((a, b) -> {
            int cmp = b.realizedKg().compareTo(a.realizedKg());
            if (cmp != 0) return cmp;
            cmp = b.conversionRatePct().compareTo(a.conversionRatePct());
            if (cmp != 0) return cmp;
            cmp = b.targetKg().compareTo(a.targetKg());
            if (cmp != 0) return cmp;
            String nameA = a.cropName() != null ? a.cropName() : "";
            String nameB = b.cropName() != null ? b.cropName() : "";
            return nameA.compareToIgnoreCase(nameB);
        });

        List<ManagementCropRankingItemDTO> ranking = new ArrayList<>();
        int rank = 1;
        for (CropAggregate agg : aggregates) {
            ranking.add(new ManagementCropRankingItemDTO(
                    rank++,
                    agg.cropId(),
                    agg.cropName(),
                    agg.cropVariety(),
                    agg.unit(),
                    agg.plansCount(),
                    agg.plantedAreaHa(),
                    agg.targetKg(),
                    agg.realizedKg(),
                    agg.conversionRatePct(),
                    agg.productionSharePct()
            ));
        }

        return ranking;
    }

    private List<ManagementCommunityPerformanceDTO> buildCommunityPerformances(
            List<ManagementCommunityCensusRow> censusRows,
            List<ManagementOrgHarvestSummaryRow> harvestRows
    ) {
        Map<UUID, List<ManagementOrgHarvestSummaryRow>> harvestByComm = harvestRows.stream()
                .filter(r -> r.communityId() != null)
                .collect(Collectors.groupingBy(ManagementOrgHarvestSummaryRow::communityId));

        Map<UUID, ManagementCommunityCensusRow> censusByComm = new LinkedHashMap<>();
        for (ManagementCommunityCensusRow c : censusRows) {
            if (c.communityId() != null) {
                censusByComm.put(c.communityId(), c);
            }
        }

        // Garante inclusão de todas as comunidades conhecidas (do censo e da MV)
        Set<UUID> allCommunityIds = new java.util.LinkedHashSet<>(censusByComm.keySet());
        allCommunityIds.addAll(harvestByComm.keySet());

        List<ManagementCommunityPerformanceDTO> list = new ArrayList<>();

        for (UUID commId : allCommunityIds) {
            ManagementCommunityCensusRow census = censusByComm.get(commId);
            List<ManagementOrgHarvestSummaryRow> hRows = harvestByComm.getOrDefault(commId, List.of());

            UUID orgId = census != null ? census.organizationId() : (!hRows.isEmpty() ? hRows.get(0).organizationId() : null);
            String orgName = census != null ? census.organizationName() : (!hRows.isEmpty() ? hRows.get(0).organizationName() : "");
            String commName = census != null ? census.communityName() : (!hRows.isEmpty() ? hRows.get(0).communityName() : "");

            int activeFarmers = census != null ? (int) census.activeFarmersCount() : 0;
            int compliantFarmers = census != null ? (int) census.compliantFarmersCount() : 0;
            BigDecimal complianceRatePct = calculatePercentage(compliantFarmers, activeFarmers);

            int plansCount = (int) hRows.stream().mapToLong(ManagementOrgHarvestSummaryRow::planCount).sum();
            BigDecimal plantedAreaHa = hRows.stream()
                    .map(ManagementOrgHarvestSummaryRow::plantedAreaHa)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal targetKg = hRows.stream()
                    .map(ManagementOrgHarvestSummaryRow::targetKg)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal realizedKg = hRows.stream()
                    .map(ManagementOrgHarvestSummaryRow::realizedKg)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal conversionRatePct = calculatePercentage(realizedKg, targetKg);

            LocalDate lastExecutionDate = hRows.stream()
                    .map(ManagementOrgHarvestSummaryRow::lastExecutionDate)
                    .filter(Objects::nonNull)
                    .max(Comparator.naturalOrder())
                    .orElse(null);

            list.add(new ManagementCommunityPerformanceDTO(
                    commId,
                    commName,
                    orgId,
                    orgName,
                    activeFarmers,
                    compliantFarmers,
                    complianceRatePct,
                    plansCount,
                    plantedAreaHa,
                    targetKg,
                    realizedKg,
                    conversionRatePct,
                    lastExecutionDate
            ));
        }

        // Ordenar por volume colhido desc, depois nome da comunidade asc
        list.sort((a, b) -> {
            int cmp = b.realizedKg().compareTo(a.realizedKg());
            if (cmp != 0) return cmp;
            String nameA = a.communityName() != null ? a.communityName() : "";
            String nameB = b.communityName() != null ? b.communityName() : "";
            return nameA.compareToIgnoreCase(nameB);
        });

        return list;
    }

    private List<ManagementOrganizationSummaryDTO> buildOrganizationSummaries(
            List<ManagementCommunityCensusRow> censusRows,
            List<ManagementOrgHarvestSummaryRow> harvestRows
    ) {
        Map<UUID, List<ManagementCommunityCensusRow>> censusByOrg = censusRows.stream()
                .filter(r -> r.organizationId() != null)
                .collect(Collectors.groupingBy(ManagementCommunityCensusRow::organizationId));

        Map<UUID, List<ManagementOrgHarvestSummaryRow>> harvestByOrg = harvestRows.stream()
                .filter(r -> r.organizationId() != null)
                .collect(Collectors.groupingBy(ManagementOrgHarvestSummaryRow::organizationId));

        Set<UUID> allOrgIds = new java.util.LinkedHashSet<>(censusByOrg.keySet());
        allOrgIds.addAll(harvestByOrg.keySet());

        List<ManagementOrganizationSummaryDTO> list = new ArrayList<>();

        for (UUID orgId : allOrgIds) {
            List<ManagementCommunityCensusRow> cRows = censusByOrg.getOrDefault(orgId, List.of());
            List<ManagementOrgHarvestSummaryRow> hRows = harvestByOrg.getOrDefault(orgId, List.of());

            String orgName = !cRows.isEmpty() ? cRows.get(0).organizationName() : (!hRows.isEmpty() ? hRows.get(0).organizationName() : "");

            Set<UUID> commSet = cRows.stream().map(ManagementCommunityCensusRow::communityId).filter(Objects::nonNull).collect(Collectors.toSet());
            commSet.addAll(hRows.stream().map(ManagementOrgHarvestSummaryRow::communityId).filter(Objects::nonNull).collect(Collectors.toSet()));
            int communitiesCount = commSet.size();

            int activeFarmers = (int) cRows.stream().mapToLong(ManagementCommunityCensusRow::activeFarmersCount).sum();
            int compliantFarmers = (int) cRows.stream().mapToLong(ManagementCommunityCensusRow::compliantFarmersCount).sum();
            BigDecimal complianceRatePct = calculatePercentage(compliantFarmers, activeFarmers);

            int plansCount = (int) hRows.stream().mapToLong(ManagementOrgHarvestSummaryRow::planCount).sum();
            BigDecimal plantedAreaHa = hRows.stream()
                    .map(ManagementOrgHarvestSummaryRow::plantedAreaHa)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal targetKg = hRows.stream()
                    .map(ManagementOrgHarvestSummaryRow::targetKg)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal realizedKg = hRows.stream()
                    .map(ManagementOrgHarvestSummaryRow::realizedKg)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal conversionRatePct = calculatePercentage(realizedKg, targetKg);

            list.add(new ManagementOrganizationSummaryDTO(
                    orgId,
                    orgName,
                    communitiesCount,
                    activeFarmers,
                    compliantFarmers,
                    complianceRatePct,
                    plansCount,
                    plantedAreaHa,
                    targetKg,
                    realizedKg,
                    conversionRatePct
            ));
        }

        // Ordenar por volume colhido desc, depois nome asc
        list.sort((a, b) -> {
            int cmp = b.realizedKg().compareTo(a.realizedKg());
            if (cmp != 0) return cmp;
            String nameA = a.organizationName() != null ? a.organizationName() : "";
            String nameB = b.organizationName() != null ? b.organizationName() : "";
            return nameA.compareToIgnoreCase(nameB);
        });

        return list;
    }

    private Manager getActiveManagerOrThrow(User loggedUser) {
        if (loggedUser == null || loggedUser.getId() == null) {
            throw new AccessDeniedException("Usuário não autenticado.");
        }
        Manager manager = managerRepository.findByUserId(loggedUser.getId())
                .orElseThrow(() -> new AccessDeniedException("Perfil de gestor não encontrado para o usuário logado."));

        if (manager.getDeletedAt() != null) {
            throw new AccessDeniedException("Perfil de gestor inativo ou excluído.");
        }
        return manager;
    }

    private HarvestSummaryInfoDTO toHarvestSummaryInfoDTO(Harvest harvest) {
        if (harvest == null) {
            return null;
        }
        return new HarvestSummaryInfoDTO(
                harvest.getId(),
                harvest.getLabel(),
                harvest.getStartDate(),
                harvest.getEndDate()
        );
    }

    private BigDecimal calculatePercentage(long part, long total) {
        if (total <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(part)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculatePercentage(BigDecimal part, BigDecimal total) {
        if (part == null || total == null || total.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return part.multiply(BigDecimal.valueOf(100))
                .divide(total, 2, RoundingMode.HALF_UP);
    }

    private record CropAggregate(
            UUID cropId,
            String cropName,
            String cropVariety,
            String unit,
            int plansCount,
            BigDecimal plantedAreaHa,
            BigDecimal targetKg,
            BigDecimal realizedKg,
            BigDecimal conversionRatePct,
            BigDecimal productionSharePct
    ) {
    }
}
