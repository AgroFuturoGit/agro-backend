package com.ufal.smartagro.application.service.manager;

import com.ufal.smartagro.adapters.in.web.dto.manager.ManagerRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.manager.ManagerResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.organization.OrganizationResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.user.UserRegisterDTO;
import com.ufal.smartagro.adapters.in.web.mapper.Mapper;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.model.Manager;
import com.ufal.smartagro.domain.model.Organization;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.ManagerRepository;
import com.ufal.smartagro.domain.port.out.OrganizationRepository;
import com.ufal.smartagro.application.service.user.UserRegisterUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class RegisterManagerUseCase {

    private final ManagerRepository managerRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRegisterUseCase userRegisterUseCase;

    @Transactional
    public ManagerResponseDTO register(UUID organizationId, ManagerRegisterDTO dto, User loggedUser) {
        if (loggedUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException();
        }

        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Organização não encontrada"));

        UserRegisterDTO userDto = new UserRegisterDTO(
                dto.fullName(), dto.email(), dto.password(), dto.cpf(), dto.dateOfBirth(), Role.MANAGER
        );
        User savedUser = userRegisterUseCase.createBaseUser(userDto);

        Manager manager = new Manager(
                null,
                savedUser,
                organization,
                null,
                null,
                null
        );

        Manager savedManager = managerRepository.save(manager);

        return new ManagerResponseDTO(
                savedManager.getId(),
                Mapper.toUserResponseDTO(savedUser),
                new OrganizationResponseDTO(
                        organization.getId(),
                        organization.getName(),
                        organization.getTaxId(),
                        organization.getType(),
                        organization.getCreatedAt(),
                        organization.getUpdatedAt()
                ),
                savedManager.getCreatedAt(),
                savedManager.getUpdatedAt()
        );
    }
}
