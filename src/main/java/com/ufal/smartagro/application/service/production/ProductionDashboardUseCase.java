package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.adapters.in.web.dto.production.ProductionDashboardDTO;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.model.*;
import com.ufal.smartagro.domain.model.enums.ExecutionStatus;
import com.ufal.smartagro.domain.model.enums.PlanStatus;
import com.ufal.smartagro.domain.port.out.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductionDashboardUseCase {
    private final FarmerRepository farmerRepository;
    private final ManagerRepository managerRepository;
    private final TechnicianRepository technicianRepository;
    private final TechnicalAssistanceRepository assistanceRepository;
    private final ProductionPlanRepository planRepository;
    private final ProductionExecutionRepository executionRepository;

    @Transactional(readOnly = true)
    public ProductionDashboardDTO getDashboard(User user) {
        if (user == null || user.getRole() == null) throw new AccessDeniedException("Acesso negado ao painel de produção.");

        List<Farmer> farmers = switch (user.getRole()) {
            case FARMER -> List.of(farmerRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new AccessDeniedException("Agricultor não encontrado.")));
            case TECHNICIAN -> assignedFarmers(user);
            case MANAGER -> organizationFarmers(user);
            case ADMIN -> farmerRepository.findAll().stream().filter(f -> f.getDeletedAt() == null).toList();
            default -> throw new AccessDeniedException("Acesso negado ao painel de produção.");
        };

        List<UUID> farmerIds = farmers.stream()
                .filter(f -> f.getDeletedAt() == null)
                .map(Farmer::getId)
                .filter(Objects::nonNull)
                .toList();

        List<ProductionPlan> allPlans = farmerIds.isEmpty() ? List.of()
                : planRepository.findAllByFarmerIdIn(farmerIds);

        List<ProductionPlan> activePlans = allPlans.stream()
                .filter(p -> p.getDeletedAt() == null && p.getStatus() != PlanStatus.CANCELED)
                .toList();

        List<UUID> planIds = activePlans.stream()
                .map(ProductionPlan::getId)
                .filter(Objects::nonNull)
                .toList();

        List<ProductionExecution> allExecutions = planIds.isEmpty() ? List.of()
                : executionRepository.findAllByProductionPlanIdIn(planIds);

        Map<UUID, List<ProductionPlan>> plansByFarmer = activePlans.stream()
                .filter(p -> p.getFarmer() != null && p.getFarmer().getId() != null)
                .collect(Collectors.groupingBy(p -> p.getFarmer().getId()));

        Map<UUID, BigDecimal> validatedYieldByPlan = allExecutions.stream()
                .filter(e -> e.getStatus() == ExecutionStatus.VALIDATED && e.getProductionPlan() != null && e.getProductionPlan().getId() != null)
                .collect(Collectors.groupingBy(
                        e -> e.getProductionPlan().getId(),
                        Collectors.reducing(BigDecimal.ZERO, ProductionExecution::getQuantityKg, BigDecimal::add)
                ));

        Map<UUID, CommunityTotals> totals = new LinkedHashMap<>();
        BigDecimal expected = BigDecimal.ZERO;
        BigDecimal actual = BigDecimal.ZERO;
        long planCount = 0;
        for (Farmer farmer : farmers) {
            if (farmer.getDeletedAt() != null) continue;
            UUID communityId = farmer.getCommunity() == null ? null : farmer.getCommunity().getId();
            String communityName = farmer.getCommunity() == null ? "Sem comunidade" : farmer.getCommunity().getName();
            CommunityTotals community = totals.computeIfAbsent(communityId, id -> new CommunityTotals(id, communityName));
            community.farmers++;

            List<ProductionPlan> plans = plansByFarmer.getOrDefault(farmer.getId(), List.of());
            for (ProductionPlan plan : plans) {
                BigDecimal target = plan.getExpectedYield() != null ? plan.getExpectedYield() : BigDecimal.ZERO;
                BigDecimal realized = validatedYieldByPlan.getOrDefault(plan.getId(), BigDecimal.ZERO);

                expected = expected.add(target);
                actual = actual.add(realized);
                planCount++;
                community.plans++;
                community.expected = community.expected.add(target);
                community.actual = community.actual.add(realized);
            }
        }
        List<ProductionDashboardDTO.CommunityProductionDTO> communityRows = totals.values().stream()
                .map(CommunityTotals::toDto).toList();
        return new ProductionDashboardDTO(farmers.size(), planCount, expected, actual,
                actual.subtract(expected), percentage(actual, expected), communityRows);
    }

    private List<Farmer> assignedFarmers(User user) {
        Technician technician = technicianRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AccessDeniedException("Técnico não encontrado."));
        Set<UUID> communityIds = assistanceRepository.findByTechnicianId(technician.getId()).stream()
                .filter(TechnicalAssistance::isActive).map(TechnicalAssistance::getCommunity)
                .filter(Objects::nonNull).map(Community::getId).collect(Collectors.toSet());
        if (communityIds.isEmpty()) {
            return List.of();
        }
        return farmerRepository.findAllByCommunityIdIn(communityIds);
    }

    private List<Farmer> organizationFarmers(User user) {
        Manager manager = managerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AccessDeniedException("Gestor não encontrado."));
        if (manager.getOrganization() == null) throw new AccessDeniedException("Gestor não possui organização vinculada.");
        UUID orgId = manager.getOrganization().getId();
        return farmerRepository.findAllByOrganizationId(orgId);
    }

    private static BigDecimal percentage(BigDecimal actual, BigDecimal expected) {
        return expected.signum() == 0 ? BigDecimal.ZERO
                : actual.multiply(BigDecimal.valueOf(100)).divide(expected, 2, RoundingMode.HALF_UP);
    }

    private static class CommunityTotals {
        private final UUID id;
        private final String name;
        private long farmers;
        private long plans;
        private BigDecimal expected = BigDecimal.ZERO;
        private BigDecimal actual = BigDecimal.ZERO;

        CommunityTotals(UUID id, String name) {
            this.id = id;
            this.name = name;
        }

        ProductionDashboardDTO.CommunityProductionDTO toDto() {
            BigDecimal diff = actual.subtract(expected);
            return new ProductionDashboardDTO.CommunityProductionDTO(id, name, farmers, plans,
                    expected, actual, diff, percentage(actual, expected));
        }
    }
}
