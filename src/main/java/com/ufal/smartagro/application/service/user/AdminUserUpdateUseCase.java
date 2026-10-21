package com.ufal.smartagro.application.service.user;

import com.ufal.smartagro.adapters.in.web.dto.user.AdminUserUpdateDTO;
import com.ufal.smartagro.adapters.in.web.dto.user.UserResponseDTO;
import com.ufal.smartagro.adapters.in.web.mapper.Mapper;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.UserNotFoundException;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class AdminUserUpdateUseCase {

    private final UserRepository userRepository;

    @Transactional
    public UserResponseDTO update(UUID userId, AdminUserUpdateDTO dto, User loggedUser) {
        if (loggedUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException();
        }

        User existingUser = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (dto.role() != existingUser.getRole() &&
                (dto.role() == Role.MANAGER || dto.role() == Role.FARMER || dto.role() == Role.TECHNICIAN)) {
            throw new IllegalArgumentException("Para criar ou promover gestores, produtores e técnicos, utilize as rotas específicas de perfil.");
        }

        User updatedUser = new User(
                existingUser.getId(),
                dto.fullName(),
                existingUser.getEmail(),
                existingUser.getPassword(),
                existingUser.getCpf(),
                dto.role()
        );

        User savedUser = userRepository.save(updatedUser);
        return Mapper.toUserResponseDTO(savedUser);
    }
}
