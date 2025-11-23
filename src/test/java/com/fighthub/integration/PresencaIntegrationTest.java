package com.fighthub.integration;

import com.fighthub.dto.presenca.PresencaRequest;
import com.fighthub.model.Endereco;
import com.fighthub.model.Usuario;
import com.fighthub.model.Professor;
import com.fighthub.model.Aluno;
import com.fighthub.model.Turma;
import com.fighthub.model.Aula;
import com.fighthub.model.Inscricao;
import com.fighthub.model.Presenca;
import com.fighthub.model.enums.Role;
import com.fighthub.model.enums.SubscriptionStatus;
import com.fighthub.service.JwtService;
import com.fighthub.service.TokenService;
import com.fighthub.utils.IntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class PresencaIntegrationTest extends IntegrationTestBase {

    @Autowired private JwtService jwtService;
    @SpyBean private TokenService tokenService;

    private Usuario professorUsuario;
    private Usuario outroProfessorUsuario;
    private Usuario alunoUsuario;

    private Professor professor;
    private Professor outroProfessor;
    private Aluno aluno;

    private Turma turma;
    private Aula aula;
    private Inscricao inscricao;

    private String tokenProfessor;
    private String tokenOutroProfessor;
    private String tokenAluno;

    @BeforeEach
    void setup() {
        Endereco endereco = Endereco.builder()
                .cep("01000-000")
                .logradouro("Rua Teste")
                .numero("123")
                .bairro("Centro")
                .cidade("São Paulo")
                .estado("SP")
                .build();

        professorUsuario = usuarioRepository.save(Usuario.builder()
                .id(UUID.randomUUID())
                .nome("Professor A")
                .email("prof.a@example.com")
                .cpf("333.333.333-33")
                .telefone("(11)90000-0000")
                .role(Role.PROFESSOR)
                .ativo(true)
                .senha("senha")
                .endereco(endereco)
                .build());
        professor = professorRepository.save(Professor.builder().usuario(professorUsuario).build());

        outroProfessorUsuario = usuarioRepository.save(Usuario.builder()
                .id(UUID.randomUUID())
                .nome("Professor B")
                .email("prof.b@example.com")
                .cpf("444.444.444-44")
                .telefone("(11)91111-1111")
                .role(Role.PROFESSOR)
                .ativo(true)
                .senha("senha")
                .endereco(endereco)
                .build());
        outroProfessor = professorRepository.save(Professor.builder().usuario(outroProfessorUsuario).build());

        alunoUsuario = usuarioRepository.save(Usuario.builder()
                .id(UUID.randomUUID())
                .nome("Aluno Teste")
                .email("aluno.test@example.com")
                .cpf("555.555.555-55")
                .telefone("(11)92222-2222")
                .role(Role.ALUNO)
                .ativo(true)
                .senha("senha")
                .endereco(endereco)
                .build());
        aluno = alunoRepository.save(Aluno.builder()
                .id(UUID.randomUUID())
                .usuario(alunoUsuario)
                .dataNascimento(LocalDate.of(2005,1,1))
                .dataMatricula(LocalDate.now())
                .matriculaAtiva(true)
                .build());

        tokenProfessor = jwtService.gerarToken(professorUsuario);
        tokenOutroProfessor = jwtService.gerarToken(outroProfessorUsuario);
        tokenAluno = jwtService.gerarToken(alunoUsuario);

        tokenService.salvarAccessToken(professorUsuario, tokenProfessor);
        tokenService.salvarAccessToken(outroProfessorUsuario, tokenOutroProfessor);
        tokenService.salvarAccessToken(alunoUsuario, tokenAluno);

        turma = turmaRepository.save(Turma.builder()
                .nome("Turma Teste")
                .horario("19:00")
                .ativo(true)
                .professor(professor)
                .build());

        aula = aulaRepository.save(Aula.builder()
                .data(LocalDateTime.now().plusDays(1))
                .titulo("Aula Integracao")
                .turma(turma)
                .build());

        inscricao = inscricaoRepository.save(Inscricao.builder()
                .id(UUID.randomUUID())
                .aula(aula)
                .aluno(aluno)
                .inscritoEm(LocalDateTime.now())
                .status(SubscriptionStatus.INSCRITO)
                .build());
    }

    @Test
    void deveRetornar200_AoAtualizarPresenca_QuandoProfessorDaTurma() throws Exception {
        var request = new PresencaRequest(true);

        mockMvc.perform(patch("/aulas/{idAula}/presencas/inscricao/{idInscricao}", aula.getId(), inscricao.getId())
                        .header("Authorization", "Bearer " + tokenProfessor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        var saved = presencaRepository.findAll();
        assertFalse(saved.isEmpty());
        assertEquals(inscricao.getId(), saved.get(0).getInscricao().getId());
        assertTrue(saved.get(0).isPresente());
    }

    @Test
    void deveRetornar403_AoAtualizarPresenca_QuandoUsuarioNaoForProfessor() throws Exception {
        var request = new PresencaRequest(true);

        mockMvc.perform(patch("/aulas/{idAula}/presencas/inscricao/{idInscricao}", aula.getId(), inscricao.getId())
                        .header("Authorization", "Bearer " + tokenAluno)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveRetornar404_AoAtualizarPresenca_QuandoAulaInexistente() throws Exception {
        var request = new PresencaRequest(true);
        mockMvc.perform(patch("/aulas/{idAula}/presencas/inscricao/{idInscricao}", UUID.randomUUID(), inscricao.getId())
                        .header("Authorization", "Bearer " + tokenProfessor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar404_AoAtualizarPresenca_QuandoInscricaoInexistente() throws Exception {
        var request = new PresencaRequest(true);
        mockMvc.perform(patch("/aulas/{idAula}/presencas/inscricao/{idInscricao}", aula.getId(), UUID.randomUUID())
                        .header("Authorization", "Bearer " + tokenProfessor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar200_AoListarPresencasPorAula_QuandoExistiremPresencas() throws Exception {
        presencaRepository.save(Presenca.builder()
                .id(UUID.randomUUID())
                .inscricao(inscricao)
                .presente(true)
                .dataRegistro(LocalDate.now())
                .build());

        mockMvc.perform(get("/aulas/{idAula}/presencas", aula.getId())
                        .header("Authorization", "Bearer " + tokenProfessor)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].presente", is(true)));
    }

    @Test
    void deveRetornar200_AoListarPresencasPorAula_QuandoSemInscricoesRetornaVazio() throws Exception {
        Usuario prof = usuarioRepository.save(Usuario.builder()
                .id(UUID.randomUUID())
                .nome("Prof Vazio")
                .email("prof.vazio@example.com")
                .cpf("666.666.666-66")
                .telefone("(11)93333-3333")
                .role(Role.PROFESSOR)
                .ativo(true)
                .senha("senha")
                .endereco(Endereco.builder().cep("01000-000").logradouro("Rua").numero("1").bairro("B").cidade("C").estado("SP").build())
                .build());
        Professor p = professorRepository.save(Professor.builder().usuario(prof).build());
        String tokenProfVazio = jwtService.gerarToken(prof);
        tokenService.salvarAccessToken(prof, tokenProfVazio);

        Turma turmaVazia = turmaRepository.save(Turma.builder()
                .nome("Turma Vazia")
                .horario("20:00")
                .professor(p)
                .ativo(true)
                .build());
        Aula aulaVazia = aulaRepository.save(Aula.builder().data(LocalDateTime.now()).turma(turmaVazia).build());

        mockMvc.perform(get("/aulas/{idAula}/presencas", aulaVazia.getId())
                        .header("Authorization", "Bearer " + tokenProfVazio)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void deveRetornar409_AoListarPresencasPorAula_QuandoProfessorNaoForDono() throws Exception {
        mockMvc.perform(get("/aulas/{idAula}/presencas", aula.getId())
                        .header("Authorization", "Bearer " + tokenOutroProfessor)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornar409_AoAtualizarPresenca_QuandoProfessorNaoForDono() throws Exception {
        var request = new PresencaRequest(true);

        mockMvc.perform(patch("/aulas/{idAula}/presencas/inscricao/{idInscricao}", aula.getId(), inscricao.getId())
                        .header("Authorization", "Bearer " + tokenOutroProfessor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornar409_AoAtualizarPresenca_QuandoStatusJaFoiRegistradoIgual() throws Exception {
        presencaRepository.save(Presenca.builder()
                .id(UUID.randomUUID())
                .inscricao(inscricao)
                .presente(true)
                .dataRegistro(LocalDate.now())
                .build());

        var request = new PresencaRequest(true);

        mockMvc.perform(patch("/aulas/{idAula}/presencas/inscricao/{idInscricao}", aula.getId(), inscricao.getId())
                        .header("Authorization", "Bearer " + tokenProfessor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornar200_AoAtualizarPresenca_QuandoUsuarioForAdmin() throws Exception {
        Endereco endereco = Endereco.builder().cep("01000-000").logradouro("Rua").numero("1").bairro("B").cidade("C").estado("SP").build();
        Usuario admin = usuarioRepository.save(Usuario.builder()
                .id(UUID.randomUUID())
                .nome("Admin Teste")
                .email("admin.test@example.com")
                .cpf("888.888.888-88")
                .telefone("(11)98888-0000")
                .role(com.fighthub.model.enums.Role.ADMIN)
                .ativo(true)
                .senha("senha")
                .endereco(endereco)
                .build());

        String tokenAdmin = jwtService.gerarToken(admin);
        tokenService.salvarAccessToken(admin, tokenAdmin);

        var request = new PresencaRequest(true);

        mockMvc.perform(patch("/aulas/{idAula}/presencas/inscricao/{idInscricao}", aula.getId(), inscricao.getId())
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void deveRetornar403_AoListarMinhasPresencas_QuandoUsuarioNaoForAluno() throws Exception {
        mockMvc.perform(get("/aulas/me/presencas")
                        .header("Authorization", "Bearer " + tokenProfessor)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveRetornar200_AoListarMinhasPresencas_QuandoExistirem() throws Exception {
        presencaRepository.save(Presenca.builder()
                .id(UUID.randomUUID())
                .inscricao(inscricao)
                .presente(true)
                .dataRegistro(LocalDate.now())
                .build());

        mockMvc.perform(get("/aulas/me/presencas")
                        .header("Authorization", "Bearer " + tokenAluno)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()", is(1)));
    }

    @Test
    void deveRetornar200_AoListarMinhasPresencas_QuandoSemInscricoesRetornaVazio() throws Exception {
        Usuario novoAluno = usuarioRepository.save(Usuario.builder()
                .id(UUID.randomUUID())
                .nome("Aluno Sem Inscricao")
                .email("aluno.sem@example.com")
                .cpf("777.777.777-77")
                .telefone("(11)94444-4444")
                .role(Role.ALUNO)
                .ativo(true)
                .senha("senha")
                .endereco(Endereco.builder().cep("01000-000").logradouro("Rua").numero("1").bairro("B").cidade("C").estado("SP").build())
                .build());
        alunoRepository.save(Aluno.builder()
                .id(UUID.randomUUID())
                .usuario(novoAluno)
                .dataNascimento(LocalDate.of(2006,2,2))
                .dataMatricula(LocalDate.now())
                .matriculaAtiva(true)
                .build());

        String token = jwtService.gerarToken(novoAluno);
        tokenService.salvarAccessToken(novoAluno, token);

        mockMvc.perform(get("/aulas/me/presencas")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void deveRetornar401_ParaEndpoints_QuandoSemToken() throws Exception {
        mockMvc.perform(get("/aulas/{idAula}/presencas", aula.getId()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(patch("/aulas/{idAula}/presencas/inscricao/{idInscricao}", aula.getId(), inscricao.getId()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/aulas/me/presencas"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar200_AoListarPresencasPorAula_CamposDTOPreenchidos() throws Exception {
        presencaRepository.save(Presenca.builder()
                .id(UUID.randomUUID())
                .inscricao(inscricao)
                .presente(true)
                .dataRegistro(LocalDate.now())
                .build());

        mockMvc.perform(get("/aulas/{idAula}/presencas", aula.getId())
                        .header("Authorization", "Bearer " + tokenProfessor)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").isNotEmpty())
                .andExpect(jsonPath("$.content[0].inscricaoId").value(inscricao.getId().toString()))
                .andExpect(jsonPath("$.content[0].alunoId").value(aluno.getId().toString()))
                .andExpect(jsonPath("$.content[0].alunoNome").value(alunoUsuario.getNome()))
                .andExpect(jsonPath("$.content[0].aulaId").value(aula.getId().toString()))
                .andExpect(jsonPath("$.content[0].aulaTitulo").value(aula.getTitulo()))
                .andExpect(jsonPath("$.content[0].dataRegistro").isNotEmpty());
    }

    @Test
    void deveAtualizarPresencaExistente_DeFalseParaTrue_Retorna200() throws Exception {
        presencaRepository.save(Presenca.builder()
                .id(UUID.randomUUID())
                .inscricao(inscricao)
                .presente(false)
                .dataRegistro(LocalDate.now())
                .build());

        var request = new PresencaRequest(true);

        mockMvc.perform(patch("/aulas/{idAula}/presencas/inscricao/{idInscricao}", aula.getId(), inscricao.getId())
                        .header("Authorization", "Bearer " + tokenProfessor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        var saved = presencaRepository.findAll();
        assertFalse(saved.isEmpty());
        assertTrue(saved.get(0).isPresente());
    }

    @Test
    void deveRetornar409_AoAtualizarPresenca_QuandoInscricaoNaoPertenceAAula() throws Exception {
        Turma outraTurma = turmaRepository.save(Turma.builder()
                .nome("Outra Turma")
                .horario("21:00")
                .ativo(true)
                .professor(professor)
                .build());
        Aula outraAula = aulaRepository.save(Aula.builder()
                .data(LocalDateTime.now().plusDays(2))
                .titulo("Outra Aula")
                .turma(outraTurma)
                .build());

        inscricao.setAula(outraAula);
        inscricaoRepository.save(inscricao);

        var request = new PresencaRequest(true);

        mockMvc.perform(patch("/aulas/{idAula}/presencas/inscricao/{idInscricao}", aula.getId(), inscricao.getId())
                        .header("Authorization", "Bearer " + tokenProfessor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornarPaginaCorreta_QuandoExistemMaisPresencasQuePageSize() throws Exception {
        for (int i = 0; i < 15; i++) {
            Usuario u = usuarioRepository.save(Usuario.builder()
                    .id(UUID.randomUUID())
                    .nome("Aluno " + i)
                    .email("aluno" + i + "@example.com")
                    .cpf(String.format("%03d.%03d.%03d-00", i, i, i))
                    .telefone("(11)90000-0" + i)
                    .role(Role.ALUNO)
                    .ativo(true)
                    .senha("senha")
                    .endereco(Endereco.builder().cep("01000-000").logradouro("Rua").numero("1").bairro("B").cidade("C").estado("SP").build())
                    .build());

            Aluno a = alunoRepository.save(Aluno.builder()
                    .id(UUID.randomUUID())
                    .usuario(u)
                    .dataNascimento(LocalDate.of(2000,1,1))
                    .dataMatricula(LocalDate.now())
                    .matriculaAtiva(true)
                    .build());

            Inscricao ins = inscricaoRepository.save(Inscricao.builder()
                    .id(UUID.randomUUID())
                    .aula(aula)
                    .aluno(a)
                    .inscritoEm(LocalDateTime.now())
                    .status(SubscriptionStatus.INSCRITO)
                    .build());

            presencaRepository.save(Presenca.builder()
                    .id(UUID.randomUUID())
                    .inscricao(ins)
                    .presente(i % 2 == 0)
                    .dataRegistro(LocalDate.now())
                    .build());
        }

        mockMvc.perform(get("/aulas/{idAula}/presencas", aula.getId())
                        .header("Authorization", "Bearer " + tokenProfessor)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(15)))
                .andExpect(jsonPath("$.content", hasSize(10)));
    }
}