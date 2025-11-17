package com.fighthub.controller;

import com.fighthub.dto.presenca.PresencaRequest;
import com.fighthub.service.PresencaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PresencaController {

    private final PresencaService presencaService;

    @PostMapping("/aulas/{idAula}/presencas")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<Void> registrarPresenca(
            @PathVariable UUID idAula,
            @RequestBody @Valid PresencaRequest request,
            HttpServletRequest httpServletRequest) {
        presencaService.registrarPresenca(idAula, request, httpServletRequest);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
