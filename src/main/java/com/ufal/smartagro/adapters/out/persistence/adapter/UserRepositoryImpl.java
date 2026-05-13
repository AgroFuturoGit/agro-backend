package com.ufal.smartagro.adapters.out.persistence.adapter;

import com.ufal.smartagro.adapters.out.persistence.entity.UserEntity;
import com.ufal.smartagro.adapters.out.persistence.mapper.UserMapper;
import com.ufal.smartagro.adapters.out.persistence.repository.JpaUserRepository;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final JpaUserRepository jpaUserRepository;
    private final UserMapper userMapper;

    @Override
    public User save(User user) {
        UserEntity entity = userMapper.toEntity(user);
        UserEntity savedEntity = jpaUserRepository.save(entity);
        return userMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpaUserRepository.findById(id)
                .map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaUserRepository.findByEmail(email)
                .map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByCpf(String cpf) {
        return jpaUserRepository.findByCpf(cpf)
                .map(userMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaUserRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByCpf(String cpf) {
        return jpaUserRepository.existsByCpf(cpf);
    }

    @Override
    public void softDelete(Long id) {
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
