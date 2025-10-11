package com.fighthub.controller;

import com.fighthub.dto.responsavel.CriarResponsavelRequest;
import com.fighthub.service.ResponsavelService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/responsaveis")
@RequiredArgsConstructor
@Tag(name = "Responsaveis", description = "Endpoints para criação e gerenciamento de Responsaveis no FightHub")
public class ResponsavelController {

    private final ResponsavelService responsavelService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDENADOR', 'PROFESSOR')")
    public ResponseEntity<Void> criarResponsavel(@RequestBody @Valid CriarResponsavelRequest request) {
        responsavelService.criacaoResponsavel(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
