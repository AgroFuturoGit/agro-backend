package com.ufal.smartagro.domain.port.out;

import com.ufal.smartagro.domain.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface UserRepository {
    User save(User user);
    List<User> findAll();
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(String email);
    Optional<User> findByCpf(String cpf);
    boolean existsByEmail(String email);
    boolean existsByCpf(String cpf);
    void softDelete(UUID id);
}
