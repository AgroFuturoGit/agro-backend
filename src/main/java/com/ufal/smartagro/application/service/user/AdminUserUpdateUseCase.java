package com.ufal.smartagro.application.service.user;

import com.ufal.smartagro.adapters.in.web.dto.user.AdminUserUpdateDTO;
import com.ufal.smartagro.application.service.audit.AuditLogService;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.UserNotFoundException;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.AuditAction;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class AdminUserUpdateUseCase {

    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public User update(UUID userId, AdminUserUpdateDTO dto, User loggedUser) {
        if (loggedUser == null || loggedUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException();
        }

        if (loggedUser.getId() != null && loggedUser.getId().equals(userId) && dto.role() != Role.ADMIN) {
            throw new IllegalArgumentException("O administrador não pode rebaixar o próprio papel.");
        }

        User existingUser = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (dto.role() != existingUser.getRole()) {
            if (dto.role() == Role.MANAGER || dto.role() == Role.FARMER || dto.role() == Role.TECHNICIAN) {
                throw new IllegalArgumentException("Para criar ou promover gestores, produtores e técnicos, utilize as rotas específicas de perfil.");
            }
            if (existingUser.getRole() == Role.MANAGER || existingUser.getRole() == Role.FARMER || existingUser.getRole() == Role.TECHNICIAN) {
                throw new IllegalArgumentException("Não é permitido alterar o papel de um usuário que já possui perfil de gestor, produtor ou técnico vinculado.");
            }
            log.info("Papel de usuário alterado pelo administrador [actorId={}, actorEmail='{}'] no usuário alvo [targetUserId={}]: '{}' -> '{}'",
                    loggedUser.getId(), loggedUser.getEmail(), userId, existingUser.getRole(), dto.role());
        }

        User updatedUser = new User(
                existingUser.getId(),
                dto.fullName(),
                existingUser.getEmail(),
                existingUser.getPassword(),
                existingUser.getCpf(),
                dto.role()
        );

        User savedUser = userRepository.save(updatedUser);
        auditLogService.record(
                loggedUser,
                AuditAction.UPDATE,
                "user",
                userId,
                Map.of("targetEmail", existingUser.getEmail(), "previousRole", existingUser.getRole().name(), "newRole", dto.role().name())
        );
        log.info("Usuário atualizado com sucesso pelo administrador [actorId={}] no usuário alvo [targetUserId={}]", loggedUser.getId(), userId);
        return savedUser;
    }
}
