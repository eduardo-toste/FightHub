package com.fighthub.repository;

import com.fighthub.model.Aluno;
import com.fighthub.model.Endereco;
import com.fighthub.model.Usuario;
import com.fighthub.model.enums.Role;
import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DataJpaTest
@ActiveProfiles("test")
class AlunoRepositoryTest {

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Endereco endereco;
    private Usuario usuario;
    private Aluno aluno;

    @BeforeEach
    void setup() {
        endereco = Endereco.builder()
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
                null,
                Role.ALUNO,
                false,
                true,
                "123.456.789-00",
                "(11)91234-5678",
                endereco
        );

        usuario = usuarioRepository.save(usuario);

        aluno = Aluno.builder()
                .id(UUID.randomUUID())
                .usuario(usuario)
                .dataMatricula(LocalDate.now())
                .dataNascimento(LocalDate.now().minusYears(20))
                .responsaveis(new ArrayList<>())
                .build();

        aluno = alunoRepository.save(aluno);
    }

    @Test
    void deveRetornarPageComTodosAlunos() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        var result = alunoRepository.findAll(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void deveRetornarAlunoDetalhado() {
        // Act
        var result = alunoRepository.findById(aluno.getId());

        // Assert
        assertTrue(result.isPresent());
    }

    @Test
    void deveRetornarAluno_QuandoBuscadoPeloUsuarioId() {
        // Act
        var result = alunoRepository.findByUsuarioId(usuario.getId());

        // Assert
        assertTrue(result.isPresent());
    }

}