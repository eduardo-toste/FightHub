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
    @PreAuthorize("hasAnyRole('ALUNO')")
    public ResponseEntity<Void> inscreverAluno(@PathVariable UUID idAula, HttpServletRequest request) {
        inscricaoService.inscricaoAluno(idAula, request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/aulas/{idAula}/inscricoes")
    @PreAuthorize("hasAnyRole('ALUNO')")
    public ResponseEntity<Void> desinscreverAluno(@PathVariable UUID idAula, HttpServletRequest request) {
        inscricaoService.desinscricaoAluno(idAula, request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
