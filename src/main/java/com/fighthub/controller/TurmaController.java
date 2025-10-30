package com.fighthub.controller;

import com.fighthub.dto.turma.TurmaRequest;
import com.fighthub.dto.turma.TurmaResponse;
import com.fighthub.dto.turma.TurmaUpdateCompletoRequest;
import com.fighthub.dto.turma.TurmaUpdateStatusRequest;
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
    public ResponseEntity<Page<TurmaResponse>> buscarTurmas(Pageable pageable) {
        var turmas = turmaService.buscarTurmas(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(turmas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TurmaResponse> buscarTurmaPorId(@PathVariable UUID id) {
        var turma = turmaService.buscarTurmaPorId(id);
        return ResponseEntity.status(HttpStatus.OK).body(turma);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDENADOR', ' PROFESSOR')")
    public ResponseEntity<TurmaResponse> atualizarTurma(@PathVariable UUID id, @RequestBody @Valid TurmaUpdateCompletoRequest request) {
        var turmaAtualizada = turmaService.atualizarTurma(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(turmaAtualizada);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDENADOR', ' PROFESSOR')")
    public ResponseEntity<TurmaResponse> atualizarStatusTurma(@PathVariable UUID id, @RequestBody @Valid TurmaUpdateStatusRequest request) {
        var turmaAtualizada = turmaService.atualizarStatusTurma(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(turmaAtualizada);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDENADOR', ' PROFESSOR')")
    public ResponseEntity<Void> excluirTurma(@PathVariable UUID id) {
        turmaService.excluirTurma(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
