package com.ufal.smartagro.config;

import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin.enabled:true}")
    private boolean seedAdminEnabled = true;

    @Value("${app.seed.admin.email:admin@admin.com}")
    private String adminEmail = "admin@admin.com";

    @Value("${app.seed.admin.password:12345678}")
    private String adminPassword = "12345678";

    @Override
    public void run(String... args) {
        if (!seedAdminEnabled) {
            return;
        }

        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = new User(
                    null,
                    "Administrador Sistema",
                    adminEmail,
                    passwordEncoder.encode(adminPassword),
                    "00000000000",
                    Role.ADMIN
            );
            userRepository.save(admin);
            log.info("Usuário administrador inicial criado: {}", adminEmail);
        }
    }
}
