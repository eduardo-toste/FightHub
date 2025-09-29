package com.fighthub.controller;

import com.fighthub.dto.usuario.UpdateRoleRequest;
import com.fighthub.dto.usuario.UsuarioDetalhadoResponse;
import com.fighthub.dto.usuario.UsuarioResponse;
import com.fighthub.model.Usuario;
import com.fighthub.service.UsuarioService;
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
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UsuarioResponse>> obterUsuarios(Pageable pageable) {
        var usuarios = usuarioService.obterTodosUsuarios(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(usuarios);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioDetalhadoResponse> obterUsuarioEspecifico(@PathVariable UUID id) {
        var usuario = usuarioService.obterUsuario(id);
        return ResponseEntity.status(HttpStatus.OK).body(usuario);
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> atualizarRole(@PathVariable UUID id, @RequestBody @Valid UpdateRoleRequest request) {
        var usuario = usuarioService.updateRole(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(usuario);
    }
}
