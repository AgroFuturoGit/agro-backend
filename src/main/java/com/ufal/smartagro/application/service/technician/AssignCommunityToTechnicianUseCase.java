package com.ufal.smartagro.application.service.technician;

import com.ufal.smartagro.adapters.in.web.dto.technicalassistance.TechnicalAssistanceRegisterDTO;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.Community;
import com.ufal.smartagro.domain.model.TechnicalAssistance;
import com.ufal.smartagro.domain.model.Technician;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.CommunityRepository;
import com.ufal.smartagro.domain.port.out.ManagerRepository;
import com.ufal.smartagro.domain.port.out.TechnicalAssistanceRepository;
import com.ufal.smartagro.domain.port.out.TechnicianRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssignCommunityToTechnicianUseCase {

    private final TechnicianRepository technicianRepository;
    private final CommunityRepository communityRepository;
    private final TechnicalAssistanceRepository assistanceRepository;
    private final ManagerRepository managerRepository;

    @Transactional
    public TechnicalAssistance assign(UUID technicianId, TechnicalAssistanceRegisterDTO dto, User loggedUser) {
        if (dto == null) {
            throw new IllegalArgumentException("Dados de assistência técnica são obrigatórios.");
        }
        if (technicianId != null && dto.technicianId() != null && !technicianId.equals(dto.technicianId())) {
            throw new IllegalArgumentException("O ID do técnico informado na URL não coincide com o do corpo da requisição.");
        }

        UUID effectiveTechnicianId = dto.technicianId() != null ? dto.technicianId() : technicianId;
        if (effectiveTechnicianId == null) {
            throw new IllegalArgumentException("O ID do técnico é obrigatório.");
        }
        if (dto.communityId() == null) {
            throw new IllegalArgumentException("O ID da comunidade é obrigatório.");
        }
        if (dto.startDate() == null) {
            throw new IllegalArgumentException("A data de início é obrigatória.");
        }

        if (loggedUser == null) {
            throw new AccessDeniedException("Apenas administradores ou gestores podem criar assistência técnica.");
        }
        Role role = loggedUser.getRole();
        if (role != Role.ADMIN && role != Role.MANAGER) {
            throw new AccessDeniedException("Apenas administradores ou gestores podem criar assistência técnica.");
        }

        Community community = communityRepository.findById(dto.communityId())
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new EntityNotFoundException("Comunidade não encontrada."));

        if (role == Role.MANAGER) {
            var manager = managerRepository.findByUserId(loggedUser.getId())
                    .orElseThrow(() -> new AccessDeniedException("Gestor não encontrado."));
            if (manager.getOrganization() == null || community.getOrganization() == null ||
                    !community.getOrganization().getId().equals(manager.getOrganization().getId())) {
                throw new AccessDeniedException("O gestor só pode vincular comunidades da sua organização.");
            }
        }

        Technician technician = technicianRepository.findById(effectiveTechnicianId)
                .filter(t -> t.getDeletedAt() == null)
                .orElseThrow(() -> new EntityNotFoundException("Técnico não encontrado."));

        boolean existsActive = assistanceRepository.findActiveByTechnicianAndCommunity(technician.getId(), community.getId()).isPresent();
        if (existsActive) {
            throw new IllegalArgumentException("Já existe uma assistência ativa entre este técnico e comunidade.");
        }

        TechnicalAssistance assistance = new TechnicalAssistance(
                null,
                technician,
                community,
                dto.startDate(),
                null,
                null,
                null,
                null
        );
        return assistanceRepository.save(assistance);
    }

    @Transactional
    public TechnicalAssistance assign(TechnicalAssistanceRegisterDTO dto, User loggedUser) {
        return assign(null, dto, loggedUser);
    }
}
