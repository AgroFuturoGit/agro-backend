package com.ufal.smartagro.application.service.user;

import com.ufal.smartagro.adapters.in.web.dto.user.UserRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.user.UserResponseDTO;
import com.ufal.smartagro.adapters.in.web.mapper.Mapper;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.CpfAlreadyExistsException;
import com.ufal.smartagro.domain.exception.EmailAlreadyExistsException;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.UserRepository;
import com.ufal.smartagro.domain.util.CpfNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@RequiredArgsConstructor
@Service
public class UserRegisterUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponseDTO register(UserRegisterDTO dto, User loggedUser) {
        if (loggedUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException();
        }

        if (dto.role() == Role.MANAGER || dto.role() == Role.FARMER || dto.role() == Role.TECHNICIAN) {
            throw new IllegalArgumentException("Para criar ou promover gestores, produtores e técnicos, utilize as rotas específicas de perfil.");
        }

        User savedUser = createBaseUser(dto);
        return Mapper.toUserResponseDTO(savedUser);
    }

    @Transactional
    public User createBaseUser(UserRegisterDTO dto) {
        String normalizedEmail = dto.email() != null
                ? dto.email().trim().toLowerCase(Locale.ROOT)
                : null;
        if (normalizedEmail != null && userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException();
        }

        String normalizedCpf = CpfNormalizer.digitsOnly(dto.cpf());
        if (normalizedCpf != null && userRepository.existsByCpf(normalizedCpf)) {
            throw new CpfAlreadyExistsException();
        }

        String encodedPassword = passwordEncoder.encode(dto.password());

        User baseUser = Mapper.toUser(dto, encodedPassword);
        User newUser = new User(
                baseUser.getId(),
                baseUser.getFullName() != null ? baseUser.getFullName().trim() : null,
                normalizedEmail,
                baseUser.getPassword(),
                normalizedCpf,
                baseUser.getRole()
        );
        return userRepository.save(newUser);
    }
}
