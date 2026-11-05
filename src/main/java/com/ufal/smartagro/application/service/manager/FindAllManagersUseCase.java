package com.ufal.smartagro.application.service.manager;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.model.Manager;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.ManagerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindAllManagersUseCase {

    private final ManagerRepository managerRepository;

    @Transactional(readOnly = true)
    public List<Manager> findAll(UUID organizationId, User loggedUser) {
        if (loggedUser == null || loggedUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Apenas administradores podem listar gestores.");
        }

        return managerRepository.findAll().stream()
                .filter(m -> m.getDeletedAt() == null)
                .filter(m -> organizationId == null
                        || (m.getOrganization() != null && organizationId.equals(m.getOrganization().getId())))
                .toList();
    }
}
