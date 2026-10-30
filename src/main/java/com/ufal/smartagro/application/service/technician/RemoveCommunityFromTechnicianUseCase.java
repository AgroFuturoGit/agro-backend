package com.ufal.smartagro.application.service.technician;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.TechnicalAssistance;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.ManagerRepository;
import com.ufal.smartagro.domain.port.out.TechnicalAssistanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RemoveCommunityFromTechnicianUseCase {

    private final TechnicalAssistanceRepository assistanceRepository;
    private final ManagerRepository managerRepository;

    @Transactional
    public TechnicalAssistance remove(UUID assistanceId, User loggedUser) {
        return remove(null, assistanceId, loggedUser);
    }

    @Transactional
    public TechnicalAssistance remove(UUID technicianId, UUID assistanceId, User loggedUser) {
        if (loggedUser == null) {
            throw new AccessDeniedException("Acesso negado para encerrar assistência.");
        }
        Role role = loggedUser.getRole();
        TechnicalAssistance assistance = assistanceRepository.findById(assistanceId)
                .filter(a -> a.getDeletedAt() == null)
                .orElseThrow(() -> new EntityNotFoundException("Assistência não encontrada."));

        if (technicianId != null) {
            if (assistance.getTechnician() == null || !technicianId.equals(assistance.getTechnician().getId())) {
                throw new EntityNotFoundException("Assistência técnica não encontrada para o técnico informado.");
            }
        }

        if (assistance.getEndDate() != null) {
            throw new IllegalArgumentException("Assistência técnica já se encontra encerrada.");
        }

        if (role == Role.ADMIN) {
            // allowed
        } else if (role == Role.MANAGER) {
            var manager = managerRepository.findByUserId(loggedUser.getId())
                    .orElseThrow(() -> new AccessDeniedException("Gestor não encontrado."));
            var community = assistance.getCommunity();
            if (community == null || community.getOrganization() == null ||
                    manager.getOrganization() == null ||
                    !community.getOrganization().getId().equals(manager.getOrganization().getId())) {
                throw new AccessDeniedException("O gestor só pode encerrar assistência de comunidades da sua organização.");
            }
        } else if (role == Role.TECHNICIAN) {
            var technician = assistance.getTechnician();
            if (technician == null || technician.getUser() == null ||
                    !technician.getUser().getId().equals(loggedUser.getId())) {
                throw new AccessDeniedException("Técnico só pode encerrar sua própria assistência.");
            }
        } else {
            throw new AccessDeniedException("Acesso negado para encerrar assistência.");
        }

        assistance = new TechnicalAssistance(
                assistance.getId(),
                assistance.getTechnician(),
                assistance.getCommunity(),
                assistance.getStartDate(),
                LocalDateTime.now(),
                assistance.getCreatedAt(),
                assistance.getUpdatedAt(),
                assistance.getDeletedAt()
        );
        return assistanceRepository.save(assistance);
    }
}
