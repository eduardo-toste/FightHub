package com.fighthub.service;

import com.fighthub.exception.TokenExpiradoException;
import com.fighthub.exception.TokenInvalidoException;
import com.fighthub.model.Usuario;
import com.fighthub.model.enums.Role;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        ReflectionTestUtils.setField(jwtService, "jwtSecret", "minha-chave-super-secreta-para-teste-123456789");
        ReflectionTestUtils.setField(jwtService, "expiration", 900000L);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", 604800000L);

        jwtService.init();
    }

    @Test
    void deveGerarTokenPadraoComSucesso() {
        // Arrange
        Usuario usuario = new Usuario(UUID.randomUUID(), "Teste", "teste@gmail.com", "senhaCriptografada", null, Role.ALUNO, false, true);

        // Act
        String result = jwtService.gerarToken(usuario);

        // Assert
        assertNotNull(result);
        assertTrue(result.startsWith("eyJ"));
    }

    @Test
    void deveGerarRefreshTokenComSucesso() {
        // Arrange
        Usuario usuario = new Usuario(UUID.randomUUID(), "Teste", "teste@gmail.com", "senhaCriptografada", null, Role.ALUNO, false, true);

        // Act
        String result = jwtService.gerarRefreshToken(usuario);

        // Assert
        assertNotNull(result);
        assertTrue(result.startsWith("eyJ"));
    }

    @Test
    void deveValidarTokenCorretamente() {
        // Arrange
        Usuario usuario = new Usuario(UUID.randomUUID(), "Teste", "teste@gmail.com", "senhaCriptografada", null, Role.ALUNO, false, true);
        String token = jwtService.gerarToken(usuario);

        // Act
        boolean result = jwtService.tokenValido(token);

        // Assert
        assertTrue(result);
    }

    @Test
    void deveRetornarFalse_QuandoTokenForInvalido() {
        // Arrange
        String tokenInvalido = "isto-nao-e-um-jwt";

        // Act
        boolean result = jwtService.tokenValido(tokenInvalido);

        // Assert
        assertFalse(result);
    }

    @Test
    void deveRetornarFalse_QuandoTokenEstiverExpirado() throws InterruptedException {
        // Arrange
        ReflectionTestUtils.setField(jwtService, "expiration", 1L);
        jwtService.init();

        Usuario usuario = new Usuario(UUID.randomUUID(), "Teste", "teste@gmail.com", "senhaCriptografada", null, Role.ALUNO, false, true);
        String tokenExpirado = jwtService.gerarToken(usuario);

        // Pequena pausa para garantir expiração
        Thread.sleep(5);

        // Act
        boolean result = jwtService.tokenValido(tokenExpirado);

        // Assert
        assertFalse(result);

        // Reset expiration para não afetar outros testes
        ReflectionTestUtils.setField(jwtService, "expiration", 900000L);
        jwtService.init();
    }

    @Test
    void deveExtrairOEmailComSucesso_QuandoTokenForValido() {
        // Arrange
        Usuario usuario = new Usuario(UUID.randomUUID(), "Teste", "teste@gmail.com", "senhaCriptografada", null, Role.ALUNO, false, true);
        String token = jwtService.gerarToken(usuario);

        // Act
        String result = jwtService.extrairEmail(token);

        // Assert
        assertNotNull(result);
        assertEquals("teste@gmail.com", result);
    }

    @Test
    void deveLancarExcecao_QuandoTokenForInvalido() {
        // Arrange
        String tokenInvalido = "isto-nao-e-um-jwt";

        // Act
        var ex = assertThrows(TokenInvalidoException.class,
                () -> jwtService.extrairEmail(tokenInvalido));

        // Assert
        assertEquals("Token JWT inválido ou malformado.", ex.getMessage());
    }

    @Test
    void deveLancarExcecao_QuandoTokenEstiverExpirado() throws InterruptedException {
        // Arrange
        ReflectionTestUtils.setField(jwtService, "expiration", 1L);
        jwtService.init();

        Usuario usuario = new Usuario(UUID.randomUUID(), "Teste", "teste@gmail.com", "senhaCriptografada", null, Role.ALUNO, false, true);
        String tokenExpirado = jwtService.gerarToken(usuario);

        // Pequena pausa para garantir expiração
        Thread.sleep(5);

        // Act
        var ex = assertThrows(TokenExpiradoException.class,
                () -> jwtService.extrairEmail(tokenExpirado));

        // Assert
        assertEquals("Token expirado.", ex.getMessage());

        // Reset expiration para não afetar outros testes
        ReflectionTestUtils.setField(jwtService, "expiration", 900000L);
        jwtService.init();
    }
}