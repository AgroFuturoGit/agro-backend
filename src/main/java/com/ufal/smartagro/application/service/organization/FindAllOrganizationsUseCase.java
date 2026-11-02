package com.ufal.smartagro.application.service.organization;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.model.Organization;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FindAllOrganizationsUseCase {

    private final OrganizationRepository organizationRepository;

    @Transactional(readOnly = true)
    public List<Organization> findAll() {
        return findAll(null);
    }

    @Transactional(readOnly = true)
    public List<Organization> findAll(User loggedUser) {
        if (loggedUser == null || loggedUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Apenas administradores podem listar todas as organizações.");
        }
        return organizationRepository.findAll();
    }
}
