package com.fighthub.controller;

import com.fighthub.dto.responsavel.CriarResponsavelRequest;
import com.fighthub.dto.responsavel.ResponsavelDetalhadoResponse;
import com.fighthub.dto.responsavel.ResponsavelResponse;
import com.fighthub.service.ResponsavelService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDENADOR', 'PROFESSOR')")
    public ResponseEntity<Page<ResponsavelResponse>> obterResponsaveis(Pageable pageable) {
        var responsaveis = responsavelService.obterTodosResponsaveis(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(responsaveis);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDENADOR', 'PROFESSOR')")
    public ResponseEntity<ResponsavelDetalhadoResponse> obterResponsavel(@PathVariable UUID id) {
        var responsavel = responsavelService.obterResponsavelPorId(id);
        return ResponseEntity.status(HttpStatus.OK).body(responsavel);
    }

}
