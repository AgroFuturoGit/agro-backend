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
public class FindTechnicianByUserUseCase {

    private final TechnicianRepository technicianRepository;

    @Transactional(readOnly = true)
    public Technician findByUserId(UUID userId) {
        return technicianRepository.findByUserId(userId)
                .filter(t -> t.getDeletedAt() == null)
                .orElseThrow(() -> new EntityNotFoundException("Técnico não encontrado."));
    }

    @Transactional(readOnly = true)
    public Technician findByUser(User loggedUser) {
        if (loggedUser == null || loggedUser.getRole() != Role.TECHNICIAN) {
            throw new AccessDeniedException("Acesso negado para visualizar dados de técnico.");
        }
        return findByUserId(loggedUser.getId());
    }
}
