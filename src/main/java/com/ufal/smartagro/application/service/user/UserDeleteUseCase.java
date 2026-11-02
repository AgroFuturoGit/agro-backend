package com.ufal.smartagro.application.service.user;

import com.ufal.smartagro.application.service.audit.AuditLogService;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityInUseException;
import com.ufal.smartagro.domain.exception.UserNotFoundException;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.AuditAction;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
import com.ufal.smartagro.domain.port.out.ManagerRepository;
import com.ufal.smartagro.domain.port.out.TechnicianRepository;
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
public class UserDeleteUseCase {

    private final UserRepository userRepository;
    private final FarmerRepository farmerRepository;
    private final TechnicianRepository technicianRepository;
    private final ManagerRepository managerRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public void delete(UUID userId, User loggedUser) {
        if (loggedUser == null || loggedUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException();
        }

        if (loggedUser.getId() != null && loggedUser.getId().equals(userId)) {
            throw new IllegalArgumentException("O administrador não pode excluir a própria conta.");
        }

        User targetUser = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (farmerRepository.findByUserId(userId).filter(f -> f.getDeletedAt() == null).isPresent()) {
            throw new EntityInUseException(
                    "Não é possível excluir diretamente um usuário vinculado a um perfil de agricultor. Utilize a rota de exclusão do perfil (/farmers)."
            );
        }

        if (technicianRepository.findByUserId(userId).filter(t -> t.getDeletedAt() == null).isPresent()) {
            throw new EntityInUseException(
                    "Não é possível excluir diretamente um usuário vinculado a um perfil de técnico. Utilize a rota de exclusão do perfil (/technicians)."
            );
        }

        if (managerRepository.findByUserId(userId).filter(m -> m.getDeletedAt() == null).isPresent()) {
            throw new EntityInUseException(
                    "Não é possível excluir diretamente um usuário vinculado a um perfil de gestor."
            );
        }

        userRepository.softDelete(userId);
        auditLogService.record(
                loggedUser,
                AuditAction.DELETE,
                "user",
                userId,
                Map.of("targetEmail", targetUser.getEmail(), "targetRole", targetUser.getRole().name())
        );
        log.info("Usuário excluído logicamente [targetUserId={}] pelo administrador [actorId={}]", userId, loggedUser.getId());
    }
}
