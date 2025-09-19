package com.fighthub.controller;

import com.fighthub.dto.auth.AtivacaoRequest;
import com.fighthub.service.AtivacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ativar")
@RequiredArgsConstructor
public class AtivacaoController {

    private final AtivacaoService ativacaoService;

    @PostMapping
    public ResponseEntity<Void> ativarConta(@RequestBody AtivacaoRequest request) {
        ativacaoService.ativarConta(request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
