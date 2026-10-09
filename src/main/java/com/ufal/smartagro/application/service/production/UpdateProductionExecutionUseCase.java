package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.adapters.in.web.dto.production.ProductionExecutionUpdateDTO;
import com.ufal.smartagro.adapters.in.web.mapper.Mapper;
import com.ufal.smartagro.domain.model.ProductionExecution;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.ProductionExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** Atualiza quantidades, horário, validação e localização de um apontamento existente. */
@Service
@RequiredArgsConstructor
public class UpdateProductionExecutionUseCase {

    private final ProductionExecutionRepository productionExecutionRepository;
    private final ProductionAccessValidator accessValidator;
    private final ProductionVersionGuard versionGuard;

    @Transactional
    /** Mantém campos omitidos, valida acesso/versão e persiste as alterações recebidas. */
    public ProductionExecution update(UUID executionId, ProductionExecutionUpdateDTO dto, User loggedUser) {
        ProductionExecution existingExecution = productionExecutionRepository.findById(executionId)
                .orElseThrow(() -> new IllegalArgumentException("Execução de produção não encontrada."));

        if (existingExecution.getProductionPlan() != null) {
            accessValidator.validateWriteAccess(existingExecution.getProductionPlan().getFarmer(), loggedUser);
        }

        Localizacao localizacao = resolverLocalizacao(dto, existingExecution);

        versionGuard.ensureUpToDate(
                dto.baseUpdatedAt(),
                existingExecution.getUpdatedAt(),
                existingExecution.getCreatedAt(),
                Mapper.toProductionExecutionResponseDTO(existingExecution),
                "O apontamento foi alterado por outro usuário depois que esta edição começou."
        );

        ProductionExecution updatedExecution = new ProductionExecution(
                existingExecution.getId(),
                existingExecution.getProductionPlan(),
                dto.quantity() != null ? dto.quantity() : existingExecution.getQuantity(),
                dto.quantityKg() != null ? dto.quantityKg() : existingExecution.getQuantityKg(),
                dto.harvestedAt() != null ? dto.harvestedAt() : existingExecution.getHarvestedAt(),
                dto.status() != null ? dto.status() : existingExecution.getStatus(),
                dto.validatedBy() != null ? dto.validatedBy() : existingExecution.getValidatedBy(),
                dto.validatedAt() != null ? dto.validatedAt() : existingExecution.getValidatedAt(),
                dto.notes() != null ? dto.notes() : existingExecution.getNotes(),
                localizacao.latitude(),
                localizacao.longitude(),
                localizacao.accuracy(),
                localizacao.recordedAt(),
                existingExecution.getCreatedAt(),
                null,
                existingExecution.getDeletedAt()
        );

        return productionExecutionRepository.save(updatedExecution);
    }

    private record Localizacao(
            BigDecimal latitude,
            BigDecimal longitude,
            BigDecimal accuracy,
            LocalDateTime recordedAt
    ) {}

    /**
     * Decide o destino da localização numa edição, entre três intenções que um
     * campo nulo sozinho não distingue:
     *
     * <ul>
     *   <li>{@code clearLocation} — apagar a posição de propósito, para quando o
     *       usuário reconhece que ela não corresponde ao local da colheita;</li>
     *   <li>coordenada presente — recaptura: substitui tudo, inclusive precisão
     *       e carimbo, que pertencem àquela leitura e não podem ficar para trás;</li>
     *   <li>nada — preserva. Um formulário salvo sem GPS não pode apagar a
     *       posição registrada em campo.</li>
     * </ul>
     */
    /** Interpreta a intenção de manter, substituir ou limpar a posição GPS no apontamento. */
    private Localizacao resolverLocalizacao(
            ProductionExecutionUpdateDTO dto,
            ProductionExecution existente
    ) {
        if (Boolean.TRUE.equals(dto.clearLocation())) {
            return new Localizacao(null, null, null, null);
        }

        if (dto.latitude() != null && dto.longitude() != null) {
            return new Localizacao(
                    dto.latitude(),
                    dto.longitude(),
                    dto.locationAccuracy(),
                    dto.locationRecordedAt()
            );
        }

        return new Localizacao(
                existente.getLatitude(),
                existente.getLongitude(),
                existente.getLocationAccuracy(),
                existente.getLocationRecordedAt()
        );
    }
}
