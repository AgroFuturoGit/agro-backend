package com.ufal.smartagro.adapters.in.web.controller;

import com.ufal.smartagro.adapters.in.web.dto.crop.CropRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.crop.CropResponseDTO;
import com.ufal.smartagro.adapters.in.web.mapper.Mapper;
import com.ufal.smartagro.application.service.crop.CropRegisterUseCase;
import com.ufal.smartagro.config.security.details.UserDetailsImpl;
import com.ufal.smartagro.domain.exception.UserNotFoundException;
import com.ufal.smartagro.domain.model.Crop;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/crop")
public class CropController {

    private final CropRegisterUseCase cropRegisterUseCase;
    private final UserRepository userRepository;

    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    @PostMapping("/register")
    public ResponseEntity<CropResponseDTO> register(@Valid @RequestBody CropRegisterDTO cropRegisterDTO,
                                                    @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        Crop crop = Mapper.toCrop(cropRegisterDTO);

        Crop savedCrop = cropRegisterUseCase.execute(crop, loggedUser);

        CropResponseDTO responseDTO = Mapper.toCropResponseDTO(savedCrop);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }
}
