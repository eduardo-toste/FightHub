package com.fighthub.controller;

import com.fighthub.dto.turma.TurmaRequest;
import com.fighthub.dto.turma.TurmaResponse;
import com.fighthub.service.TurmaService;
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
@RequestMapping("/turmas")
@RequiredArgsConstructor
public class TurmaController {

    private final TurmaService turmaService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDENADOR', ' PROFESSOR')")
    public ResponseEntity<Void> criarTurma(@RequestBody @Valid TurmaRequest request) {
        turmaService.criarTurma(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDENADOR', ' PROFESSOR')")
    public ResponseEntity<Page<TurmaResponse>> buscarTurmas(Pageable pageable) {
        var turmas = turmaService.buscarTurmas(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(turmas);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDENADOR', ' PROFESSOR')")
    public ResponseEntity<TurmaResponse> buscarTurmaPorId(@PathVariable UUID id) {
        var turma = turmaService.buscarTurmaPorId(id);
        return ResponseEntity.status(HttpStatus.OK).body(turma);
    }
}
