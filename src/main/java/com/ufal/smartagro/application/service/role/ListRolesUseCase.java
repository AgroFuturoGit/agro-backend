package com.ufal.smartagro.application.service.role;

import com.ufal.smartagro.adapters.in.web.dto.role.RoleResponseDTO;
import com.ufal.smartagro.domain.model.enums.Role;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class ListRolesUseCase {

    public List<RoleResponseDTO> listAll() {
        return Arrays.stream(Role.values())
            .map(role -> new RoleResponseDTO(role.name(), role.getDescription()))
            .toList();
    }
}
