package com.fighthub.repository;

import com.fighthub.model.Endereco;
import com.fighthub.model.Token;
import com.fighthub.model.Usuario;
import com.fighthub.model.enums.Role;
import com.fighthub.model.enums.TokenType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class TokenRepositoryTest {

    @Autowired
    private TokenRepository repository;

    @Autowired
    private UsuarioRepository usuarioRepository;

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

        usuario = usuarioRepository.save(usuario);
    }

    @Test
    void deveBuscarTokensValidosENaoRevogados_QuandoExistirem() {
        // Arrange
        Token token1 = Token.builder()
                .usuario(usuario)
                .token("token-1")
                .tokenType(TokenType.ACCESS)
                .expired(false)
                .revoked(false)
                .criadoEm(LocalDateTime.now())
                .expiraEm(LocalDateTime.now().plusHours(1))
                .build();

        Token token2 = Token.builder()
                .usuario(usuario)
                .token("token-2")
                .tokenType(TokenType.REFRESH)
                .expired(false)
                .revoked(false)
                .criadoEm(LocalDateTime.now())
                .expiraEm(LocalDateTime.now().plusDays(7))
                .build();

        Token tokenExpirado = Token.builder()
                .usuario(usuario)
                .token("token-expirado")
                .tokenType(TokenType.ACCESS)
                .expired(true)
                .revoked(false)
                .criadoEm(LocalDateTime.now())
                .expiraEm(LocalDateTime.now().minusHours(1))
                .build();

        repository.saveAll(List.of(token1, token2, tokenExpirado));

        // Act
        var result = repository.findAllByUsuarioAndExpiredFalseAndRevokedFalse(usuario);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(t -> t.getToken().equals("token-1")));
        assertTrue(result.stream().anyMatch(t -> t.getToken().equals("token-2")));
        assertTrue(result.stream().noneMatch(t -> t.getToken().equals("token-expirado")));
    }

    @Test
    void deveBuscarTokensNaoRevogadosETipoEspecifico_QuandoExistirem() {
        // Arrange
        Token token1 = Token.builder()
                .usuario(usuario)
                .token("token-1")
                .tokenType(TokenType.ACCESS)
                .expired(false)
                .revoked(false)
                .criadoEm(LocalDateTime.now())
                .expiraEm(LocalDateTime.now().plusHours(1))
                .build();

        Token token2 = Token.builder()
                .usuario(usuario)
                .token("token-2")
                .tokenType(TokenType.REFRESH)
                .expired(false)
                .revoked(false)
                .criadoEm(LocalDateTime.now())
                .expiraEm(LocalDateTime.now().plusDays(7))
                .build();

        Token tokenExpirado = Token.builder()
                .usuario(usuario)
                .token("token-expirado")
                .tokenType(TokenType.ACCESS)
                .expired(true)
                .revoked(true)
                .criadoEm(LocalDateTime.now())
                .expiraEm(LocalDateTime.now().minusHours(1))
                .build();

        repository.saveAll(List.of(token1, token2, tokenExpirado));

        // Act
        var result = repository.findAllByUsuarioAndRevokedFalseAndTokenType(usuario, TokenType.ACCESS);

        // Assert
        assertEquals(1, result.size());
        assertTrue(result.stream().anyMatch(t -> t.getToken().equals("token-1")));
    }

    @Test
    void deveRetornarUmToken_QuandoEleExistir() {
        // Arrange
        Token token1 = Token.builder()
                .usuario(usuario)
                .token("token-1")
                .tokenType(TokenType.ACCESS)
                .expired(false)
                .revoked(false)
                .criadoEm(LocalDateTime.now())
                .expiraEm(LocalDateTime.now().plusHours(1))
                .build();

        repository.save(token1);

        // Act
        var result = repository.findByToken("token-1");

        // Assert
        assertTrue(result.isPresent());
    }

    @Test
    void deveRetornarUmTokenPorTipo() {
        // Arrange
        Token token1 = Token.builder()
                .usuario(usuario)
                .token("token-1")
                .tokenType(TokenType.ATIVACAO)
                .expired(false)
                .revoked(false)
                .criadoEm(LocalDateTime.now())
                .expiraEm(LocalDateTime.now().plusHours(1))
                .build();

        repository.save(token1);

        // Act
        var result = repository.findByTokenAndTokenType("token-1", TokenType.ATIVACAO);

        // Assert
        assertTrue(result.isPresent());
    }

}