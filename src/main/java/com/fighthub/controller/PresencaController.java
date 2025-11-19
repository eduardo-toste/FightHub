package com.fighthub.controller;

import com.fighthub.dto.presenca.PresencaRequest;
import com.fighthub.dto.presenca.PresencaResponse;
import com.fighthub.service.PresencaService;
import jakarta.servlet.http.HttpServletRequest;
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
@RequiredArgsConstructor
public class PresencaController {

    private final PresencaService presencaService;

    @PatchMapping("/aulas/{idAula}/presencas/inscricao/{idInscricao}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<Void> atualizarStatusPresenca(
            @PathVariable UUID idAula,
            @PathVariable UUID idInscricao,
            @RequestBody @Valid PresencaRequest request,
            HttpServletRequest httpServletRequest) {
        presencaService.atualizarStatusPresencaPorInscricao(idAula, idInscricao, request, httpServletRequest);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/aulas/{idAula}/presencas")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<Page<PresencaResponse>> listarPresencasPorAula(
            @PathVariable UUID idAula,
            Pageable pageable,
            HttpServletRequest httpServletRequest) {
        var presencas = presencaService.listarPresencasPorAula(idAula, pageable, httpServletRequest);
        return ResponseEntity.status(HttpStatus.OK).body(presencas);
    }

    @GetMapping("/aulas/me/presencas")
    @PreAuthorize("hasAnyRole('ALUNO')")
    public ResponseEntity<Page<PresencaResponse>> listarMinhasPresencas(
            Pageable pageable,
            HttpServletRequest httpServletRequest) {
        var presencas = presencaService.listarMinhasPresencas(pageable, httpServletRequest);
        return ResponseEntity.status(HttpStatus.OK).body(presencas);
    }
}