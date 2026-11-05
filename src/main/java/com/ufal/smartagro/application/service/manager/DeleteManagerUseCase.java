package com.ufal.smartagro.application.service.manager;

import com.ufal.smartagro.application.service.audit.AuditLogService;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.Manager;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.AuditAction;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.ManagerRepository;
import com.ufal.smartagro.domain.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteManagerUseCase {

    private final ManagerRepository managerRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public void delete(UUID id, User loggedUser) {
        if (loggedUser == null || loggedUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Apenas administradores podem excluir gestores.");
        }

        Manager existing = managerRepository.findById(id)
                .filter(m -> m.getDeletedAt() == null)
                .or(() -> managerRepository.findByUserId(id).filter(m -> m.getDeletedAt() == null))
                .orElseThrow(() -> new EntityNotFoundException("Gestor não encontrado."));

        managerRepository.delete(existing.getId());

        if (existing.getUser() != null && existing.getUser().getId() != null) {
            userRepository.softDelete(existing.getUser().getId());
        }

        Map<String, Object> details = new HashMap<>();
        if (existing.getUser() != null && existing.getUser().getId() != null) {
            details.put("linkedUserId", existing.getUser().getId().toString());
        }
        if (existing.getOrganization() != null && existing.getOrganization().getId() != null) {
            details.put("organizationId", existing.getOrganization().getId().toString());
        }

        auditLogService.record(
                loggedUser,
                AuditAction.DELETE,
                "managers",
                existing.getId(),
                details
        );

        log.info("Gestor excluído logicamente [managerId={}, linkedUserId={}] pelo usuário [actorId={}]",
                existing.getId(),
                existing.getUser() != null ? existing.getUser().getId() : null,
                loggedUser.getId());
    }
}
