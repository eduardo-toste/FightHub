package com.fighthub.repository;

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
    void setUp() {
        usuario = new Usuario(
                UUID.randomUUID(), "Teste", "teste@gmail.com", "senhaCriptografada",
                null, Role.ALUNO, false, true
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