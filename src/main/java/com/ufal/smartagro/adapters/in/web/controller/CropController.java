package com.ufal.smartagro.adapters.in.web.controller;

import com.ufal.smartagro.adapters.in.web.dto.crop.CropRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.crop.CropResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.crop.CropUpdateDTO;
import com.ufal.smartagro.adapters.in.web.mapper.Mapper;
import com.ufal.smartagro.application.service.crop.CropFindAllUseCase;
import com.ufal.smartagro.application.service.crop.CropFindByIdUseCase;
import com.ufal.smartagro.application.service.crop.CropRegisterUseCase;
import com.ufal.smartagro.application.service.crop.CropUpdateUseCase;
import com.ufal.smartagro.application.service.crop.CropDeleteUseCase;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/crops")
public class CropController {

    private final CropRegisterUseCase cropRegisterUseCase;
    private final CropUpdateUseCase cropUpdateUseCase;
    private final CropFindByIdUseCase cropFindByIdUseCase;
    private final CropFindAllUseCase cropFindAllUseCase;
    private final CropDeleteUseCase cropDeleteUseCase;
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

    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    @PatchMapping("/{id}")
    public ResponseEntity<CropResponseDTO> update(@PathVariable UUID id,
                                                  @Valid @RequestBody CropUpdateDTO cropUpdateDTO,
                                                  @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {
        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        Crop cropToUpdate = Mapper.toCrop(cropUpdateDTO);

        Crop updatedCrop = cropUpdateUseCase.execute(id, cropToUpdate, loggedUser);

        CropResponseDTO responseDTO = Mapper.toCropResponseDTO(updatedCrop);

        return ResponseEntity.ok(responseDTO);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'MANAGER', 'FARMER')")
    @GetMapping("/{id}")
    public ResponseEntity<CropResponseDTO> findById(@PathVariable UUID id, @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {
        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);
        Crop crop = cropFindByIdUseCase.execute(id, loggedUser);
        CropResponseDTO responseDTO = Mapper.toCropResponseDTO(crop);
        return ResponseEntity.ok(responseDTO);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'MANAGER', 'FARMER')")
    @GetMapping
    public ResponseEntity<List<CropResponseDTO>> findAll(@AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {
        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);
        List<Crop> crops = cropFindAllUseCase.execute(loggedUser);
        List<CropResponseDTO> responseDTOs = crops.stream()
                .map(Mapper::toCropResponseDTO)
                .toList();
        return ResponseEntity.ok(responseDTOs);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {
        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);
        cropDeleteUseCase.execute(id, loggedUser);
        return ResponseEntity.noContent().build();
    }
}
