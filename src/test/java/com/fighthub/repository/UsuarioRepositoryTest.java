package com.fighthub.repository;

import com.fighthub.model.Endereco;
import com.fighthub.model.Usuario;
import com.fighthub.model.enums.Role;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private EntityManager entityManager;

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
    void deveBuscarUsuarioPeloEmail_QuandoExistir() {
        // Arrange
        repository.save(usuario);

        // Act
        var result = repository.findByEmail(usuario.getEmail());

        // Assert
        assertTrue(result.isPresent());
    }

    @Test
    void deveBuscarUsuarioPeloEmail_QuandoNaoExistir() {
        // Act
        var result = repository.findByEmail(usuario.getEmail());

        // Assert
        assertTrue(result.isEmpty());
    }

}