package com.ufal.smartagro.application.service.technician;

import com.ufal.smartagro.adapters.in.web.dto.technician.TechnicianUpdateDTO;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
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
public class UpdateTechnicianUseCase {

    private final TechnicianRepository technicianRepository;

    @Transactional
    public Technician update(UUID id, TechnicianUpdateDTO dto, User loggedUser) {
        Technician existing = technicianRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Técnico não encontrado."));

        if (loggedUser.getRole() == Role.TECHNICIAN) {
            if (existing.getUser() == null || !existing.getUser().getId().equals(loggedUser.getId())) {
                throw new AccessDeniedException("O técnico só pode atualizar seu próprio perfil.");
            }
        } else if (loggedUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Apenas administradores ou o próprio técnico podem atualizar este perfil.");
        }

        Technician updated = new Technician(
                existing.getId(),
                existing.getUser(),
                dto.professionalId() != null ? dto.professionalId() : existing.getProfessionalId(),
                dto.specialty() != null ? dto.specialty() : existing.getSpecialty(),
                existing.getCreatedAt(),
                null,
                existing.getDeletedAt()
        );

        return technicianRepository.save(updated);
    }
}
