package com.fighthub.controller;

import com.fighthub.dto.aula.AulaRequest;
import com.fighthub.dto.aula.AulaResponse;
import com.fighthub.dto.aula.AulaUpdateCompletoRequest;
import com.fighthub.service.AulaService;
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

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDENADOR', 'PROFESSOR')")
    public ResponseEntity<Page<AulaResponse>> buscarAulas(Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(aulaService.buscarAulas(pageable));
    }

    @GetMapping("/alunos")
    @PreAuthorize("hasAnyRole('ADMIN', 'ALUNO')")
    public ResponseEntity<Page<AulaResponse>> buscarAulasDisponiveisAluno(Pageable pageable, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(aulaService.buscarAulasDisponiveisAluno(pageable,request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AulaResponse> buscarAulaPorId(@PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.OK).body(aulaService.buscarAulaPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AulaResponse> atualizarAula(@RequestBody @Valid AulaUpdateCompletoRequest request, @PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.OK).body(aulaService.atualizarAula(request, id));
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirAula(@PathVariable UUID id) {
        aulaService.excluirAula(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
