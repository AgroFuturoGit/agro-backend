package com.ufal.smartagro.application.service.technician;

import com.ufal.smartagro.adapters.in.web.dto.technicalassistance.TechnicalAssistanceRegisterDTO;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.Technician;
import com.ufal.smartagro.domain.model.Producer;
import com.ufal.smartagro.domain.model.TechnicalAssistance;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.TechnicianRepository;
import com.ufal.smartagro.domain.port.out.ProducerRepository;
import com.ufal.smartagro.domain.port.out.TechnicalAssistanceRepository;
import com.ufal.smartagro.domain.port.out.ManagerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RemoveProducerFromTechnicianUseCase {

    private final TechnicianRepository technicianRepository;
    private final ProducerRepository producerRepository;
    private final TechnicalAssistanceRepository assistanceRepository;
    private final ManagerRepository managerRepository;

    @Transactional
    public TechnicalAssistance remove(UUID assistanceId, User loggedUser) {
        Role role = loggedUser.getRole();
        TechnicalAssistance assistance = assistanceRepository.findById(assistanceId)
                .orElseThrow(() -> new EntityNotFoundException("Assistência não encontrada."));

        // Authorization checks
        if (role == Role.ADMIN) {
            // allowed
        } else if (role == Role.MANAGER) {
            var manager = managerRepository.findByUserId(loggedUser.getId())
                    .orElseThrow(() -> new AccessDeniedException("Gestor não encontrado."));
            var producer = assistance.getProducer();
            if (producer.getCommunity() == null || producer.getCommunity().getOrganization() == null ||
                    !producer.getCommunity().getOrganization().getId().equals(manager.getOrganization().getId())) {
                throw new AccessDeniedException("O gestor só pode encerrar assistência de produtores da sua organização.");
            }
        } else if (role == Role.PRODUCER) {
            var producer = assistance.getProducer();
            if (!producer.getUser().getId().equals(loggedUser.getId())) {
                throw new AccessDeniedException("Produtor só pode encerrar sua própria assistência.");
            }
        } else if (role == Role.TECHNICIAN) {
            var technician = assistance.getTechnician();
            if (!technician.getUser().getId().equals(loggedUser.getId())) {
                throw new AccessDeniedException("Técnico só pode encerrar sua própria assistência.");
            }
        } else {
            throw new AccessDeniedException("Acesso negado para encerrar assistência.");
        }

        // Mark the assistance as ended
        assistance = new TechnicalAssistance(
                assistance.getId(),
                assistance.getTechnician(),
                assistance.getProducer(),
                assistance.getStartDate(),
                LocalDateTime.now(),
                assistance.getCreatedAt(),
                assistance.getUpdatedAt(),
                assistance.getDeletedAt()
        );
        return assistanceRepository.save(assistance);
    }
}
