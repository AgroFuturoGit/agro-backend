package com.ufal.smartagro.testsupport;

import com.ufal.smartagro.adapters.in.web.dto.farmer.FarmerRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.user.UserRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.user.UserUpdateDTO;
import com.ufal.smartagro.adapters.out.persistence.entity.UserEntity;
import com.ufal.smartagro.domain.model.Community;
import com.ufal.smartagro.domain.model.Crop;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.Harvest;
import com.ufal.smartagro.domain.model.Manager;
import com.ufal.smartagro.domain.model.Organization;
import com.ufal.smartagro.domain.model.ProductionPlan;
import com.ufal.smartagro.domain.model.TechnicalAssistance;
import com.ufal.smartagro.domain.model.Technician;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.OrganizationType;
import com.ufal.smartagro.domain.model.enums.Role;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Test Data Builder para instanciar agregados de identidade e tenant
 * (User, Organization, Community) sem contexto Spring.
 */
public final class  UserTestFactory {

    public static final String VALID_CPF = "52998224725";
    public static final String RAW_PASSWORD = "senha1234";
    public static final String ENCODED_PASSWORD = "$2a$10$encodedHash";

    private UserTestFactory() {
    }

    public static UserBuilder user() {
        return new UserBuilder();
    }

    public static OrganizationBuilder organization() {
        return new OrganizationBuilder();
    }

    public static CommunityBuilder community() {
        return new CommunityBuilder();
    }

    public static UserRegisterDTOBuilder registerDto() {
        return new UserRegisterDTOBuilder();
    }

    public static UserUpdateDTOBuilder updateDto() {
        return new UserUpdateDTOBuilder();
    }

    public static FarmerRegisterDTOBuilder farmerRegisterDto() {
        return new FarmerRegisterDTOBuilder();
    }

    public static FarmerBuilder farmerProfile() {
        return new FarmerBuilder();
    }

    public static ManagerBuilder managerProfile() {
        return new ManagerBuilder();
    }

    public static TechnicianBuilder technicianProfile() {
        return new TechnicianBuilder();
    }

    public static TechnicalAssistanceBuilder technicalAssistance() {
        return new TechnicalAssistanceBuilder();
    }

    public static ProductionPlanBuilder productionPlan() {
        return new ProductionPlanBuilder();
    }

    public static Crop crop() {
        return new Crop(UUID.randomUUID(), "Milho", "BRS", false);
    }

