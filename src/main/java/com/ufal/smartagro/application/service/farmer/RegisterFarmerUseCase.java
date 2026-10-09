package com.ufal.smartagro.application.service.farmer;

import com.ufal.smartagro.adapters.in.web.dto.farmer.FarmerRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.user.UserRegisterDTO;
import com.ufal.smartagro.application.service.user.UserRegisterUseCase;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.CpfAlreadyExistsException;
import com.ufal.smartagro.domain.model.Community;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.Manager;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.RegistrationSource;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.CommunityRepository;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
import com.ufal.smartagro.domain.port.out.ManagerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class RegisterFarmerUseCase {

    private final FarmerRepository farmerRepository;
    private final CommunityRepository communityRepository;
    private final ManagerRepository managerRepository;
    private final UserRegisterUseCase userRegisterUseCase;

    @Transactional
    public Farmer register(UUID communityId, FarmerRegisterDTO dto, User loggedUser) {
        if (loggedUser.getRole() != Role.ADMIN && loggedUser.getRole() != Role.MANAGER) {
            throw new AccessDeniedException();
        }

        if (farmerRepository.existsByCpf(dto.cpf())) {
            throw new CpfAlreadyExistsException();
        }

        Community community = null;
        if (communityId != null) {
            community = communityRepository.findById(communityId)
                    .orElseThrow(() -> new IllegalArgumentException("Comunidade não encontrada"));
        }

        if (loggedUser.getRole() == Role.MANAGER && community != null) {
            Manager manager = managerRepository.findByUserId(loggedUser.getId())
                    .orElseThrow(() -> new AccessDeniedException("Gestor não encontrado."));
            UUID managerOrgId = manager.getOrganization() != null ? manager.getOrganization().getId() : null;
            UUID communityOrgId = community.getOrganization() != null ? community.getOrganization().getId() : null;
            if (managerOrgId == null || communityOrgId == null || !managerOrgId.equals(communityOrgId)) {
                throw new AccessDeniedException("O gestor só tem acesso aos agricultores da sua própria organização.");
            }
        }

        User savedUser = null;
        if (dto.email() != null && !dto.email().isBlank() && dto.password() != null && !dto.password().isBlank()) {
            UserRegisterDTO userDto = new UserRegisterDTO(
                    dto.fullName(), dto.email(), dto.password(), dto.cpf(), Role.FARMER
            );
            savedUser = userRegisterUseCase.createBaseUser(userDto);
        }

        RegistrationSource source = (loggedUser.getRole() == Role.MANAGER)
                ? RegistrationSource.MANAGER
                : (loggedUser.getRole() == Role.TECHNICIAN ? RegistrationSource.TECHNICIAN : RegistrationSource.MANAGER);

        Farmer farmer = Farmer.builder()
                .user(savedUser)
                .community(community)
                .fullName(dto.fullName())
                .cpf(dto.cpf())
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

        return farmerRepository.save(farmer);
    }
}
