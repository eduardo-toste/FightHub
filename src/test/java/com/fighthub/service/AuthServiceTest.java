package com.fighthub.service;

import com.fighthub.dto.auth.AuthRequest;
import com.fighthub.dto.auth.ConfirmarRecuperacaoSenhaRequest;
import com.fighthub.dto.auth.RecuperarSenhaRequest;
import com.fighthub.dto.auth.ValidarCodigoRecuperacaoRequest;
import com.fighthub.exception.TokenInvalidoException;
import com.fighthub.exception.UsuarioNaoEncontradoException;
import com.fighthub.exception.ValidacaoException;
import com.fighthub.model.Endereco;
import com.fighthub.model.Token;
import com.fighthub.model.Usuario;
import com.fighthub.model.enums.Role;
import com.fighthub.model.enums.TokenType;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
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
    private TokenRepository tokenRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private TokenService tokenService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    private Usuario usuario;
    private Token refreshToken;

    @BeforeEach
    void setup() {
        String tokenFake = "refresh-token-fake";

        Endereco endereco = Endereco.builder()
                .cep("12345-678")
                .logradouro("Rua Exemplo")
                .numero("123")
                .complemento("Apto 45")
                .bairro("Centro")
                .cidade("São Paulo")
                .estado("SP")
                .build();

        usuario = new Usuario(
                UUID.randomUUID(),
                "Teste",
                "teste@gmail.com",
                "senhaCriptografada",
                null, Role.ALUNO,
                false, true, "123.456.789-00",
                "(11)91234-5678", endereco
        );

        refreshToken = Token.builder()
                .id(UUID.randomUUID())
                .token(tokenFake)
                .tokenType(TokenType.REFRESH)
                .revoked(false)
                .expired(false)
                .usuario(usuario)
                .criadoEm(LocalDateTime.now())
                .expiraEm(LocalDateTime.now().plusHours(1))
                .build();
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
        String jwtEsperado = "jwt-token-gerado";
        when(jwtService.tokenValido(refreshToken.getToken())).thenReturn(true);
        when(jwtService.extrairEmail(refreshToken.getToken())).thenReturn(usuario.getEmail());
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(jwtService.gerarToken(usuario)).thenReturn(jwtEsperado);
        when(tokenRepository.findByTokenAndTokenType(refreshToken.getToken(), TokenType.REFRESH)).thenReturn(Optional.of(refreshToken));

        // Act
        var result = authService.atualizarToken(refreshToken.getToken());

        // Assert
        assertNotNull(result);
        assertEquals("jwt-token-gerado", result.newAccessToken());
        verify(tokenService).revogarToken(usuario, TokenType.ACCESS);
        verify(tokenService).salvarAccessToken(usuario, jwtEsperado);
        verify(jwtService).extrairEmail(refreshToken.getToken());
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
        assertEquals("Token inválido ou malformado.", ex.getMessage());
    }

    @Test
    void deveLancarExcecao_QuandoTipoTokenNaoForRefresh() {
        // Arrange
        String token = "token-invalido";
        when(jwtService.tokenValido(token)).thenReturn(true);
        when(tokenRepository.findByTokenAndTokenType(token, TokenType.REFRESH)).thenReturn(Optional.empty());

        // Act
        var ex = assertThrows(ValidacaoException.class,
                () -> authService.atualizarToken(token));

        // Assert
        assertEquals("O token recebido não é do tipo REFRESH", ex.getMessage());
    }

    @Test
    void deveLancarExcecao_QuandoEmailDoUsuarioNaoForEncontradoDuranteAtualizacao() {
        // Arrange
        String refreshToken = "refresh-token-valido";

        when(jwtService.tokenValido(refreshToken)).thenReturn(true);
        when(tokenRepository.findByTokenAndTokenType(refreshToken, TokenType.REFRESH))
                .thenReturn(Optional.of(this.refreshToken));
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
        assertEquals("Token inválido ou malformado.", ex.getMessage());
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
        assertEquals("Token inválido ou malformado.", ex.getMessage());
    }

    @Test
    void deveRevogarCodigosDeRecuperacaoAntigosEEnviarEmailComNovoCodigo() {
        var request = new RecuperarSenhaRequest(usuario.getEmail());
        var codigoRecuperacao = "111222";
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(tokenService.salvarCodigoRecuperacao(usuario)).thenReturn(codigoRecuperacao);

        authService.recoverPassword(request);

        verify(usuarioRepository).findByEmail(usuario.getEmail());
        verify(tokenService).revogarToken(usuario, TokenType.RECUPERACAO_SENHA);
        verify(emailService).enviarEmailRecuperacaoSenha(usuario, codigoRecuperacao);
    }

    @Test
    void deveLancarExcecao_QuandoUsuarioNaoExistir_AoRevogarCodigosDeRecuperacaoAntigosEEnviarEmailComNovoCodigo() {
        // Arrange
        var request = new RecuperarSenhaRequest(usuario.getEmail());
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.empty());

        // Act
        var ex = assertThrows(UsuarioNaoEncontradoException.class,
                () -> authService.recoverPassword(request));

        // Assert
        assertEquals("Usuário não encontrado.", ex.getMessage());
    }

    @Test
    void deveValidarCorretamenteCodigoDeRecuperacao() {
        var codigoRecuperacao = "111222";
        var request = new ValidarCodigoRecuperacaoRequest(codigoRecuperacao, usuario.getEmail());
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(tokenService.validarCodigoRecuperacao(usuario, codigoRecuperacao)).thenReturn(true);

        authService.validateRecoverCode(request);

        verify(usuarioRepository).findByEmail(usuario.getEmail());
        verify(tokenService).validarCodigoRecuperacao(usuario, codigoRecuperacao);
    }

    @Test
    void deveLancarExcecao_QuandoUsuarioNaoExistir_AoValidarCodigoDeRecuperacao() {
        // Arrange
        var codigoRecuperacao = "111222";
        var request = new ValidarCodigoRecuperacaoRequest(codigoRecuperacao, usuario.getEmail());
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.empty());

        // Act
        var ex = assertThrows(UsuarioNaoEncontradoException.class,
                () -> authService.validateRecoverCode(request));

        // Assert
        assertEquals("Usuário não encontrado.", ex.getMessage());
    }

    @Test
    void deveLancarExcecao_QuandoCodigoForInvalido_AoValidarCodigoDeRecuperacao() {
        // Arrange
        var codigoRecuperacao = "111222";
        var request = new ValidarCodigoRecuperacaoRequest(codigoRecuperacao, usuario.getEmail());
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(tokenService.validarCodigoRecuperacao(usuario, codigoRecuperacao)).thenReturn(false);

        // Act
        var ex = assertThrows(TokenInvalidoException.class,
                () -> authService.validateRecoverCode(request));

        // Assert
        assertEquals("Token inválido ou malformado.", ex.getMessage());
    }

    @Test
    void deveConfirmarRecuperacaoDeSenhaComSucesso() {
        var codigoRecuperacao = "111222";
        var novaSenha = "teste123";
        var request = new ConfirmarRecuperacaoSenhaRequest(codigoRecuperacao, usuario.getEmail(), novaSenha);
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(tokenService.validarCodigoRecuperacao(usuario, codigoRecuperacao)).thenReturn(true);

        authService.confirmarRecuperacaoSenha(request);

        verify(usuarioRepository).findByEmail(usuario.getEmail());
        verify(tokenService).validarCodigoRecuperacao(usuario, codigoRecuperacao);
        verify(usuarioRepository).save(usuario);
        verify(tokenService).revogarToken(usuario, TokenType.RECUPERACAO_SENHA);
    }

    @Test
    void deveLancarExcecao_QuandoUsuarioNaoExistir_AoConfirmarRecuperacaoDeSenha() {
        // Arrange
        var codigoRecuperacao = "111222";
        var novaSenha = "teste123";
        var request = new ConfirmarRecuperacaoSenhaRequest(codigoRecuperacao, usuario.getEmail(), novaSenha);
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.empty());

        // Act
        var ex = assertThrows(UsuarioNaoEncontradoException.class,
                () -> authService.confirmarRecuperacaoSenha(request));

        // Assert
        assertEquals("Usuário não encontrado.", ex.getMessage());
    }

    @Test
    void deveLancarExcecao_QuandoCodigoForInvalido_AoConfirmarRecuperacaoDeSenha() {
        // Arrange
        var codigoRecuperacao = "111222";
        var novaSenha = "teste123";
        var request = new ConfirmarRecuperacaoSenhaRequest(codigoRecuperacao, usuario.getEmail(), novaSenha);
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(tokenService.validarCodigoRecuperacao(usuario, codigoRecuperacao)).thenReturn(false);

        // Act
        var ex = assertThrows(TokenInvalidoException.class,
                () -> authService.confirmarRecuperacaoSenha(request));

        // Assert
        assertEquals("Token inválido ou malformado.", ex.getMessage());
    }

}