package com.fighthub.repository;

import com.fighthub.model.Endereco;
import com.fighthub.model.Responsavel;
import com.fighthub.model.Usuario;
import com.fighthub.model.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class ResponsavelRepositoryTest {

    @Autowired
    private ResponsavelRepository responsavelRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario usuario;
    private Responsavel responsavel;
    private Endereco endereco;

    @BeforeEach
    void setUp() {
        endereco = Endereco.builder()
                .cep("12345-678")
                .logradouro("Rua das Flores")
                .numero("123")
                .complemento("Apto 45")
                .bairro("Centro")
                .cidade("São Paulo")
                .estado("SP")
                .build();

        usuario = Usuario.builder()
                .id(UUID.randomUUID())
                .nome("Usuario Teste")
                .email("email@teste.com")
                .cpf("111.111.111-11")
                .role(Role.ALUNO)
                .ativo(false)
                .loginSocial(false)
                .endereco(endereco)
                .build();

        usuario = usuarioRepository.save(usuario);

        responsavel = Responsavel.builder()
                .id(UUID.randomUUID())
                .usuario(usuario)
                .alunos(new ArrayList<>())
                .build();

        responsavel = responsavelRepository.save(responsavel);
    }

    @Test
    void deveRetornarPageComTodosResponsaveis() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        var result = responsavelRepository.findAll(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void deveRetornarResponsavelPorId() {
        // Act
        var result = responsavelRepository.findById(responsavel.getId());

        // Assert
        assertTrue(result.isPresent());
    }

}