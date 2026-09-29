package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.model.Community;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.Manager;
import com.ufal.smartagro.domain.model.Organization;
import com.ufal.smartagro.domain.model.Technician;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.ManagerRepository;
import com.ufal.smartagro.domain.port.out.TechnicalAssistanceRepository;
import com.ufal.smartagro.domain.port.out.TechnicianRepository;
import com.ufal.smartagro.testsupport.UserTestFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductionAccessValidatorTest {

    @Mock
    private ManagerRepository managerRepository;

    @Mock
    private TechnicianRepository technicianRepository;

    @Mock
    private TechnicalAssistanceRepository technicalAssistanceRepository;

    @InjectMocks
    private ProductionAccessValidator productionAccessValidator;

    @Nested
    @DisplayName("Farmer nulo — nenhum papel é bloqueado")
    class NullFarmer {

        @ParameterizedTest(name = "Given Role.{0} e farmer null When validateAccess Then não lança")
        @EnumSource(Role.class)
        void givenAnyRole_whenFarmerIsNull_thenAllows(Role role) {
            User loggedUser = UserTestFactory.user().role(role).build();

            assertThatCode(() -> productionAccessValidator.validateAccess(null, loggedUser))
                    .doesNotThrowAnyException();

            verifyNoInteractions(managerRepository);
        }
    }

    @Nested
    @DisplayName("ADMIN e TECHNICIAN — leitura ampliada sem checagem de tenant")
    class AdminAndTechnician {

        @ParameterizedTest(name = "Given Role.{0} When acessa farmer de outra org Then permite")
        @EnumSource(value = Role.class, names = {"ADMIN", "TECHNICIAN"})
        void givenAdminOrTechnician_whenAccessAnyFarmer_thenAllows(Role role) {
            User loggedUser = UserTestFactory.user().role(role).build();
            Farmer farmer = UserTestFactory.farmerProfile().build();

            assertThatCode(() -> productionAccessValidator.validateAccess(farmer, loggedUser))
                    .doesNotThrowAnyException();

            verifyNoInteractions(managerRepository);
        }
    }

    @Nested
    @DisplayName("FARMER (PRODUCER) — somente os próprios dados")
    class FarmerScope {

        @Test
        @DisplayName("Given FARMER When acessa o próprio perfil Then permite")
        void givenFarmer_whenAccessOwnProduction_thenAllows() {
            User farmerUser = UserTestFactory.user().farmer().build();
            Farmer ownFarmer = UserTestFactory.farmerProfile().user(farmerUser).build();

            assertThatCode(() -> productionAccessValidator.validateAccess(ownFarmer, farmerUser))
                    .doesNotThrowAnyException();

            verifyNoInteractions(managerRepository);
        }

        @Test
        @DisplayName("Given FARMER When acessa dados de terceiro Then AccessDeniedException")
        void givenFarmer_whenAccessThirdPartyProduction_thenAccessDenied() {
            User loggedFarmer = UserTestFactory.user().farmer().build();
            User otherUser = UserTestFactory.user().farmer().email("outro@smartagro.test").build();
            Farmer otherFarmer = UserTestFactory.farmerProfile().user(otherUser).build();

            assertThatThrownBy(() -> productionAccessValidator.validateAccess(otherFarmer, loggedFarmer))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("O agricultor só tem acesso aos seus próprios dados de produção.");

            verifyNoInteractions(managerRepository);
        }

        @Test
        @DisplayName("Given FARMER When farmer.user é null Then AccessDeniedException")
        void givenFarmer_whenTargetUserIsNull_thenAccessDenied() {
            User loggedFarmer = UserTestFactory.user().farmer().build();
            Farmer orphanFarmer = new Farmer(
                    UUID.randomUUID(),
                    null,
                    UserTestFactory.community().build(),
                    "Alias",
                    true,
                    null,
                    null,
                    null
            );

            assertThatThrownBy(() -> productionAccessValidator.validateAccess(orphanFarmer, loggedFarmer))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("O agricultor só tem acesso aos seus próprios dados de produção.");
        }
    }

    @Nested
    @DisplayName("MANAGER — apenas agricultores da própria Organization")
    class ManagerTenant {

        @Test
        @DisplayName("Given MANAGER When farmer da mesma org Then permite")
        void givenManager_whenFarmerInSameOrganization_thenAllows() {
            Organization organization = UserTestFactory.organization().build();
            Community community = UserTestFactory.community().organization(organization).build();
            User managerUser = UserTestFactory.user().manager().build();
            Manager manager = UserTestFactory.managerProfile()
                    .user(managerUser)
                    .organization(organization)
                    .build();
            Farmer farmer = UserTestFactory.farmerProfile().community(community).build();

            when(managerRepository.findByUserId(managerUser.getId())).thenReturn(Optional.of(manager));

            assertThatCode(() -> productionAccessValidator.validateAccess(farmer, managerUser))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Given MANAGER When farmer de outra org Then AccessDeniedException")
        void givenManager_whenFarmerInAnotherOrganization_thenAccessDenied() {
            Organization managerOrg = UserTestFactory.organization().name("Org Gestor").build();
            Organization otherOrg = UserTestFactory.organization().name("Org Alheia").build();
            Community otherCommunity = UserTestFactory.community().organization(otherOrg).build();
            User managerUser = UserTestFactory.user().manager().build();
            Manager manager = UserTestFactory.managerProfile()
                    .user(managerUser)
                    .organization(managerOrg)
                    .build();
            Farmer farmer = UserTestFactory.farmerProfile().community(otherCommunity).build();

            when(managerRepository.findByUserId(managerUser.getId())).thenReturn(Optional.of(manager));

            assertThatThrownBy(() -> productionAccessValidator.validateAccess(farmer, managerUser))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("O gestor só tem acesso aos agricultores da sua própria organização.");
        }

        @Test
        @DisplayName("Given MANAGER When perfil de gestor não existe Then AccessDeniedException")
        void givenManager_whenManagerProfileMissing_thenAccessDenied() {
            User managerUser = UserTestFactory.user().manager().build();
            Farmer farmer = UserTestFactory.farmerProfile().build();
            when(managerRepository.findByUserId(managerUser.getId())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productionAccessValidator.validateAccess(farmer, managerUser))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Gestor não encontrado.");
        }

        @Test
        @DisplayName("Given MANAGER When organization do gestor é null Then AccessDeniedException")
        void givenManager_whenManagerOrganizationIsNull_thenAccessDenied() {
            User managerUser = UserTestFactory.user().manager().build();
            Manager managerWithoutOrg = new Manager(
                    UUID.randomUUID(),
                    managerUser,
                    null,
                    null,
                    null,
                    null
            );
            Farmer farmer = UserTestFactory.farmerProfile().build();
            when(managerRepository.findByUserId(managerUser.getId())).thenReturn(Optional.of(managerWithoutOrg));

            assertThatThrownBy(() -> productionAccessValidator.validateAccess(farmer, managerUser))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("O gestor só tem acesso aos agricultores da sua própria organização.");
        }

        @Test
        @DisplayName("Given MANAGER When farmer sem community/org Then AccessDeniedException")
        void givenManager_whenFarmerHasNoOrganization_thenAccessDenied() {
            Organization organization = UserTestFactory.organization().build();
            User managerUser = UserTestFactory.user().manager().build();
            Manager manager = UserTestFactory.managerProfile()
                    .user(managerUser)
                    .organization(organization)
                    .build();
            Farmer farmerWithoutCommunity = new Farmer(
                    UUID.randomUUID(),
                    UserTestFactory.user().farmer().build(),
                    null,
                    "Alias",
                    true,
                    null,
                    null,
                    null
            );
            when(managerRepository.findByUserId(managerUser.getId())).thenReturn(Optional.of(manager));

            assertThatThrownBy(() -> productionAccessValidator.validateAccess(farmerWithoutCommunity, managerUser))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("O gestor só tem acesso aos agricultores da sua própria organização.");

            verify(managerRepository).findByUserId(managerUser.getId());
            verify(managerRepository, never()).save(manager);
        }
    }

    @Nested
    @DisplayName("Escrita — TECHNICIAN só com assistência técnica ativa")
    class WriteAccess {

        @Test
        @DisplayName("Given TECHNICIAN When assistência ativa Then validateWriteAccess permite")
        void givenTechnician_whenActiveAssistance_thenWriteAllowed() {
            User techUser = UserTestFactory.user().technician().build();
            Technician technician = UserTestFactory.technicianProfile().user(techUser).build();
            Farmer farmer = UserTestFactory.farmerProfile().build();
            when(technicianRepository.findByUserId(techUser.getId())).thenReturn(Optional.of(technician));
            when(technicalAssistanceRepository.findActiveByTechnicianAndFarmer(technician.getId(), farmer.getId()))
                    .thenReturn(Optional.of(UserTestFactory.technicalAssistance().technician(technician).farmer(farmer).build()));

            assertThatCode(() -> productionAccessValidator.validateWriteAccess(farmer, techUser))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Given TECHNICIAN When sem assistência Then validateWriteAccess nega")
        void givenTechnician_whenNoAssistance_thenWriteDenied() {
            User techUser = UserTestFactory.user().technician().build();
            Technician technician = UserTestFactory.technicianProfile().user(techUser).build();
            Farmer farmer = UserTestFactory.farmerProfile().build();
            when(technicianRepository.findByUserId(techUser.getId())).thenReturn(Optional.of(technician));
            when(technicalAssistanceRepository.findActiveByTechnicianAndFarmer(technician.getId(), farmer.getId()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> productionAccessValidator.validateWriteAccess(farmer, techUser))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage(ProductionAccessValidator.TECHNICIAN_WRITE_DENIED);
        }
    }
}
