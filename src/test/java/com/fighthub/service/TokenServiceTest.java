package com.fighthub.service;

import com.fighthub.model.Endereco;
import com.fighthub.model.Token;
import com.fighthub.model.Usuario;
import com.fighthub.model.enums.Role;
import com.fighthub.model.enums.TokenType;
import com.fighthub.repository.TokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private TokenService tokenService;

    @Captor
    private ArgumentCaptor<Token> tokenCaptor;

    private Usuario usuario;

    @BeforeEach
    void setup() {
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
                null, // foto
                Role.ALUNO,
                false, // loginSocial
                true,  // ativo
                "123.456.789-00", // cpf
                "(11)91234-5678", // telefone
                endereco
        );
    }

    @Test
    void deveSalvarTokensComSucesso() {
        // Arrange
        var accessTokenRecebido = "access-token";
        var refreshTokenRecebido = "refresh-token";

        when(tokenRepository.saveAll(anyList())).thenReturn(Collections.emptyList());

        // Act
        tokenService.salvarTokens(usuario, accessTokenRecebido, refreshTokenRecebido);

        // Assert
        ArgumentCaptor<List<Token>> captor = ArgumentCaptor.forClass(List.class);
        verify(tokenRepository).saveAll(captor.capture());

        List<Token> tokensSalvos = captor.getValue();
        assertEquals(2, tokensSalvos.size());

        Token tokenAccess = tokensSalvos.get(0);
        Token tokenRefresh = tokensSalvos.get(1);

        assertEquals(accessTokenRecebido, tokenAccess.getToken());
        assertEquals(TokenType.ACCESS, tokenAccess.getTokenType());
        assertEquals(usuario, tokenAccess.getUsuario());
        assertFalse(tokenAccess.isExpired());
        assertFalse(tokenAccess.isRevoked());

        assertEquals(refreshTokenRecebido, tokenRefresh.getToken());
        assertEquals(TokenType.REFRESH, tokenRefresh.getTokenType());
        assertEquals(usuario, tokenRefresh.getUsuario());
        assertFalse(tokenRefresh.isExpired());
        assertFalse(tokenRefresh.isRevoked());
    }

    @Test
    void deveSalvarAccessTokenComSucesso() {
        // Arrange
        var accessTokenRecebido = "access-token";
        when(tokenRepository.save(any(Token.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        tokenService.salvarAccessToken(usuario, accessTokenRecebido);

        // Assert
        ArgumentCaptor<Token> captor = ArgumentCaptor.forClass(Token.class);
        verify(tokenRepository).save(captor.capture());

        Token tokenSalvo = captor.getValue();
        assertEquals(accessTokenRecebido, tokenSalvo.getToken());
        assertEquals(TokenType.ACCESS, tokenSalvo.getTokenType());
        assertEquals(usuario, tokenSalvo.getUsuario());
        assertFalse(tokenSalvo.isExpired());
        assertFalse(tokenSalvo.isRevoked());
        assertNotNull(tokenSalvo.getCriadoEm());
        assertNotNull(tokenSalvo.getExpiraEm());
        assertTrue(tokenSalvo.getExpiraEm().isAfter(tokenSalvo.getCriadoEm()));
    }

    @Test
    void deveSalvarTokenAtivacaoComSucesso() {
        // Arrange
        when(tokenRepository.save(any(Token.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        tokenService.salvarTokenAtivacao(usuario);

        // Assert
        ArgumentCaptor<Token> captor = ArgumentCaptor.forClass(Token.class);
        verify(tokenRepository).save(captor.capture());

        Token tokenSalvo = captor.getValue();
        assertEquals(TokenType.ATIVACAO, tokenSalvo.getTokenType());
        assertEquals(usuario, tokenSalvo.getUsuario());
        assertFalse(tokenSalvo.isExpired());
        assertFalse(tokenSalvo.isRevoked());
        assertNotNull(tokenSalvo.getCriadoEm());
        assertNotNull(tokenSalvo.getExpiraEm());
        assertTrue(tokenSalvo.getExpiraEm().isAfter(tokenSalvo.getCriadoEm()));
    }

    @Test
    void deveRevogarTokensComSucesso() {
        // Arrange
        Token token1 = Token.builder()
                .usuario(usuario)
                .token("token-1")
                .tokenType(TokenType.ACCESS)
                .expired(false)
                .revoked(false)
                .build();

        Token token2 = Token.builder()
                .usuario(usuario)
                .token("token-2")
                .tokenType(TokenType.REFRESH)
                .expired(false)
                .revoked(false)
                .build();

        List<Token> tokens = List.of(token1, token2);
        when(tokenRepository.findAllByUsuarioAndExpiredFalseAndRevokedFalse(usuario)).thenReturn(tokens);

        // Act
        tokenService.revogarTokens(usuario);

        // Assert
        assertTrue(token1.isExpired());
        assertTrue(token1.isRevoked());
        assertTrue(token2.isExpired());
        assertTrue(token2.isRevoked());
        verify(tokenRepository).saveAll(tokens);
    }

    @Test
    void deveRevogarTokensPorJwt_QuandoTokenExiste() {
        // Arrange
        var tokenExistente = Token.builder()
                .usuario(usuario)
                .token("jwt-valido")
                .tokenType(TokenType.ACCESS)
                .expired(false)
                .revoked(false)
                .build();

        var tokensAtivos = List.of(
                Token.builder().usuario(usuario).token("t1").expired(false).revoked(false).build(),
                Token.builder().usuario(usuario).token("t2").expired(false).revoked(false).build()
        );

        when(tokenRepository.findByToken("jwt-valido")).thenReturn(Optional.of(tokenExistente));
        when(tokenRepository.findAllByUsuarioAndExpiredFalseAndRevokedFalse(usuario)).thenReturn(tokensAtivos);

        // Act
        tokenService.revogarTokensPorJwt("jwt-valido");

        // Assert
        tokensAtivos.forEach(token -> {
            assertTrue(token.isExpired());
            assertTrue(token.isRevoked());
        });
        verify(tokenRepository).saveAll(tokensAtivos);
    }

    @Test
    void naoDeveRevogarTokens_QuandoTokenNaoExiste() {
        // Arrange
        when(tokenRepository.findByToken("jwt-inexistente")).thenReturn(Optional.empty());

        // Act
        tokenService.revogarTokensPorJwt("jwt-inexistente");

        // Assert
        verify(tokenRepository, never()).findAllByUsuarioAndExpiredFalseAndRevokedFalse(any());
        verify(tokenRepository, never()).saveAll(any());
    }

    @Test
    void deveRevogarApenasTokensDeAcesso() {
        Token t1 = Token.builder().usuario(usuario).token("a1").tokenType(TokenType.ACCESS).expired(false).revoked(false).build();
        Token t2 = Token.builder().usuario(usuario).token("a2").tokenType(TokenType.ACCESS).expired(false).revoked(false).build();

        when(tokenRepository.findAllByUsuarioAndRevokedFalseAndTokenType(usuario, TokenType.ACCESS))
                .thenReturn(List.of(t1, t2));

        tokenService.revogarToken(usuario, TokenType.ACCESS);

        assertTrue(t1.isExpired());
        assertTrue(t1.isRevoked());
        assertTrue(t2.isExpired());
        assertTrue(t2.isRevoked());
        verify(tokenRepository).saveAll(List.of(t1, t2));
    }

    @Test
    void naoDeveChamarSaveAllQuandoNaoExistiremAccessTokensAtivos() {
        when(tokenRepository.findAllByUsuarioAndRevokedFalseAndTokenType(usuario, TokenType.ACCESS))
                .thenReturn(Collections.emptyList());

        tokenService.revogarToken(usuario, TokenType.ACCESS);

        verify(tokenRepository, never()).saveAll(anyList());
    }

    @Test
    void deveRevogarMultiplosAccessTokens() {
        List<Token> tokens = List.of(
                Token.builder().usuario(usuario).token("a1").tokenType(TokenType.ACCESS).expired(false).revoked(false).build(),
                Token.builder().usuario(usuario).token("a2").tokenType(TokenType.ACCESS).expired(false).revoked(false).build(),
                Token.builder().usuario(usuario).token("a3").tokenType(TokenType.ACCESS).expired(false).revoked(false).build()
        );

        when(tokenRepository.findAllByUsuarioAndRevokedFalseAndTokenType(usuario, TokenType.ACCESS))
                .thenReturn(tokens);

        tokenService.revogarToken(usuario, TokenType.ACCESS);

        tokens.forEach(token -> {
            assertTrue(token.isExpired());
            assertTrue(token.isRevoked());
        });
        verify(tokenRepository).saveAll(tokens);
    }

    @Test
    void deveGerarESalvarCodigoDeRecuperacao() {
        var codigo = tokenService.salvarCodigoRecuperacao(usuario);

        assertNotNull(codigo);
        assertEquals(6, codigo.length());

        verify(tokenRepository).save(tokenCaptor.capture());
        var tokenSalvo = tokenCaptor.getValue();

        assertEquals(codigo, tokenSalvo.getToken());
        assertEquals(TokenType.RECUPERACAO_SENHA, tokenSalvo.getTokenType());
        assertFalse(tokenSalvo.isExpired());
        assertFalse(tokenSalvo.isRevoked());
        assertEquals(usuario, tokenSalvo.getUsuario());
        assertNotNull(tokenSalvo.getCriadoEm());
        assertNotNull(tokenSalvo.getExpiraEm());
    }

    @Test
    void deveRetornarTrue_AoValidarCodigoRecuperaco_QuandoEleExistirVinculadoAoUsuario() {
        var codigoRecuperacao = "codigo-recuperacao";
        Token token = Token.builder()
                .usuario(usuario)
                .token(codigoRecuperacao)
                .tokenType(TokenType.RECUPERACAO_SENHA)
                .expired(false)
                .revoked(false)
                .criadoEm(LocalDateTime.now())
                .expiraEm(LocalDateTime.now().plusMinutes(15))
                .build();
        when(tokenRepository.findByTokenAndUsuarioAndExpiredFalseAndRevokedFalse(codigoRecuperacao, usuario)).thenReturn(Optional.of(token));

        var result = tokenService.validarCodigoRecuperacao(usuario, codigoRecuperacao);

        assertTrue(result);
        verify(tokenRepository).findByTokenAndUsuarioAndExpiredFalseAndRevokedFalse(codigoRecuperacao, usuario);
    }

    @Test
    void deveRetornarFalse_AoValidarCodigoRecuperaco_QuandoEleNaoExistirVinculadoAoUsuario() {
        var codigoRecuperacao = "codigo-recuperacao";
        Token token = Token.builder()
                .usuario(usuario)
                .token(codigoRecuperacao)
                .tokenType(TokenType.RECUPERACAO_SENHA)
                .expired(false)
                .revoked(false)
                .criadoEm(LocalDateTime.now())
                .expiraEm(LocalDateTime.now().plusMinutes(15))
                .build();
        when(tokenRepository.findByTokenAndUsuarioAndExpiredFalseAndRevokedFalse(codigoRecuperacao, usuario)).thenReturn(Optional.empty());

        var result = tokenService.validarCodigoRecuperacao(usuario, codigoRecuperacao);

        assertFalse(result);
        verify(tokenRepository).findByTokenAndUsuarioAndExpiredFalseAndRevokedFalse(codigoRecuperacao, usuario);
    }
}