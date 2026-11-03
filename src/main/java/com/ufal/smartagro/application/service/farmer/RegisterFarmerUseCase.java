package com.ufal.smartagro.application.service.farmer;

import com.ufal.smartagro.adapters.in.web.dto.farmer.FarmerRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.user.UserRegisterDTO;
import com.ufal.smartagro.application.service.audit.AuditLogService;
import com.ufal.smartagro.application.service.user.UserRegisterUseCase;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.CpfAlreadyExistsException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.Community;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.Manager;
import com.ufal.smartagro.domain.model.Technician;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.AuditAction;
import com.ufal.smartagro.domain.model.enums.RegistrationSource;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.CommunityRepository;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
import com.ufal.smartagro.domain.port.out.ManagerRepository;
import com.ufal.smartagro.domain.port.out.TechnicalAssistanceRepository;
import com.ufal.smartagro.domain.port.out.TechnicianRepository;
import com.ufal.smartagro.domain.util.CpfNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class RegisterFarmerUseCase {

    private final FarmerRepository farmerRepository;
    private final CommunityRepository communityRepository;
    private final ManagerRepository managerRepository;
    private final TechnicianRepository technicianRepository;
    private final TechnicalAssistanceRepository technicalAssistanceRepository;
    private final UserRegisterUseCase userRegisterUseCase;
    private final AuditLogService auditLogService;

    @Transactional
    public Farmer register(UUID communityId, FarmerRegisterDTO dto, User loggedUser) {
        if (loggedUser == null || (loggedUser.getRole() != Role.ADMIN && loggedUser.getRole() != Role.MANAGER && loggedUser.getRole() != Role.TECHNICIAN)) {
            throw new AccessDeniedException();
        }

        String normalizedCpf = CpfNormalizer.digitsOnly(dto.cpf());
        if (farmerRepository.existsByCpf(normalizedCpf)) {
            throw new CpfAlreadyExistsException();
        }

        Community community = null;
        if (communityId != null) {
            community = communityRepository.findById(communityId)
                    .orElseThrow(() -> new EntityNotFoundException("Comunidade não encontrada"));
        }

        if (loggedUser.getRole() == Role.MANAGER) {
            Manager manager = managerRepository.findByUserId(loggedUser.getId())
                    .orElseThrow(() -> new AccessDeniedException("Gestor não encontrado."));
            UUID managerOrgId = manager.getOrganization() != null ? manager.getOrganization().getId() : null;
            UUID communityOrgId = (community != null && community.getOrganization() != null)
                    ? community.getOrganization().getId()
                    : null;
            if (managerOrgId == null || communityOrgId == null || !managerOrgId.equals(communityOrgId)) {
                throw new AccessDeniedException("O gestor só tem acesso aos agricultores da sua própria organização.");
            }
        }

        if (loggedUser.getRole() == Role.TECHNICIAN) {
            if (community == null) {
                throw new AccessDeniedException("O técnico precisa vincular o agricultor a uma comunidade.");
            }
            Technician technician = technicianRepository.findByUserId(loggedUser.getId())
                    .orElseThrow(() -> new AccessDeniedException("Técnico não encontrado."));
            boolean hasAssistance = technicalAssistanceRepository
                    .findActiveByTechnicianAndCommunity(technician.getId(), community.getId())
                    .isPresent();
            if (!hasAssistance) {
                throw new AccessDeniedException("O técnico só pode cadastrar agricultores em comunidades sob sua assistência técnica.");
            }
        }

        User savedUser = null;
        if (dto.email() != null && !dto.email().isBlank() && dto.password() != null && !dto.password().isBlank()) {
            UserRegisterDTO userDto = new UserRegisterDTO(
                    dto.fullName(), dto.email(), dto.password(), normalizedCpf, Role.FARMER
            );
            savedUser = userRegisterUseCase.createBaseUser(userDto);
        }

        RegistrationSource source = (loggedUser.getRole() == Role.TECHNICIAN)
                ? RegistrationSource.TECHNICIAN
                : RegistrationSource.MANAGER;

        Farmer farmer = Farmer.builder()
                .user(savedUser)
                .community(community)
                .fullName(dto.fullName())
                .cpf(normalizedCpf)
                .dateOfBirth(dto.dateOfBirth())
                .motherName(dto.motherName())
                .origin(dto.origin())
                .educationLevel(dto.educationLevel())
                .phone(dto.phone())
                .localName(dto.localName())
                .street(dto.street())
                .city(dto.city())
                .state(dto.state())
                .ibgeCode(dto.ibgeCode())
                .latitude(dto.latitude())
                .longitude(dto.longitude())
                .registrationSource(source)
                .createdBy(loggedUser)
                .isCompliant(true)
                .build();

        Farmer saved = farmerRepository.save(farmer);

        Map<String, Object> details = new HashMap<>();
        details.put("fullName", saved.getFullName());
        if (saved.getUser() != null && saved.getUser().getId() != null) {
            details.put("userId", saved.getUser().getId().toString());
        }
        if (community != null && community.getId() != null) {
            details.put("communityId", community.getId().toString());
        }

        auditLogService.record(
                loggedUser,
                AuditAction.CREATE,
                "farmers",
                saved.getId(),
                details
        );

        return saved;
    }
}
