package com.ufal.smartagro.application.service.technician;

import com.ufal.smartagro.adapters.in.web.dto.technicalassistance.TechnicalAssistanceRegisterDTO;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.model.Technician;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.TechnicalAssistance;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.TechnicianRepository;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
import com.ufal.smartagro.domain.port.out.TechnicalAssistanceRepository;
import com.ufal.smartagro.domain.port.out.ManagerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssignFarmerToTechnicianUseCase {

    private final TechnicianRepository technicianRepository;
    private final FarmerRepository farmerRepository;
    private final TechnicalAssistanceRepository assistanceRepository;
    private final ManagerRepository managerRepository;

    @Transactional
    public TechnicalAssistance assign(TechnicalAssistanceRegisterDTO dto, User loggedUser) {
        // Validate role and ownership
        Role role = loggedUser.getRole();
        if (role == Role.ADMIN) {
            // allowed
        } else if (role == Role.MANAGER) {
            // Manager must belong to same organization as farmer
            var manager = managerRepository.findByUserId(loggedUser.getId())
                    .orElseThrow(() -> new AccessDeniedException("Gestor não encontrado."));
            var farmer = farmerRepository.findById(dto.farmerId())
                    .orElseThrow(() -> new IllegalArgumentException("Agricultor não encontrado."));
            if (farmer.getCommunity() == null || farmer.getCommunity().getOrganization() == null ||
                    !farmer.getCommunity().getOrganization().getId().equals(manager.getOrganization().getId())) {
                throw new AccessDeniedException("O gestor só pode vincular agricultores da sua organização.");
            }
        } else if (role == Role.FARMER) {
            // Farmer can only link himself
            var farmer = farmerRepository.findById(dto.farmerId())
                    .orElseThrow(() -> new IllegalArgumentException("Agricultor não encontrado."));
            if (!farmer.getUser().getId().equals(loggedUser.getId())) {
                throw new AccessDeniedException("Agricultor só pode vincular a si mesmo.");
            }
        } else {
            throw new AccessDeniedException("Apenas administradores, gestores ou agricultores podem criar assistência técnica.");
        }

        Technician technician = technicianRepository.findById(dto.technicianId())
                .orElseThrow(() -> new IllegalArgumentException("Técnico não encontrado."));
        Farmer farmer = farmerRepository.findById(dto.farmerId())
                .orElseThrow(() -> new IllegalArgumentException("Agricultor não encontrado."));

        // Check if there is already an active assistance between them
        boolean existsActive = assistanceRepository.findActiveByTechnicianAndFarmer(technician.getId(), farmer.getId()).isPresent();
        if (existsActive) {
            throw new IllegalArgumentException("Já existe uma assistência ativa entre este técnico e agricultor.");
        }

        TechnicalAssistance assistance = new TechnicalAssistance(
                null,
                technician,
                farmer,
                dto.startDate(),
                null,
                null,
                null,
                null
        );
        return assistanceRepository.save(assistance);
    }
}
