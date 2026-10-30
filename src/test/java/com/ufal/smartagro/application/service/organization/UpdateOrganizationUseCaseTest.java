package com.ufal.smartagro.application.service.organization;

import com.ufal.smartagro.adapters.in.web.dto.organization.OrganizationUpdateDTO;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.Organization;
import com.ufal.smartagro.domain.model.enums.OrganizationType;
import com.ufal.smartagro.domain.port.out.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateOrganizationUseCase — validações e normalização")
class UpdateOrganizationUseCaseTest {

    @Mock
    private OrganizationRepository organizationRepository;

    private UpdateOrganizationUseCase useCase;

    private static final UUID ORG_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new UpdateOrganizationUseCase(organizationRepository);
    }

    @Test
    @DisplayName("Atualização bem-sucedida com trim no nome")
    void givenValidName_whenUpdate_thenTrimsAndSaves() {
        Organization existing = new Organization(ORG_ID, "Cooperativa Original", "12345678000199", OrganizationType.COOP, null, null, null);
        when(organizationRepository.findById(ORG_ID)).thenReturn(Optional.of(existing));
        when(organizationRepository.save(any(Organization.class))).thenAnswer(i -> i.getArgument(0));

        OrganizationUpdateDTO dto = new OrganizationUpdateDTO("  Cooperativa Nova  ", OrganizationType.ASSOC);

        Organization updated = useCase.update(ORG_ID, dto);

        assertThat(updated.getName()).isEqualTo("Cooperativa Nova");
        assertThat(updated.getType()).isEqualTo(OrganizationType.ASSOC);

        ArgumentCaptor<Organization> captor = ArgumentCaptor.forClass(Organization.class);
        verify(organizationRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Cooperativa Nova");
    }

    @Test
    @DisplayName("Nome contendo apenas espaços lança IllegalArgumentException")
    void givenBlankName_whenUpdate_thenThrowsIllegalArgumentException() {
        Organization existing = new Organization(ORG_ID, "Cooperativa Original", "12345678000199", OrganizationType.COOP, null, null, null);
        when(organizationRepository.findById(ORG_ID)).thenReturn(Optional.of(existing));

        OrganizationUpdateDTO dto = new OrganizationUpdateDTO("   ", null);

        assertThatThrownBy(() -> useCase.update(ORG_ID, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O nome da organização não pode ser vazio.");

        verify(organizationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Organização inexistente lança EntityNotFoundException")
    void givenNonExistentId_whenUpdate_thenThrowsEntityNotFoundException() {
        when(organizationRepository.findById(ORG_ID)).thenReturn(Optional.empty());

        OrganizationUpdateDTO dto = new OrganizationUpdateDTO("Novo Nome", null);

        assertThatThrownBy(() -> useCase.update(ORG_ID, dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Organização não encontrada.");
    }
}
