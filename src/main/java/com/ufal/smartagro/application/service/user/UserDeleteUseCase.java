package com.ufal.smartagro.application.service.user;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.UserNotFoundException;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserDeleteUseCase {

    private final UserRepository userRepository;

    public void delete(Long userId, User loggedUser) {
        if (loggedUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException();
        }

        userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        userRepository.deleteById(userId);
    }
}
