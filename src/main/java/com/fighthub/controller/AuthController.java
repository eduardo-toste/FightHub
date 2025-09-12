package com.fighthub.controller;

import com.fighthub.docs.SwaggerExamples;
import com.fighthub.dto.AuthRequest;
import com.fighthub.dto.RefreshTokenRequest;
import com.fighthub.dto.AuthResponse;
import com.fighthub.dto.RefreshTokenResponse;
import com.fighthub.exception.dto.ErrorResponse;
import com.fighthub.service.AuthService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoints para autenticação e gerenciamento de tokens JWT no FightHub")
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Login do usuário",
            description = "Autentica um usuário com email e senha, retornando o token JWT de acesso e o token de refresh."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "Credenciais inválidas", value = SwaggerExamples.CREDENCIAIS_INVALIDAS)
                            }
                    )
            )
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid AuthRequest request) {
        var response = authService.login(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(
            summary = "Atualização de token",
            description = "Gera um novo token de acesso (JWT) a partir de um refresh token válido."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Refresh token inválido ou expirado",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "Token malformado", value = SwaggerExamples.TOKEN_MALFORMADO),
                                    @ExampleObject(name = "Token expirado", value = SwaggerExamples.TOKEN_EXPIRADO)
                            }
                    )
            )
    })
    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> atualizarToken(@RequestBody @Valid RefreshTokenRequest request) {
        var response = authService.atualizarToken(request.refreshToken());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(
            summary = "Logout do usuário",
            description = "Invalida o token JWT atual, impedindo seu uso futuro."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logout realizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido, expirado ou ausente",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "Token ausente", value = SwaggerExamples.TOKEN_MALFORMADO),
                                    @ExampleObject(name = "Token expirado", value = SwaggerExamples.TOKEN_EXPIRADO)
                            }
                    )
            )
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        authService.logout(request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}