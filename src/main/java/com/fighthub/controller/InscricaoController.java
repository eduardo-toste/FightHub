package com.fighthub.controller;

import com.fighthub.dto.inscricao.InscricaoResponse;
import com.fighthub.service.InscricaoService;
import jakarta.servlet.http.HttpServletRequest;
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
public class InscricaoController {

    private final InscricaoService inscricaoService;

    @PostMapping("/aulas/{idAula}/inscricoes")
    @PreAuthorize("hasRole('ALUNO')")
    public ResponseEntity<Void> inscreverAluno(@PathVariable UUID idAula, HttpServletRequest request) {
        inscricaoService.inscreverAluno(idAula, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/aulas/{idAula}/inscricoes")
    @PreAuthorize("hasRole('ALUNO')")
    public ResponseEntity<Void> cancelarInscricaoAluno(@PathVariable UUID idAula, HttpServletRequest request) {
        inscricaoService.cancelarInscricao(idAula, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/aulas/{idAula}/inscricoes")
    @PreAuthorize("hasRole('ADMIN', 'COORDENADOR', 'PROFESSOR')")
    public ResponseEntity<Page<InscricaoResponse>> buscarInscricoes(@PathVariable UUID idAula, Pageable pageable) {
        var inscricoes = inscricaoService.buscarInscricoesPorAula(idAula, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(inscricoes);
    }

    @DeleteMapping("/minhas-inscricoes")
    @PreAuthorize("hasRole('ALUNO')")
    public ResponseEntity<Page<InscricaoResponse>> buscarInscricoesProprias(HttpServletRequest request,  Pageable pageable) {
        var inscricoes = inscricaoService.buscarInscricoesProprias(request, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(inscricoes);
    }
}
