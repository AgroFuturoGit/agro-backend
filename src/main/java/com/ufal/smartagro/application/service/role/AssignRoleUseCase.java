package com.ufal.smartagro.application.service.role;

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

@Service
@RequiredArgsConstructor
public class AssignRoleUseCase {

    private final UserRepository userRepository;

    @Transactional
    public UserResponseDTO assignRole(Long userId, Role newRole, User loggedUser) {
        if (loggedUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException();
        }

        User user = userRepository.findById(userId)
            .orElseThrow(UserNotFoundException::new);

        User updated = new User(
            user.getId(), user.getFullName(), user.getEmail(),
            user.getPassword(), user.getCpf(), user.getDateOfBirth(),
            newRole
        );

        return Mapper.toUserResponseDTO(userRepository.save(updated));
    }
}
