package com.ufal.smartagro.application.service.technician;

import com.ufal.smartagro.adapters.in.web.dto.technician.TechnicianUpdateDTO;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.Technician;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.ProfessionalRegistrationType;
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
        if (loggedUser == null) {
            throw new AccessDeniedException("Apenas administradores ou o próprio técnico podem atualizar este perfil.");
        }

        Technician existing = technicianRepository.findById(id)
                .filter(t -> t.getDeletedAt() == null)
                .orElseThrow(() -> new EntityNotFoundException("Técnico não encontrado."));

        if (loggedUser.getRole() == Role.TECHNICIAN) {
            if (existing.getUser() == null || !existing.getUser().getId().equals(loggedUser.getId())) {
                throw new AccessDeniedException("O técnico só pode atualizar seu próprio perfil.");
            }
        } else if (loggedUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Apenas administradores ou o próprio técnico podem atualizar este perfil.");
        }

        ProfessionalRegistrationType regType;
        String regNum;

        if (dto.professionalId() != null) {
            regType = dto.registrationType();
            regNum = dto.registrationNumber() != null ? dto.registrationNumber() : dto.professionalId();
        } else {
            regType = dto.registrationType() != null ? dto.registrationType() : existing.getRegistrationType();
            regNum = dto.registrationNumber() != null ? dto.registrationNumber() : existing.getRegistrationNumber();
        }

        Technician updated = Technician.builder()
                .id(existing.getId())
                .user(existing.getUser())
                .registrationType(regType)
                .registrationNumber(regNum)
                .specialty(dto.specialty() != null ? dto.specialty() : existing.getSpecialty())
                .createdBy(existing.getCreatedBy())
                .createdAt(existing.getCreatedAt())
                .updatedAt(null)
                .deletedAt(existing.getDeletedAt())
                .build();

        return technicianRepository.save(updated);
    }
}
