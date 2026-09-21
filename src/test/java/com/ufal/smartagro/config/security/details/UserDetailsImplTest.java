package com.ufal.smartagro.config.security.details;

import com.ufal.smartagro.adapters.out.persistence.entity.UserEntity;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.testsupport.UserTestFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.security.core.GrantedAuthority;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserDetailsImplTest {

    @Nested
    @DisplayName("Given um UserEntity When UserDetailsImpl é criado")
    class IdentityMapping {

        @Test
        @DisplayName("Then username é o email e password é o hash persistido")
        void givenUserEntity_whenGetCredentials_thenMapsEmailAndPassword() {
            UserEntity entity = UserTestFactory.user()
                    .admin()
                    .email("admin@smartagro.test")
                    .password(UserTestFactory.ENCODED_PASSWORD)
                    .toEntity();

            UserDetailsImpl details = new UserDetailsImpl(entity);

            assertThat(details.getUsername()).isEqualTo("admin@smartagro.test");
            assertThat(details.getPassword()).isEqualTo(UserTestFactory.ENCODED_PASSWORD);
            assertThat(details.getName()).isEqualTo(entity.getFullName());
            assertThat(details.getId()).isEqualTo(entity.getId());
            assertThat(details.getUser()).isSameAs(entity);
        }

        @Test
        @DisplayName("Then a conta permanece habilitada (sem regras de lock/expiry)")
        void givenUserEntity_whenAccountFlags_thenAlwaysEnabled() {
            UserDetailsImpl details = new UserDetailsImpl(UserTestFactory.user().farmer().toEntity());

            assertThat(details.isAccountNonExpired()).isTrue();
            assertThat(details.isAccountNonLocked()).isTrue();
            assertThat(details.isCredentialsNonExpired()).isTrue();
            assertThat(details.isEnabled()).isTrue();
        }
    }

    @Nested
    @DisplayName("Given cada Role do RBAC When getAuthorities")
    class RoleAuthorities {

        @ParameterizedTest(name = "Then Role.{0} vira autoridade ROLE_{0}")
        @EnumSource(Role.class)
        void givenRole_whenGetAuthorities_thenPrefixedWithROLE(Role role) {
            UUID userId = UUID.randomUUID();
            UserEntity entity = UserTestFactory.user()
                    .id(userId)
                    .role(role)
                    .toEntity();

            UserDetailsImpl details = new UserDetailsImpl(entity);

            assertThat(details.getAuthorities())
                    .extracting(GrantedAuthority::getAuthority)
                    .containsExactly("ROLE_" + role.name());
        }
    }
}
