package com.fighthub.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fighthub.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.transaction.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class IntegrationTestBase {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;

    @Autowired protected UsuarioRepository usuarioRepository;
    @Autowired protected TokenRepository tokenRepository;
    @Autowired protected AlunoRepository alunoRepository;
    @Autowired protected ResponsavelRepository responsavelRepository;
    @Autowired protected ProfessorRepository professorRepository;
    @Autowired protected TurmaRepository turmaRepository;
    @Autowired protected AulaRepository aulaRepository;
    @Autowired protected  InscricaoRepository inscricaoRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @BeforeEach
    void limparBaseDeDados() {
        entityManager.createNativeQuery("DELETE FROM alunos_responsaveis").executeUpdate();

        inscricaoRepository.deleteAll();
        alunoRepository.deleteAll();
        responsavelRepository.deleteAll();
        professorRepository.deleteAll();
        tokenRepository.deleteAll();
        usuarioRepository.deleteAll();
        turmaRepository.deleteAll();
        aulaRepository.deleteAll();

        entityManager.flush();
    }
}