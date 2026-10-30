package com.ufal.smartagro.application.service.farmer;

import com.ufal.smartagro.adapters.in.web.dto.farmer.FarmerUpdateDTO;
import com.ufal.smartagro.application.service.production.ProductionAccessValidator;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
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
@DisplayName("UpdateFarmerUseCase — limpeza de campos opcionais e validações")
class UpdateFarmerUseCaseTest {

    @Mock
    private FarmerRepository farmerRepository;

    @Mock
    private ProductionAccessValidator accessValidator;

    private UpdateFarmerUseCase useCase;

    private static final UUID FARMER_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new UpdateFarmerUseCase(farmerRepository, accessValidator);
    }

    private Farmer existingFarmer() {
        return Farmer.builder()
                .id(FARMER_ID)
                .fullName("João da Silva")
                .cpf("12345678901")
                .localName("Sítio Boa Esperança")
                .phone("82999998888")
                .build();
    }

    @Test
    @DisplayName("Limpeza de campos opcionais: string vazia redefine localName e phone para null")
    void givenEmptyStringsForOptionalFields_whenUpdate_thenClearsToNull() {
        Farmer existing = existingFarmer();
        when(farmerRepository.findById(FARMER_ID)).thenReturn(Optional.of(existing));
        when(farmerRepository.save(any(Farmer.class))).thenAnswer(i -> i.getArgument(0));

        FarmerUpdateDTO dto = new FarmerUpdateDTO(
                null, null, null, null, null,
                "",   // phone vazio
                "   ", // localName apenas espaços
                null, null, null, null, null, null, null
        );

        Farmer updated = useCase.update(FARMER_ID, dto);

        assertThat(updated.getLocalName()).isNull();
        assertThat(updated.getPhone()).isNull();
        assertThat(updated.getFullName()).isEqualTo("João da Silva");

        ArgumentCaptor<Farmer> captor = ArgumentCaptor.forClass(Farmer.class);
        verify(farmerRepository).save(captor.capture());
        assertThat(captor.getValue().getLocalName()).isNull();
        assertThat(captor.getValue().getPhone()).isNull();
    }

    @Test
    @DisplayName("Nome completo vazio ou apenas com espaços lança IllegalArgumentException")
    void givenBlankFullName_whenUpdate_thenThrowsIllegalArgumentException() {
        Farmer existing = existingFarmer();
        when(farmerRepository.findById(FARMER_ID)).thenReturn(Optional.of(existing));

        FarmerUpdateDTO dto = new FarmerUpdateDTO(
                "   ", null, null, null, null, null, null, null, null, null, null, null, null, null
        );

        assertThatThrownBy(() -> useCase.update(FARMER_ID, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O nome completo não pode ser vazio.");

        verify(farmerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Agricultor inexistente lança EntityNotFoundException")
    void givenNonExistentId_whenUpdate_thenThrowsEntityNotFoundException() {
        when(farmerRepository.findById(FARMER_ID)).thenReturn(Optional.empty());

        FarmerUpdateDTO dto = new FarmerUpdateDTO("Novo Apelido");

        assertThatThrownBy(() -> useCase.update(FARMER_ID, dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Agricultor não encontrado.");
    }
}