    public static Harvest harvest() {
        return new Harvest(UUID.randomUUID(), "Safra 2026", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30));
    }

    public static final class UserBuilder {
        private UUID id = UUID.randomUUID();
        private String fullName = "Usuário Teste";
        private String email = "user@smartagro.test";
        private String password = ENCODED_PASSWORD;
        private String cpf = VALID_CPF;
        private Role role = Role.FARMER;

        public UserBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public UserBuilder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public UserBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserBuilder password(String password) {
            this.password = password;
            return this;
        }

        public UserBuilder cpf(String cpf) {
            this.cpf = cpf;
            return this;
        }

        public UserBuilder role(Role role) {
            this.role = role;
            return this;
        }

        public UserBuilder admin() {
            return role(Role.ADMIN)
                    .fullName("Admin Sistema")
                    .email("admin@smartagro.test");
        }

        public UserBuilder manager() {
            return role(Role.MANAGER)
                    .fullName("Gestor Cooperativa")
                    .email("manager@smartagro.test");
        }

        public UserBuilder technician() {
            return role(Role.TECHNICIAN)
                    .fullName("Técnico Agrícola")
                    .email("technician@smartagro.test");
        }

        /**
         * Papel FARMER no domínio — equivalente ao PRODUCER do modelo RBAC de negócio.
         */
        public UserBuilder farmer() {
            return role(Role.FARMER)
                    .fullName("Agricultor Comunidade")
                    .email("farmer@smartagro.test");
        }

        public User build() {
            return new User(id, fullName, email, password, cpf, role);
        }

        public UserEntity toEntity() {
            UserEntity entity = new UserEntity();
            entity.setId(id);
            entity.setFullName(fullName);
            entity.setEmail(email);
            entity.setPassword(password);
            entity.setCpf(cpf);
            entity.setRole(role);
            return entity;
        }
    }

    public static final class OrganizationBuilder {
        private UUID id = UUID.randomUUID();
        private String name = "Cooperativa Norte";
        private String taxId = "12345678000199";
        private OrganizationType type = OrganizationType.COOP;
        private LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 0, 0);

        public OrganizationBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public OrganizationBuilder name(String name) {
            this.name = name;
            return this;
        }

        public OrganizationBuilder taxId(String taxId) {
            this.taxId = taxId;
            return this;
        }

        public OrganizationBuilder type(OrganizationType type) {
            this.type = type;
            return this;
        }

        public Organization build() {
            return new Organization(id, name, taxId, type, createdAt, null, null);
        }
    }

    public static final class CommunityBuilder {
        private UUID id = UUID.randomUUID();
        private String name = "Comunidade Rio Verde";
        private Organization organization = UserTestFactory.organization().build();

        public CommunityBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public CommunityBuilder name(String name) {
            this.name = name;
            return this;
        }

        public CommunityBuilder organization(Organization organization) {
            this.organization = organization;
            return this;
        }

        public Community build() {
            return new Community(id, name, organization, LocalDateTime.of(2024, 2, 1, 0, 0), null, null);
        }
    }

    public static final class UserRegisterDTOBuilder {
        private String fullName = "Novo Usuário";
        private String email = "novo@smartagro.test";
        private String password = RAW_PASSWORD;
        private String cpf = VALID_CPF;
        private Role role = Role.TECHNICIAN;

        public UserRegisterDTOBuilder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public UserRegisterDTOBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserRegisterDTOBuilder password(String password) {
            this.password = password;
            return this;
        }

        public UserRegisterDTOBuilder cpf(String cpf) {
            this.cpf = cpf;
            return this;
        }

        public UserRegisterDTOBuilder role(Role role) {
            this.role = role;
            return this;
        }

        public UserRegisterDTO build() {
            return new UserRegisterDTO(fullName, email, password, cpf, role);
        }
    }

    public static final class UserUpdateDTOBuilder {
        private String fullName = "Nome Atualizado";

        public UserUpdateDTOBuilder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public UserUpdateDTO build() {
            return new UserUpdateDTO(fullName);
        }
    }

    public static final class FarmerRegisterDTOBuilder {
        private String fullName = "João da Silva";
        private String email = "joao@smartagro.test";
        private String password = RAW_PASSWORD;
        private String cpf = VALID_CPF;
        private LocalDate dateOfBirth = LocalDate.of(1985, 3, 12);
        private String aliasName = "João";

        public FarmerRegisterDTOBuilder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public FarmerRegisterDTOBuilder email(String email) {
            this.email = email;
            return this;
        }

        public FarmerRegisterDTOBuilder aliasName(String aliasName) {
            this.aliasName = aliasName;
            return this;
        }

        public FarmerRegisterDTO build() {
            return new FarmerRegisterDTO(fullName, email, password, cpf, dateOfBirth, aliasName);
        }
    }

    public static final class FarmerBuilder {
        private UUID id = UUID.randomUUID();
        private User user = UserTestFactory.user().farmer().build();
        private Community community = UserTestFactory.community().build();
        private String aliasName = "Produtor";
        private Boolean isCompliant = true;

        public FarmerBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public FarmerBuilder user(User user) {
            this.user = user;
            return this;
        }

        public FarmerBuilder community(Community community) {
            this.community = community;
            return this;
        }

        public FarmerBuilder aliasName(String aliasName) {
            this.aliasName = aliasName;
            return this;
        }

        public Farmer build() {
            return Farmer.builder()
                    .id(id)
                    .user(user)
                    .community(community)
                    .fullName(user != null ? user.getFullName() : "Agricultor Teste")
                    .cpf(user != null ? user.getCpf() : VALID_CPF)
                    .localName(aliasName)
                    .isCompliant(isCompliant)
                    .registrationSource(com.ufal.smartagro.domain.model.enums.RegistrationSource.MANAGER)
                    .build();
        }
    }

    public static final class ManagerBuilder {
        private UUID id = UUID.randomUUID();
        private User user = UserTestFactory.user().manager().build();
        private Organization organization = UserTestFactory.organization().build();

        public ManagerBuilder user(User user) {
            this.user = user;
            return this;
        }

        public ManagerBuilder organization(Organization organization) {
            this.organization = organization;
            return this;
        }

        public Manager build() {
            return new Manager(id, user, organization, null, null, null);
        }
    }

    public static final class TechnicianBuilder {
        private UUID id = UUID.randomUUID();
        private User user = UserTestFactory.user().technician().build();
        private String professionalId = "CREA-123";
        private String specialty = "Solo";

        public TechnicianBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public TechnicianBuilder user(User user) {
            this.user = user;
            return this;
        }

        public Technician build() {
            return new Technician(id, user, professionalId, specialty, null, null, null);
        }
    }

    public static final class TechnicalAssistanceBuilder {
        private UUID id = UUID.randomUUID();
        private Technician technician = UserTestFactory.technicianProfile().build();
        private Community community = UserTestFactory.community().build();
        private LocalDateTime startDate = LocalDateTime.of(2026, 1, 10, 8, 0);
        private LocalDateTime endDate = null;

        public TechnicalAssistanceBuilder technician(Technician technician) {
            this.technician = technician;
            return this;
        }

        public TechnicalAssistanceBuilder community(Community community) {
            this.community = community;
            return this;
        }

        public TechnicalAssistanceBuilder farmer(Farmer farmer) {
            this.community = farmer != null ? farmer.getCommunity() : null;
            return this;
        }

        public TechnicalAssistanceBuilder ended() {
            this.endDate = LocalDateTime.of(2026, 2, 1, 8, 0);
            return this;
        }

        public TechnicalAssistance build() {
            return new TechnicalAssistance(id, technician, community, startDate, endDate, null, null, null);
        }
    }

    public static final class ProductionPlanBuilder {
        private UUID id = UUID.randomUUID();
        private Farmer farmer = UserTestFactory.farmerProfile().build();
        private Harvest harvest = UserTestFactory.harvest();
        private Crop crop = UserTestFactory.crop();
        private BigDecimal plantedArea = new BigDecimal("10.00");
        private BigDecimal expectedYield = new BigDecimal("100.00");

        public ProductionPlanBuilder farmer(Farmer farmer) {
            this.farmer = farmer;
            return this;
        }

        public ProductionPlanBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public ProductionPlan build() {
            return new ProductionPlan(
                    id,
                    farmer,
                    harvest,
                    crop,
                    plantedArea,
                    expectedYield,
                    LocalDate.of(2026, 3, 1),
                    null,
                    null,
                    null,
                    null
            );
        }
    }
}
