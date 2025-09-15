package com.fighthub.controller;

import com.fighthub.dto.CriarAlunoRequest;
import com.fighthub.service.AlunoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
