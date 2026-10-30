package com.ufal.smartagro.config;

import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    public static final String DEFAULT_ADMIN_PASSWORD = "12345678";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    @Value("${app.seed.admin.enabled:false}")
    private boolean seedAdminEnabled = false;

    @Value("${app.seed.admin.email:admin@admin.com}")
    private String adminEmail = "admin@admin.com";

    @Value("${app.seed.admin.password:12345678}")
    private String adminPassword = DEFAULT_ADMIN_PASSWORD;

    @Override
    public void run(String... args) {
        if (!seedAdminEnabled) {
            log.info("Seed de usuário administrador desabilitado.");
            return;
        }

        boolean isDevOrTest = environment != null && environment.acceptsProfiles(Profiles.of("dev", "test"));
        if (!isDevOrTest && DEFAULT_ADMIN_PASSWORD.equals(adminPassword)) {
            throw new IllegalStateException(
                    "Segurança: app.seed.admin.enabled está ativo fora dos profiles dev/test, mas a senha configurada é a senha fraca padrão ("
                            + DEFAULT_ADMIN_PASSWORD + "). Altere a propriedade app.seed.admin.password para um valor forte ou desative o seed."
            );
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
            log.info("Usuário administrador inicial criado com sucesso: {} (seed habilitado)", adminEmail);
        } else {
            log.debug("Usuário administrador {} já existe no banco.", adminEmail);
        }
    }
}
