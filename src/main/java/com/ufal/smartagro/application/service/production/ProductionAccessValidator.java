package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.model.Manager;
import com.ufal.smartagro.domain.model.Producer;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.ManagerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductionAccessValidator {

    private final ManagerRepository managerRepository;

    public void validateAccess(Producer producer, User loggedUser) {
        if (producer == null) return;

        if (loggedUser.getRole() == Role.PRODUCER) {
            if (producer.getUser() == null || !producer.getUser().getId().equals(loggedUser.getId())) {
                throw new AccessDeniedException("O produtor só tem acesso aos seus próprios dados de produção.");
            }
        } else if (loggedUser.getRole() == Role.MANAGER) {
            Manager manager = managerRepository.findByUserId(loggedUser.getId())
                    .orElseThrow(() -> new AccessDeniedException("Gestor não encontrado."));

            UUID managerOrgId = manager.getOrganization() != null ? manager.getOrganization().getId() : null;
            UUID producerOrgId = (producer.getCommunity() != null && producer.getCommunity().getOrganization() != null)
                    ? producer.getCommunity().getOrganization().getId()
                    : null;

            if (managerOrgId == null || producerOrgId == null || !managerOrgId.equals(producerOrgId)) {
                throw new AccessDeniedException("O gestor só tem acesso aos produtores da sua própria organização.");
            }
        }
    }
}
