package com.ufal.smartagro.config;

import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Environment environment;

    @InjectMocks
    private DataInitializer dataInitializer;

    @Test
    @DisplayName("Não deve executar seed quando app.seed.admin.enabled for false")
    void shouldNotSeedWhenDisabled() {
        ReflectionTestUtils.setField(dataInitializer, "seedAdminEnabled", false);

        dataInitializer.run();

        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("Deve criar administrador no ambiente dev quando não existir")
    void shouldSeedAdminInDevProfile() {
        ReflectionTestUtils.setField(dataInitializer, "seedAdminEnabled", true);
        ReflectionTestUtils.setField(dataInitializer, "adminEmail", "admin@admin.com");
        ReflectionTestUtils.setField(dataInitializer, "adminPassword", "12345678");

        when(environment.acceptsProfiles(any(Profiles.class))).thenReturn(true);
        when(userRepository.existsByEmail("admin@admin.com")).thenReturn(false);
        when(passwordEncoder.encode("12345678")).thenReturn("encoded-pwd");

        dataInitializer.run();

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar IllegalStateException em ambiente de produção se a senha for a padrão fraca")
    void shouldThrowExceptionInProdWithDefaultPassword() {
        ReflectionTestUtils.setField(dataInitializer, "seedAdminEnabled", true);
        ReflectionTestUtils.setField(dataInitializer, "adminEmail", "admin@prod.com");
        ReflectionTestUtils.setField(dataInitializer, "adminPassword", "12345678");

        when(environment.acceptsProfiles(any(Profiles.class))).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> dataInitializer.run());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deve criar administrador em ambiente de produção se a senha configurada for forte")
    void shouldSeedAdminInProdWithStrongPassword() {
        ReflectionTestUtils.setField(dataInitializer, "seedAdminEnabled", true);
        ReflectionTestUtils.setField(dataInitializer, "adminEmail", "admin@prod.com");
        ReflectionTestUtils.setField(dataInitializer, "adminPassword", "SuperStrongPassword@2026");

        when(environment.acceptsProfiles(any(Profiles.class))).thenReturn(false);
        when(userRepository.existsByEmail("admin@prod.com")).thenReturn(false);
        when(passwordEncoder.encode("SuperStrongPassword@2026")).thenReturn("encoded-strong");

        dataInitializer.run();

        verify(userRepository, times(1)).save(any(User.class));
    }
}
