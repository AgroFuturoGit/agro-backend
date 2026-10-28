package com.ufal.smartagro.application.service.user;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.UserNotFoundException;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserDeleteUseCase {

    private final UserRepository userRepository;

    @Transactional
    public void delete(UUID userId, User loggedUser) {
        if (loggedUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException();
        }

        if (loggedUser.getId() != null && loggedUser.getId().equals(userId)) {
            throw new IllegalArgumentException("O administrador não pode excluir a própria conta.");
        }

        userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        userRepository.softDelete(userId);
        log.info("Usuário excluído logicamente [targetUserId={}] pelo administrador [actorId={}]", userId, loggedUser.getId());
    }
}
