package com.ufal.smartagro.application.service.technician;

import com.ufal.smartagro.adapters.in.web.dto.technicalassistance.TechnicalAssistanceRegisterDTO;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
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
public class AssignProducerToTechnicianUseCase {

    private final TechnicianRepository technicianRepository;
    private final ProducerRepository producerRepository;
    private final TechnicalAssistanceRepository assistanceRepository;
    private final ManagerRepository managerRepository;

    @Transactional
    public TechnicalAssistance assign(TechnicalAssistanceRegisterDTO dto, User loggedUser) {
        // Validate role and ownership
        Role role = loggedUser.getRole();
        if (role == Role.ADMIN) {
            // allowed
        } else if (role == Role.MANAGER) {
            // Manager must belong to same organization as producer
            var manager = managerRepository.findByUserId(loggedUser.getId())
                    .orElseThrow(() -> new AccessDeniedException("Gestor não encontrado."));
            var producer = producerRepository.findById(dto.producerId())
                    .orElseThrow(() -> new IllegalArgumentException("Produtor não encontrado."));
            if (producer.getCommunity() == null || producer.getCommunity().getOrganization() == null ||
                    !producer.getCommunity().getOrganization().getId().equals(manager.getOrganization().getId())) {
                throw new AccessDeniedException("O gestor só pode vincular produtores da sua organização.");
            }
        } else if (role == Role.PRODUCER) {
            // Producer can only link himself
            var producer = producerRepository.findById(dto.producerId())
                    .orElseThrow(() -> new IllegalArgumentException("Produtor não encontrado."));
            if (!producer.getUser().getId().equals(loggedUser.getId())) {
                throw new AccessDeniedException("Produtor só pode vincular a si mesmo.");
            }
        } else {
            throw new AccessDeniedException("Apenas administradores, gestores ou produtores podem criar assistência técnica.");
        }

        Technician technician = technicianRepository.findById(dto.technicianId())
                .orElseThrow(() -> new IllegalArgumentException("Técnico não encontrado."));
        Producer producer = producerRepository.findById(dto.producerId())
                .orElseThrow(() -> new IllegalArgumentException("Produtor não encontrado."));

        // Check if there is already an active assistance between them
        boolean existsActive = assistanceRepository.findActiveByTechnicianAndProducer(technician.getId(), producer.getId()).isPresent();
        if (existsActive) {
            throw new IllegalArgumentException("Já existe uma assistência ativa entre este técnico e produtor.");
        }

        TechnicalAssistance assistance = new TechnicalAssistance(
                null,
                technician,
                producer,
                dto.startDate(),
                null,
                null,
                null,
                null
        );
        return assistanceRepository.save(assistance);
    }
}
