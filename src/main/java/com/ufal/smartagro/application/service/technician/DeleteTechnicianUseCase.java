package com.ufal.smartagro.application.service.technician;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.Technician;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.TechnicianRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteTechnicianUseCase {

    private final TechnicianRepository technicianRepository;

    @Transactional
    public void delete(UUID id, User loggedUser) {
        if (loggedUser == null || loggedUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Apenas administradores podem excluir técnicos.");
        }

        Technician existing = technicianRepository.findById(id)
                .filter(t -> t.getDeletedAt() == null)
                .orElseThrow(() -> new EntityNotFoundException("Técnico não encontrado."));

        technicianRepository.delete(id);
    }
}
