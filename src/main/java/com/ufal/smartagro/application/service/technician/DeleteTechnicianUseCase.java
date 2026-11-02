package com.ufal.smartagro.application.service.technician;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.TechnicalAssistance;
import com.ufal.smartagro.domain.model.Technician;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.TechnicalAssistanceRepository;
import com.ufal.smartagro.domain.port.out.TechnicianRepository;
import com.ufal.smartagro.domain.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteTechnicianUseCase {

    private final TechnicianRepository technicianRepository;
    private final UserRepository userRepository;
    private final TechnicalAssistanceRepository technicalAssistanceRepository;

    @Transactional
    public void delete(UUID id, User loggedUser) {
        if (loggedUser == null || loggedUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Apenas administradores podem excluir técnicos.");
        }

        Technician existing = technicianRepository.findById(id)
                .filter(t -> t.getDeletedAt() == null)
                .orElseThrow(() -> new EntityNotFoundException("Técnico não encontrado."));

        List<TechnicalAssistance> activeAssistances = technicalAssistanceRepository.findByTechnicianId(id).stream()
                .filter(TechnicalAssistance::isActive)
                .toList();

        for (TechnicalAssistance assistance : activeAssistances) {
            technicalAssistanceRepository.save(new TechnicalAssistance(
                    assistance.getId(),
                    assistance.getTechnician(),
                    assistance.getCommunity(),
                    assistance.getStartDate(),
                    LocalDateTime.now(),
                    assistance.getCreatedAt(),
                    LocalDateTime.now(),
                    null
            ));
        }

        technicianRepository.delete(id);

        if (existing.getUser() != null && existing.getUser().getId() != null) {
            userRepository.softDelete(existing.getUser().getId());
        }

        log.info("Técnico excluído logicamente [technicianId={}, linkedUserId={}] pelo usuário [actorId={}]",
                id,
                existing.getUser() != null ? existing.getUser().getId() : null,
                loggedUser.getId());
    }
}
