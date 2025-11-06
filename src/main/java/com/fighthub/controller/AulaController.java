package com.fighthub.controller;

import com.fighthub.dto.aula.AulaRequest;
import com.fighthub.dto.aula.AulaResponse;
import com.fighthub.service.AulaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/aulas")
@RequiredArgsConstructor
public class AulaController {

    private final AulaService aulaService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDENADOR', 'PROFESSOR')")
    public ResponseEntity<AulaResponse> criarAula(@RequestBody @Valid AulaRequest request) {
        aulaService.criarAula(request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PatchMapping("/{idAula}/turmas/{idTurma}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDENADOR', 'PROFESSOR')")
    public ResponseEntity<Void> vincularTurma(@PathVariable UUID idAula, @PathVariable UUID idTurma) {
        aulaService.vincularTurma(idAula, idTurma);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/{idAula}/turmas/{idTurma}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDENADOR', 'PROFESSOR')")
    public ResponseEntity<Void> desvincularTurma(@PathVariable UUID idAula, @PathVariable UUID idTurma) {
        aulaService.desvincularTurma(idAula, idTurma);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
