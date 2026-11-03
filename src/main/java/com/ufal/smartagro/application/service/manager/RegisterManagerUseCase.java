package com.ufal.smartagro.application.service.manager;

import com.ufal.smartagro.adapters.in.web.dto.manager.ManagerRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.user.UserRegisterDTO;
import com.ufal.smartagro.application.service.audit.AuditLogService;
import com.ufal.smartagro.application.service.user.UserRegisterUseCase;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.Manager;
import com.ufal.smartagro.domain.model.Organization;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.AuditAction;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.ManagerRepository;
import com.ufal.smartagro.domain.port.out.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class RegisterManagerUseCase {

    private final ManagerRepository managerRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRegisterUseCase userRegisterUseCase;
    private final AuditLogService auditLogService;

    @Transactional
    public Manager register(UUID organizationId, ManagerRegisterDTO dto, User loggedUser) {
        if (loggedUser == null || loggedUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Acesso negado. Somente administradores podem realizar esta ação.");
        }

        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Organização não encontrada"));

        UserRegisterDTO userDto = new UserRegisterDTO(
                dto.fullName(), dto.email(), dto.password(), dto.cpf(), Role.MANAGER
        );
        User savedUser = userRegisterUseCase.createBaseUser(userDto);

        Manager manager = new Manager(
                null,
                savedUser,
                organization,
                null,
                null,
                null
        );

        Manager savedManager = managerRepository.save(manager);

        Map<String, Object> details = new HashMap<>();
        details.put("userId", savedUser.getId() != null ? savedUser.getId().toString() : null);
        details.put("organizationId", organizationId.toString());

        auditLogService.record(
                loggedUser,
                AuditAction.CREATE,
                "managers",
                savedManager.getId(),
                details
        );

        return savedManager;
    }
}
