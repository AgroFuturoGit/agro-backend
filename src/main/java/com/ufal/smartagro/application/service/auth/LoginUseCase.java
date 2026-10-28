package com.ufal.smartagro.application.service.auth;

import com.ufal.smartagro.adapters.in.web.dto.auth.LoginDTO;
import com.ufal.smartagro.adapters.in.web.dto.auth.LoginResponseDTO;
import com.ufal.smartagro.adapters.in.web.mapper.Mapper;
import com.ufal.smartagro.config.security.details.UserDetailsImpl;
import com.ufal.smartagro.config.security.service.JwtTokenService;
import com.ufal.smartagro.domain.exception.UserNotFoundException;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Slf4j
@RequiredArgsConstructor
@Service
public class LoginUseCase {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;

    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginDTO loginDTO) {
        String normalizedEmail = loginDTO.email() != null
                ? loginDTO.email().trim().toLowerCase(Locale.ROOT)
                : "";
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(
                        normalizedEmail,
                        loginDTO.password()
                );

        try {
            Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            User user = userRepository.findById(userDetails.getId())
                    .orElseThrow(UserNotFoundException::new);
            String token = jwtTokenService.generateToken(userDetails);

            log.info("Login efetuado com sucesso [userId={}, email='{}', role={}]", user.getId(), normalizedEmail, user.getRole());
            return Mapper.toLoginResponseDTO(token, user);
        } catch (AuthenticationException ex) {
            log.warn("Falha de autenticação para o email '{}': {}", normalizedEmail, ex.getMessage());
            throw ex;
        }
    }
}
