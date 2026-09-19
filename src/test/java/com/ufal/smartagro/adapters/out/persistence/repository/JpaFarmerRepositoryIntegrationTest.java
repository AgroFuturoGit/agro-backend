package com.ufal.smartagro.adapters.out.persistence.repository;

import com.ufal.smartagro.adapters.out.persistence.entity.CommunityEntity;
import com.ufal.smartagro.adapters.out.persistence.entity.FarmerEntity;
import com.ufal.smartagro.adapters.out.persistence.entity.OrganizationEntity;
import com.ufal.smartagro.adapters.out.persistence.entity.UserEntity;
import com.ufal.smartagro.config.security.persistence.JpaAuditingConfig;
import com.ufal.smartagro.domain.model.enums.OrganizationType;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.support.AbstractIntegrationTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfig.class)
class JpaFarmerRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private JpaFarmerRepository farmerRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void savesAndFindsFarmerByUserId() {
        CommunityEntity community = persistCommunity();
        UserEntity user = persistUser("farmer.one@smartagro.test", "11111111111");
        FarmerEntity farmer = newFarmer(user, community, "Produtor Um");

        FarmerEntity saved = farmerRepository.saveAndFlush(farmer);
        entityManager.clear();

        FarmerEntity found = farmerRepository.findByUserId(user.getId()).orElseThrow();

        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getAliasName()).isEqualTo("Produtor Um");
        assertThat(found.getIsCompliant()).isTrue();
        assertThat(found.getCreatedAt()).isNotNull();
    }

    @Test
    void findsFarmersByCommunityAndExcludesSoftDeletedFarmers() {
        CommunityEntity community = persistCommunity();
        FarmerEntity activeFarmer = farmerRepository.saveAndFlush(newFarmer(
                persistUser("farmer.two@smartagro.test", "22222222222"), community, "Ativo"));
        FarmerEntity deletedFarmer = farmerRepository.saveAndFlush(newFarmer(
                persistUser("farmer.three@smartagro.test", "33333333333"), community, "Excluído"));

        farmerRepository.softDeleteById(deletedFarmer.getId());
        entityManager.flush();
        entityManager.clear();

        List<FarmerEntity> farmers = farmerRepository.findAllByCommunityId(community.getId());

        assertThat(farmers).extracting(FarmerEntity::getId).containsExactly(activeFarmer.getId());
        assertThat(farmerRepository.findById(deletedFarmer.getId())).isEmpty();
    }

    private CommunityEntity persistCommunity() {
        OrganizationEntity organization = new OrganizationEntity();
        organization.setName("Cooperativa Teste");
        organization.setTaxId(UUID.randomUUID().toString());
        organization.setType(OrganizationType.COOP);
        entityManager.persist(organization);

        CommunityEntity community = new CommunityEntity();
        community.setName("Comunidade Teste");
        community.setOrganization(organization);
        entityManager.persist(community);
        entityManager.flush();
        return community;
    }

    private UserEntity persistUser(String email, String cpf) {
        UserEntity user = new UserEntity();
        user.setFullName("Usuário de Teste");
        user.setEmail(email);
        user.setPassword("senha");
        user.setCpf(cpf);
        user.setDateOfBirth(LocalDate.of(1990, 1, 1));
        user.setRole(Role.FARMER);
        entityManager.persist(user);
        entityManager.flush();
        return user;
    }

    private FarmerEntity newFarmer(UserEntity user, CommunityEntity community, String aliasName) {
        FarmerEntity farmer = new FarmerEntity();
        farmer.setUser(user);
        farmer.setCommunity(community);
        farmer.setAliasName(aliasName);
        farmer.setIsCompliant(true);
        return farmer;
    }
}
