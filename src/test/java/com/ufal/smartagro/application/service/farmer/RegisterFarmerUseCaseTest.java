package com.ufal.smartagro.application.service.farmer;

import com.ufal.smartagro.adapters.in.web.dto.farmer.FarmerRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.user.UserRegisterDTO;
import com.ufal.smartagro.application.service.user.UserRegisterUseCase;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.model.Community;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.CommunityRepository;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterFarmerUseCaseTest {

    @Mock
    private FarmerRepository farmerRepository;

    @Mock
    private CommunityRepository communityRepository;

    @Mock
    private UserRegisterUseCase userRegisterUseCase;

    @InjectMocks
    private RegisterFarmerUseCase registerFarmerUseCase;

    private UUID communityId;
    private Community community;
    private FarmerRegisterDTO registerDto;
    private User savedUser;

    @BeforeEach
    void setUp() {
        communityId = UUID.randomUUID();
        community = new Community(communityId, "Comunidade Norte", null, null, null, null);
        registerDto = new FarmerRegisterDTO(
                "João da Silva",
                "joao@smartagro.test",
                "senha1234",
                "52998224725",
                LocalDate.of(1985, 3, 12),
                "João"
        );
        savedUser = new User(
                UUID.randomUUID(),
                registerDto.fullName(),
                registerDto.email(),
                "encoded-password",
                registerDto.cpf(),
                registerDto.dateOfBirth(),
                Role.FARMER
        );
    }

    @Test
    @DisplayName("deve cadastrar produtor quando o usuário logado é ADMIN")
    void shouldRegisterFarmerWhenLoggedUserIsAdmin() {
        // ARRANGE (Organizar/Preparar)
        User admin = loggedUser(Role.ADMIN);
        Farmer persisted = new Farmer(
                UUID.randomUUID(),
                savedUser,
                community,
                registerDto.aliasName(),
                true,
                null,
                null,
                null
        );

        //Quando o sistema procurar a comunidade pelo communityId, finja que encontrou a comunidade.
        when(communityRepository.findById(communityId)).thenReturn(Optional.of(community));

        //Quando o registerFarmerUseCase tentar criar o usuário base, não execute o cadastro real. Simplesmente devolva savedUser.
        when(userRegisterUseCase.createBaseUser(any(UserRegisterDTO.class))).thenReturn(savedUser);

        //Quando o código tentar salvar um Farmer, finja que o banco salvou e devolva persisted.
        when(farmerRepository.save(any(Farmer.class))).thenReturn(persisted);

        // ACT (Agir/Executar)
        //Executamos a função register para tetar se ela esta funcionando corretamente
        Farmer result = registerFarmerUseCase.register(communityId, registerDto, admin);

        // ASSERT (Afirmar/Verificar)
        //Verificando se o resultado foi o correto
        assertNotNull(result.getId());
        assertEquals(savedUser, result.getUser());
        assertEquals(community, result.getCommunity());
        assertEquals("João", result.getAliasName());
        assertTrue(result.getIsCompliant());

        //Testando se os metodos foram chamados corretamente
        ArgumentCaptor<UserRegisterDTO> userDtoCaptor = ArgumentCaptor.forClass(UserRegisterDTO.class);
        verify(userRegisterUseCase).createBaseUser(userDtoCaptor.capture());
        UserRegisterDTO capturedUserDto = userDtoCaptor.getValue();
        assertEquals(registerDto.fullName(), capturedUserDto.fullName());
        assertEquals(registerDto.email(), capturedUserDto.email());
        assertEquals(registerDto.password(), capturedUserDto.password());
        assertEquals(registerDto.cpf(), capturedUserDto.cpf());
        assertEquals(registerDto.dateOfBirth(), capturedUserDto.dateOfBirth());
        assertEquals(Role.FARMER, capturedUserDto.role());

        //Testando qual objeto farmer foi enviado para o repositorio
        ArgumentCaptor<Farmer> farmerCaptor = ArgumentCaptor.forClass(Farmer.class);
        verify(farmerRepository).save(farmerCaptor.capture());
        Farmer capturedFarmer = farmerCaptor.getValue();
        assertEquals(savedUser, capturedFarmer.getUser());
        assertEquals(community, capturedFarmer.getCommunity());
        assertEquals(registerDto.aliasName(), capturedFarmer.getAliasName());
        assertTrue(capturedFarmer.getIsCompliant());
    }

    private User loggedUser(Role role) {
        return new User(
                UUID.randomUUID(),
                "Usuário Logado",
                "logged@smartagro.test",
                "encoded",
                "39053344705",
                LocalDate.of(1980, 1, 1),
                role
        );
    }
}
