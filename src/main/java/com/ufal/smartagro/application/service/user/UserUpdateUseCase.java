package com.ufal.smartagro.application.service.user;

import com.ufal.smartagro.adapters.in.web.dto.user.UserResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.user.UserUpdateDTO;
import com.ufal.smartagro.adapters.in.web.mapper.Mapper;
import com.ufal.smartagro.domain.exception.UserNotFoundException;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserUpdateUseCase {

    private final UserRepository userRepository;

    public UserResponseDTO update(UserUpdateDTO dto, User loggedUser) {
        User existingUser = userRepository.findById(loggedUser.getId())
                .orElseThrow(UserNotFoundException::new);

        User updatedUser = new User(
                existingUser.getId(),
                dto.fullName(),
                existingUser.getEmail(),
                existingUser.getPassword(),
                existingUser.getCpf(),
                dto.dateOfBirth(),
                existingUser.getRole()
        );

        User savedUser = userRepository.save(updatedUser);
        return Mapper.toUserResponseDTO(savedUser);
    }
}
