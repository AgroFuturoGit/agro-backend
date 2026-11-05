package com.ufal.smartagro.application.service.manager;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.Manager;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.ManagerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindManagerByIdUseCase {

    private final ManagerRepository managerRepository;

    @Transactional(readOnly = true)
    public Manager findById(UUID id, User loggedUser) {
        if (loggedUser == null) {
            throw new AccessDeniedException("Apenas administradores e gestores podem consultar gestores por ID.");
        }

        Manager manager = managerRepository.findById(id)
                .filter(m -> m.getDeletedAt() == null)
                .or(() -> managerRepository.findByUserId(id).filter(m -> m.getDeletedAt() == null))
                .orElseThrow(() -> new EntityNotFoundException("Gestor não encontrado."));

        if (loggedUser.getRole() == Role.ADMIN) {
            return manager;
        } else if (loggedUser.getRole() == Role.MANAGER) {
            Manager self = managerRepository.findByUserId(loggedUser.getId())
                    .filter(m -> m.getDeletedAt() == null)
                    .orElseThrow(() -> new AccessDeniedException("Gestor não encontrado."));

            if (self.getOrganization() != null
                    && manager.getOrganization() != null
                    && self.getOrganization().getId().equals(manager.getOrganization().getId())) {
                return manager;
            }
            throw new AccessDeniedException("O gestor só tem acesso aos gestores da sua própria organização.");
        }

        throw new AccessDeniedException("Apenas administradores e gestores podem consultar gestores por ID.");
    }
}
