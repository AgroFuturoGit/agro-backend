package com.ufal.smartagro.application.service.technician;

import com.ufal.smartagro.adapters.in.web.dto.technician.TechnicianRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.user.UserRegisterDTO;
import com.ufal.smartagro.application.service.user.UserRegisterUseCase;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.model.Technician;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.ProfessionalRegistrationType;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.TechnicianRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class RegisterTechnicianUseCase {

    private final TechnicianRepository technicianRepository;
    private final UserRegisterUseCase userRegisterUseCase;

    @Transactional
    public Technician register(TechnicianRegisterDTO dto, User loggedUser) {
        if (loggedUser == null || loggedUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException();
        }

        UserRegisterDTO userDto = new UserRegisterDTO(
                dto.fullName(), dto.email(), dto.password(), dto.cpf(), Role.TECHNICIAN
        );
        User savedUser = userRegisterUseCase.createBaseUser(userDto);

        ProfessionalRegistrationType regType = dto.registrationType() != null 
                ? dto.registrationType() 
                : ProfessionalRegistrationType.CREA;
        String regNum = dto.registrationNumber() != null 
                ? dto.registrationNumber() 
                : dto.professionalId();

        Technician technician = Technician.builder()
                .user(savedUser)
                .registrationType(regType)
                .registrationNumber(regNum)
                .specialty(dto.specialty())
                .createdBy(loggedUser)
                .build();

        return technicianRepository.save(technician);
    }
}
