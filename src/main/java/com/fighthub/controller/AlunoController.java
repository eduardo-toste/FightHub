package com.fighthub.controller;

import com.fighthub.dto.aluno.AlunoDetalhadoResponse;
import com.fighthub.dto.aluno.AlunoResponse;
import com.fighthub.dto.aluno.CriarAlunoRequest;
import com.fighthub.service.AlunoService;
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
@RequestMapping("/alunos")
@RequiredArgsConstructor
public class AlunoController {

    private final AlunoService alunoService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDENADOR', 'PROFESSOR')")
    public ResponseEntity<Void> criarAluno(@RequestBody @Valid CriarAlunoRequest request) {
        alunoService.criarAluno(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<Page<AlunoResponse>> obterAlunos(Pageable pageable) {
        Page<AlunoResponse> alunos = alunoService.obterTodos(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(alunos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlunoDetalhadoResponse> obterAluno(@PathVariable UUID id) {
        var aluno = alunoService.obterAluno(id);
        return ResponseEntity.status(HttpStatus.OK).body(aluno);
    }
}
