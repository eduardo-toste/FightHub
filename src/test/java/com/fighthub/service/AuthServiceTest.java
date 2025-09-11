package com.fighthub.service;

import com.fighthub.dto.AuthRequest;
import com.fighthub.exception.TokenInvalidoException;
import com.fighthub.exception.UsuarioNaoEncontradoException;
import com.fighthub.model.Usuario;
import com.fighthub.model.enums.Role;
import com.fighthub.repository.TokenRepository;
import com.fighthub.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthService authService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario(
                UUID.randomUUID(), "Teste", "teste@gmail.com", "senhaCriptografada",
                null, Role.ALUNO, false, true
        );
    }

    @Test
    void deveFazerLoginComSucesso() {
        // Arrange
        AuthRequest request = new AuthRequest("teste@gmail.com", "password");
        String jwtEsperado = "jwt-token-gerado";
        String refreshEsperado = "refresh-token-gerado";

        when(usuarioRepository.findByEmail(request.email()))
                .thenReturn(Optional.of(usuario));
        when(jwtService.gerarToken(usuario)).thenReturn(jwtEsperado);
        when(jwtService.gerarRefreshToken(usuario)).thenReturn(refreshEsperado);

        // Act
        var result = authService.login(request);

        // Assert
        assertNotNull(result);
        assertEquals(jwtEsperado, result.accessToken());
        assertEquals(refreshEsperado, result.refreshToken());

        // Verifica que as dependências foram chamadas corretamente
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenService).revogarTokens(usuario);
        verify(tokenService).salvarTokens(usuario, jwtEsperado, refreshEsperado);
    }

    @Test
    void deveLancarExcecao_QuandoEmailDoUsuarioNaoForEncontrado() {
        // Arrange
        AuthRequest request = new AuthRequest("naoexiste@gmail.com", "password");
        when(usuarioRepository.findByEmail(request.email()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsuarioNaoEncontradoException.class,
                () -> authService.login(request));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenService, never()).revogarTokens(any());
        verify(jwtService, never()).gerarToken(any());
        verify(jwtService, never()).gerarRefreshToken(any());
    }

    @Test
    void deveAtualizarTokenComSucesso_QuandoRefreshForValido() {
        // Arrange
        String refreshToken = jwtService.gerarRefreshToken(usuario);
        String jwtEsperado = "jwt-token-gerado";
        when(jwtService.tokenValido(refreshToken)).thenReturn(true);
        when(jwtService.extrairEmail(refreshToken)).thenReturn(usuario.getEmail());
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(jwtService.gerarToken(usuario)).thenReturn(jwtEsperado);

        // Act
        var result = authService.atualizarToken(refreshToken);

        // Assert
        assertNotNull(result);
        assertEquals("jwt-token-gerado", result.newAccessToken());
        verify(tokenService).revogarAccessToken(usuario);
        verify(tokenService).salvarAccessToken(usuario, jwtEsperado);
        verify(jwtService).extrairEmail(refreshToken);
        verify(jwtService).gerarToken(usuario);
    }

    @Test
    void deveLancarExcecao_QuandoRefreshTokenForInvalido() {
        // Arrange
        String refreshToken = "refresh-token-invalido";
        when(jwtService.tokenValido(refreshToken)).thenReturn(false);

        // Act
        var ex = assertThrows(TokenInvalidoException.class,
                () -> authService.atualizarToken(refreshToken));

        // Assert
        assertEquals("Token JWT inválido ou malformado.", ex.getMessage());
    }

    @Test
    void deveLancarExcecao_QuandoEmailDoUsuarioNaoForEncontradoDuranteAtualizacao() {
        // Arrange
        String refreshToken = "refresh-token-valido";
        when(jwtService.tokenValido(refreshToken)).thenReturn(true);
        when(jwtService.extrairEmail(refreshToken)).thenReturn(usuario.getEmail());
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.empty());

        // Act
        var ex = assertThrows(UsuarioNaoEncontradoException.class,
                () -> authService.atualizarToken(refreshToken));

        // Assert
        assertEquals("Usuário não encontrado.", ex.getMessage());
    }

    @Test
    void deveRealizarLogoutComSucesso_QuandoTokenForValido() {
        // Arrange
        String jwt = "jwt-valido";
        when(request.getHeader(HttpHeaders.AUTHORIZATION))
                .thenReturn("Bearer " + jwt);

        // Act
        authService.logout(request);

        // Assert
        verify(tokenService).revogarTokensPorJwt(jwt);
    }

    @Test
    void deveLancarExcecao_QuandoTokenForNuloNaRequest() {
        // Arrange
        when(request.getHeader(HttpHeaders.AUTHORIZATION))
                .thenReturn(null);

        // Act
        var ex = assertThrows(TokenInvalidoException.class,
                () -> authService.logout(request));

        // Assert
        assertEquals("Token JWT inválido ou malformado.", ex.getMessage());
    }

    @Test
    void deveLancarExcecao_QuandoTokenNaoIniciarCorretamenteNaRequest() {
        // Arrange
        when(request.getHeader(HttpHeaders.AUTHORIZATION))
                .thenReturn(" ");

        // Act
        var ex = assertThrows(TokenInvalidoException.class,
                () -> authService.logout(request));

        // Assert
        assertEquals("Token JWT inválido ou malformado.", ex.getMessage());
    }

}