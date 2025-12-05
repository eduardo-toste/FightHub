package com.fighthub.controller;

import com.fighthub.docs.SwaggerExamples;
import com.fighthub.dto.usuario.*;
import com.fighthub.exception.dto.ErrorResponse;
import com.fighthub.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Endpoints para gerenciamento de Usuários no FightHub")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @Operation(summary = "Listagem de usuários", description = "Retorna uma lista paginada de usuários cadastrados no sistema.")
    @ApiResponse(responseCode = "200", description = "Lista de usuários retornada com sucesso",
            content = @Content(schema = @Schema(implementation = UsuarioResponse.class)))
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','COORDENADOR')")
    public ResponseEntity<Page<UsuarioResponse>> obterUsuarios(Pageable pageable) {
        var usuarios = usuarioService.obterTodosUsuarios(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(usuarios);
    }

    @Operation(summary = "Consulta de usuário por ID", description = "Retorna os dados detalhados de um usuário específico pelo seu ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário encontrado",
                    content = @Content(schema = @Schema(implementation = UsuarioDetalhadoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Usuário não encontrado", value = SwaggerExamples.USUARIO_NAO_ENCONTRADO)))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COORDENADOR')")
    public ResponseEntity<UsuarioDetalhadoResponse> obterUsuarioEspecifico(@PathVariable UUID id) {
        var usuario = usuarioService.obterUsuario(id);
        return ResponseEntity.status(HttpStatus.OK).body(usuario);
    }

    @Operation(summary = "Atualização de role de usuário", description = "Permite que um ADMIN altere a role de um usuário existente (ex: de ALUNO para PROFESSOR).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Role atualizada com sucesso",
                    content = @Content(schema = @Schema(implementation = UsuarioResponse.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Erro de validação", value = SwaggerExamples.ERRO_VALIDACAO))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Usuário não encontrado", value = SwaggerExamples.USUARIO_NAO_ENCONTRADO)))
    })
    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> atualizarRole(@PathVariable UUID id, @RequestBody @Valid UpdateRoleRequest request) {
        var usuario = usuarioService.updateRole(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(usuario);
    }

    @Operation(summary = "Atualização de status de usuário", description = "Permite ativar ou desativar um usuário do sistema.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = UsuarioResponse.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Erro de validação", value = SwaggerExamples.ERRO_VALIDACAO))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Usuário não encontrado", value = SwaggerExamples.USUARIO_NAO_ENCONTRADO))),
            @ApiResponse(responseCode = "409", description = "Status já definido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Status já definido", value = SwaggerExamples.STATUS_INVALIDO)))
    })
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> atualizarStatus(@PathVariable UUID id, @RequestBody @Valid UpdateStatusRequest request) {
        var usuario = usuarioService.updateStatus(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(usuario);
    }

    @Operation(summary = "Atualização completa de usuário", description = "Atualiza todos os dados de um usuário existente. Substitui completamente o estado atual.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = UsuarioDetalhadoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Erro de validação", value = SwaggerExamples.ERRO_VALIDACAO))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Usuário não encontrado", value = SwaggerExamples.USUARIO_NAO_ENCONTRADO)))
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioDetalhadoResponse> atualizarUsuarioCompleto(@PathVariable UUID id, @RequestBody @Valid UsuarioUpdateCompletoRequest request) {
        var usuario = usuarioService.updateUsuarioCompleto(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(usuario);
    }

    @Operation(summary = "Atualização parcial de usuário", description = "Atualiza apenas os campos informados de um usuário.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = UsuarioDetalhadoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Erro de validação", value = SwaggerExamples.ERRO_VALIDACAO))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Usuário não encontrado", value = SwaggerExamples.USUARIO_NAO_ENCONTRADO)))
    })
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioDetalhadoResponse> atualizarUsuarioParcial(@PathVariable UUID id, @RequestBody @Valid UsuarioUpdateParcialRequest request) {
        var usuario = usuarioService.updateUsuarioParcial(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(usuario);
    }

    @Operation(summary = "Consulta dos próprios dados", description = "Retorna os dados do usuário autenticado com base no token enviado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dados retornados com sucesso",
                    content = @Content(schema = @Schema(implementation = UsuarioDetalhadoResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UsuarioDetalhadoResponse> obterDadosProprios(HttpServletRequest request) {
        var usuario = usuarioService.obterDadosDoProprioUsuario(request);
        return ResponseEntity.status(HttpStatus.OK).body(usuario);
    }

    @Operation(summary = "Atualização completa dos próprios dados", description = "Atualiza todos os dados pessoais do usuário autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dados atualizados com sucesso",
                    content = @Content(schema = @Schema(implementation = UsuarioDetalhadoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UsuarioDetalhadoResponse> alterarDadosPropriosCompletamente(HttpServletRequest request, @RequestBody @Valid UsuarioUpdateCompletoRequest updateRequest) {
        var usuario = usuarioService.updateProprioCompleto(request, updateRequest);
        return ResponseEntity.status(HttpStatus.OK).body(usuario);
    }

    @Operation(summary = "Atualização parcial dos próprios dados", description = "Atualiza apenas os campos informados do usuário autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dados atualizados com sucesso",
                    content = @Content(schema = @Schema(implementation = UsuarioDetalhadoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UsuarioDetalhadoResponse> alterarDadosPropriosParcialmente(HttpServletRequest request, @RequestBody @Valid UsuarioUpdateParcialRequest updateRequest) {
        var usuario = usuarioService.updateProprioParcial(request, updateRequest);
        return ResponseEntity.status(HttpStatus.OK).body(usuario);
    }

    @Operation(summary = "Alteração de senha do próprio usuário", description = "Permite que o usuário altere sua senha atual.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Senha atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> alterarSenha(HttpServletRequest request, @RequestBody @Valid UpdateSenhaRequest updateRequest) {
        usuarioService.updateSenha(request, updateRequest);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}