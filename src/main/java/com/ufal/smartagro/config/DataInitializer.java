package com.ufal.smartagro.config;

import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        String adminEmail = "admin@admin.com";
        
        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = new User(
                    null,
                    "Administrador Sistema",
                    adminEmail,
                    passwordEncoder.encode("12345678"),
                    "00000000000",
                    LocalDate.now(),
                    Role.ADMIN
            );
            userRepository.save(admin);
        }
    }
}
