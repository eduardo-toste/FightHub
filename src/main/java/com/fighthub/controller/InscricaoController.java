package com.fighthub.controller;

import com.fighthub.service.InscricaoService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
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
}
