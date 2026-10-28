package com.ufal.smartagro.adapters.out.persistence.adapter;

import com.ufal.smartagro.adapters.out.persistence.entity.UserEntity;
import com.ufal.smartagro.adapters.out.persistence.mapper.UserMapper;
import com.ufal.smartagro.adapters.out.persistence.repository.JpaUserRepository;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.UserRepository;
import com.ufal.smartagro.domain.util.CpfNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final JpaUserRepository jpaUserRepository;
    private final UserMapper userMapper;

    @Override
    public User save(User user) {
        UserEntity entity = userMapper.toEntity(user);
        if (entity.getEmail() != null) {
            entity.setEmail(entity.getEmail().trim().toLowerCase(Locale.ROOT));
        }
        if (entity.getCpf() != null) {
            entity.setCpf(CpfNormalizer.digitsOnly(entity.getCpf()));
        }
        UserEntity savedEntity = jpaUserRepository.save(entity);
        return userMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaUserRepository.findById(id)
                .map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String normalizedEmail = email != null ? email.trim().toLowerCase(Locale.ROOT) : null;
        return jpaUserRepository.findByEmail(normalizedEmail)
                .map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByCpf(String cpf) {
        return jpaUserRepository.findByCpf(CpfNormalizer.digitsOnly(cpf))
                .map(userMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        String normalizedEmail = email != null ? email.trim().toLowerCase(Locale.ROOT) : null;
        return jpaUserRepository.existsByEmail(normalizedEmail);
    }

    @Override
    public boolean existsByCpf(String cpf) {
        return jpaUserRepository.existsByCpf(CpfNormalizer.digitsOnly(cpf));
    }

    @Override
    public void softDelete(UUID id) {
        jpaUserRepository.softDeleteById(id);
    }

    @Override
    public List<User> findAll() {
        return jpaUserRepository.findAll()
                .stream()
                .map(userMapper::toDomain)
                .collect(Collectors.toList());
    }
}
