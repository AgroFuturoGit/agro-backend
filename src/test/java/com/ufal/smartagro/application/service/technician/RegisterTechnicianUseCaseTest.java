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

import com.ufal.smartagro.adapters.in.web.dto.user.UserRegisterDTO;
import com.ufal.smartagro.domain.model.Technician;
import com.ufal.smartagro.domain.model.enums.ProfessionalRegistrationType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterTechnicianUseCaseTest {

    @Mock
    private TechnicianRepository technicianRepository;
    @Mock
    private UserRegisterUseCase userRegisterUseCase;

    @InjectMocks
    private RegisterTechnicianUseCase registerTechnicianUseCase;

    @Test
    @DisplayName("Given ADMIN When register Then cadastra técnico com sucesso incluindo registrationType e createdBy")
    void givenAdmin_whenRegister_thenRegistersSuccessfully() {
        User admin = UserTestFactory.user().admin().build();
        User createdUser = UserTestFactory.user().technician().build();
        TechnicianRegisterDTO dto = new TechnicianRegisterDTO(
                "Técnico Teste", "tech@smartagro.test", UserTestFactory.RAW_PASSWORD,
                UserTestFactory.VALID_CPF, ProfessionalRegistrationType.CFT, "98765", "Solo"
        );

        when(userRegisterUseCase.createBaseUser(any(UserRegisterDTO.class))).thenReturn(createdUser);
        when(technicianRepository.save(any(Technician.class))).thenAnswer(inv -> inv.getArgument(0));

        Technician result = registerTechnicianUseCase.register(dto, admin);

        assertThat(result).isNotNull();
        assertThat(result.getUser()).isEqualTo(createdUser);
        assertThat(result.getRegistrationType()).isEqualTo(ProfessionalRegistrationType.CFT);
        assertThat(result.getRegistrationNumber()).isEqualTo("98765");
        assertThat(result.getSpecialty()).isEqualTo("Solo");
        assertThat(result.getCreatedBy()).isEqualTo(admin);
    }

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
