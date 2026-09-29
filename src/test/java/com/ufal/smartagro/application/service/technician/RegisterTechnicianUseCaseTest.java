package com.ufal.smartagro.application.service.technician;

import com.ufal.smartagro.adapters.in.web.dto.technician.TechnicianRegisterDTO;
import com.ufal.smartagro.application.service.user.UserRegisterUseCase;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.TechnicianRepository;
import com.ufal.smartagro.testsupport.UserTestFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class RegisterTechnicianUseCaseTest {

    @Mock
    private TechnicianRepository technicianRepository;
    @Mock
    private UserRegisterUseCase userRegisterUseCase;

    @InjectMocks
    private RegisterTechnicianUseCase registerTechnicianUseCase;

    @ParameterizedTest(name = "Given Role.{0} When register Then AccessDeniedException")
    @EnumSource(value = Role.class, names = {"MANAGER", "TECHNICIAN", "FARMER"})
    @DisplayName("Given não-ADMIN When register Then AccessDeniedException")
    void givenNonAdmin_whenRegister_thenAccessDenied(Role role) {
        User actor = UserTestFactory.user().role(role).build();
        TechnicianRegisterDTO dto = new TechnicianRegisterDTO(
                "Técnico", "tech@smartagro.test", UserTestFactory.RAW_PASSWORD,
                UserTestFactory.VALID_CPF, LocalDate.of(1988, 5, 5), "CREA-1", "Solo");

        assertThatThrownBy(() -> registerTechnicianUseCase.register(dto, actor))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Acesso negado. Somente administradores podem realizar esta ação.");

        verifyNoInteractions(userRegisterUseCase, technicianRepository);
    }
}
