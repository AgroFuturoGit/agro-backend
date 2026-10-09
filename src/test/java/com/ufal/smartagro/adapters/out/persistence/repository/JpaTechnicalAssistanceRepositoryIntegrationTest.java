package com.ufal.smartagro.adapters.out.persistence.repository;

import com.ufal.smartagro.adapters.out.persistence.entity.CommunityEntity;
import com.ufal.smartagro.adapters.out.persistence.entity.OrganizationEntity;
import com.ufal.smartagro.adapters.out.persistence.entity.TechnicalAssistanceEntity;
import com.ufal.smartagro.adapters.out.persistence.entity.TechnicianEntity;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfig.class)
class JpaTechnicalAssistanceRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private JpaTechnicalAssistanceRepository assistanceRepository;

    @Autowired
    private JpaTechnicianRepository technicianRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void savesAndFindsActiveTechnicalAssistanceByTechnicianAndCommunity() {
        CommunityEntity community = persistCommunity("Comunidade Norte");
        TechnicianEntity technician = persistTechnician("tech.one@smartagro.test", "11122233344");

        TechnicalAssistanceEntity assistance = new TechnicalAssistanceEntity();
        assistance.setTechnician(technician);
        assistance.setCommunity(community);
        assistance.setStartDate(LocalDateTime.of(2026, 1, 10, 8, 0));

        TechnicalAssistanceEntity saved = assistanceRepository.saveAndFlush(assistance);
        entityManager.clear();

        Optional<TechnicalAssistanceEntity> active = assistanceRepository
                .findByTechnicianIdAndCommunityIdAndEndDateIsNull(technician.getId(), community.getId());

        assertThat(active).isPresent();
        assertThat(active.get().getId()).isEqualTo(saved.getId());
        assertThat(active.get().getCommunity().getId()).isEqualTo(community.getId());
        assertThat(active.get().getTechnician().getId()).isEqualTo(technician.getId());
        assertThat(active.get().getEndDate()).isNull();
    }

    @Test
    void findsAssistancesByTechnicianAndCommunityAndExcludesSoftDeleted() {
        CommunityEntity community = persistCommunity("Comunidade Sul");
        TechnicianEntity technician = persistTechnician("tech.two@smartagro.test", "55566677788");

        TechnicalAssistanceEntity activeAssistance = new TechnicalAssistanceEntity();
        activeAssistance.setTechnician(technician);
        activeAssistance.setCommunity(community);
        activeAssistance.setStartDate(LocalDateTime.of(2026, 2, 1, 8, 0));
        activeAssistance = assistanceRepository.saveAndFlush(activeAssistance);

        TechnicalAssistanceEntity deletedAssistance = new TechnicalAssistanceEntity();
        deletedAssistance.setTechnician(technician);
        deletedAssistance.setCommunity(community);
        deletedAssistance.setStartDate(LocalDateTime.of(2026, 3, 1, 8, 0));
        deletedAssistance = assistanceRepository.saveAndFlush(deletedAssistance);

        assistanceRepository.softDeleteById(deletedAssistance.getId());
        entityManager.flush();
        entityManager.clear();

        List<TechnicalAssistanceEntity> byTechnician = assistanceRepository.findByTechnicianId(technician.getId());
        List<TechnicalAssistanceEntity> byCommunity = assistanceRepository.findByCommunityId(community.getId());

        assertThat(byTechnician).extracting(TechnicalAssistanceEntity::getId).containsExactly(activeAssistance.getId());
        assertThat(byCommunity).extracting(TechnicalAssistanceEntity::getId).containsExactly(activeAssistance.getId());
        assertThat(assistanceRepository.findById(deletedAssistance.getId())).isEmpty();
    }

    @Test
    void findsTechniciansByOrganizationViaCommunity() {
        OrganizationEntity org1 = persistOrganization("Org A", "12345678000101");
        OrganizationEntity org2 = persistOrganization("Org B", "12345678000102");

        CommunityEntity comm1 = persistCommunityUnderOrg("Comm 1", org1);
        CommunityEntity comm2 = persistCommunityUnderOrg("Comm 2", org2);

        TechnicianEntity tech1 = persistTechnician("tech.org1@smartagro.test", "99988877766");
        TechnicianEntity tech2 = persistTechnician("tech.org2@smartagro.test", "88877766655");

        TechnicalAssistanceEntity a1 = new TechnicalAssistanceEntity();
        a1.setTechnician(tech1);
        a1.setCommunity(comm1);
        a1.setStartDate(LocalDateTime.of(2026, 1, 1, 0, 0));
        assistanceRepository.saveAndFlush(a1);

        TechnicalAssistanceEntity a2 = new TechnicalAssistanceEntity();
        a2.setTechnician(tech2);
        a2.setCommunity(comm2);
        a2.setStartDate(LocalDateTime.of(2026, 1, 1, 0, 0));
        assistanceRepository.saveAndFlush(a2);

        entityManager.clear();

        List<TechnicianEntity> org1Technicians = technicianRepository.findAllByOrganizationId(org1.getId());
        assertThat(org1Technicians).extracting(TechnicianEntity::getId).containsExactly(tech1.getId());
        assertThat(technicianRepository.existsByIdAndOrganizationId(tech1.getId(), org1.getId())).isTrue();
        assertThat(technicianRepository.existsByIdAndOrganizationId(tech1.getId(), org2.getId())).isFalse();
    }

    private OrganizationEntity persistOrganization(String name, String taxId) {
        OrganizationEntity org = new OrganizationEntity();
        org.setName(name);
        org.setTaxId(taxId);
        org.setType(OrganizationType.COOP);
        entityManager.persist(org);
        entityManager.flush();
        return org;
    }

    private CommunityEntity persistCommunity(String name) {
        OrganizationEntity organization = persistOrganization("Org " + UUID.randomUUID(), UUID.randomUUID().toString());
        return persistCommunityUnderOrg(name, organization);
    }

    private CommunityEntity persistCommunityUnderOrg(String name, OrganizationEntity organization) {
        CommunityEntity community = new CommunityEntity();
        community.setName(name);
        community.setOrganization(organization);
        entityManager.persist(community);
        entityManager.flush();
        return community;
    }

    private TechnicianEntity persistTechnician(String email, String cpf) {
        UserEntity user = new UserEntity();
        user.setFullName("Técnico Teste");
        user.setEmail(email);
        user.setPassword("senha123");
        user.setCpf(cpf);
        user.setRole(Role.TECHNICIAN);
        entityManager.persist(user);

        TechnicianEntity technician = new TechnicianEntity();
        technician.setUser(user);
        technician.setProfessionalId("CREA-" + UUID.randomUUID().toString().substring(0, 6));
        technician.setSpecialty("Agronomia");
        entityManager.persist(technician);
        entityManager.flush();
        return technician;
    }
}
