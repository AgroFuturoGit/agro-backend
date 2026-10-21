package com.ufal.smartagro.application.service.farmer.financing;

import com.ufal.smartagro.application.service.production.ProductionAccessValidator;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.Financing;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.FinancingRepository;
import com.ufal.smartagro.testsupport.UserTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteFinancingUseCaseTest {

    @Mock
    private FinancingRepository financingRepository;

    @Mock
    private ProductionAccessValidator accessValidator;

    private DeleteFinancingUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteFinancingUseCase(financingRepository, accessValidator);
    }

    @Test
    @DisplayName("Exclui financiamento com sucesso")
    void givenExistingId_whenDelete_thenDeletes() {
        User admin = UserTestFactory.user().admin().build();
        Farmer farmer = UserTestFactory.farmerProfile().build();
        UUID id = UUID.randomUUID();
        Financing existing = Financing.builder()
                .id(id)
                .farmer(farmer)
                .description("PRONAF")
                .build();

        when(financingRepository.findById(id)).thenReturn(Optional.of(existing));

        useCase.delete(id, admin);

        verify(accessValidator).validateWriteAccess(farmer, admin);
        verify(financingRepository).delete(id);
    }

    @Test
    @DisplayName("Lança IllegalArgumentException quando financiamento não existe")
    void givenNonExistingId_whenDelete_thenThrowsIllegalArgumentException() {
        User admin = UserTestFactory.user().admin().build();
        UUID id = UUID.randomUUID();

        when(financingRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.delete(id, admin))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Financiamento não encontrado.");

        verify(financingRepository, never()).delete(any());
    }
}
