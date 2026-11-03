package com.ufal.smartagro.application.service.community;

import com.ufal.smartagro.adapters.in.web.dto.community.CommunityUpdateDTO;
import com.ufal.smartagro.application.service.audit.AuditLogService;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.Community;
import com.ufal.smartagro.domain.model.Manager;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.AuditAction;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.CommunityRepository;
import com.ufal.smartagro.domain.port.out.ManagerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateCommunityUseCase {

    private final CommunityRepository communityRepository;
    private final ManagerRepository managerRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public Community update(UUID id, CommunityUpdateDTO dto) {
        return update(id, dto, null);
    }

    @Transactional
    public Community update(UUID id, CommunityUpdateDTO dto, User loggedUser) {
        if (loggedUser != null && loggedUser.getRole() != Role.ADMIN && loggedUser.getRole() != Role.MANAGER) {
            throw new AccessDeniedException("Papel não autorizado para atualizar comunidade.");
        }

        Community existingCommunity = communityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comunidade não encontrada."));

        if (loggedUser != null && loggedUser.getRole() == Role.MANAGER) {
            Manager manager = managerRepository.findByUserId(loggedUser.getId())
                    .orElseThrow(() -> new AccessDeniedException("Gestor não encontrado."));
            UUID managerOrgId = manager.getOrganization() != null ? manager.getOrganization().getId() : null;
            UUID communityOrgId = existingCommunity.getOrganization() != null ? existingCommunity.getOrganization().getId() : null;
            if (managerOrgId == null || communityOrgId == null || !managerOrgId.equals(communityOrgId)) {
                throw new AccessDeniedException("O gestor só pode atualizar comunidades da sua própria organização.");
            }
        }

        Community updatedCommunity = new Community(
                existingCommunity.getId(),
                dto.name(),
                existingCommunity.getOrganization(),
                existingCommunity.getCreatedAt(),
                LocalDateTime.now(),
                existingCommunity.getDeletedAt()
        );

        Community saved = communityRepository.save(updatedCommunity);

        if (loggedUser != null) {
            Map<String, Object> details = new HashMap<>();
            details.put("name", saved.getName());
            if (saved.getOrganization() != null && saved.getOrganization().getId() != null) {
                details.put("organizationId", saved.getOrganization().getId().toString());
            }
            auditLogService.record(
                    loggedUser,
                    AuditAction.UPDATE,
                    "communities",
                    saved.getId(),
                    details
            );
        }

        return saved;
    }
}
