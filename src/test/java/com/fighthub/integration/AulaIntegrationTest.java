package com.fighthub.integration;

import com.fighthub.dto.aula.AulaRequest;
import com.fighthub.dto.aula.AulaUpdateCompletoRequest;
import com.fighthub.dto.aula.AulaUpdateStatusRequest;
import com.fighthub.model.Aula;
import com.fighthub.model.Endereco;
import com.fighthub.model.Turma;
import com.fighthub.model.Usuario;
import com.fighthub.model.enums.ClassStatus;
import com.fighthub.model.enums.Role;
import com.fighthub.service.JwtService;
import com.fighthub.service.TokenService;
import com.fighthub.utils.IntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AulaIntegrationTest extends IntegrationTestBase {

    @SpyBean private TokenService tokenService;
    @Autowired private JwtService jwtService;

    private Usuario usuario;
    private Turma turma;
    private Aula aula;
    private String accessToken;

    @BeforeEach
    void setup() {
        Endereco endereco = Endereco.builder()
                .cep("12345-678")
                .logradouro("Rua das Flores")
                .numero("123")
                .bairro("Centro")
                .cidade("São Paulo")
                .estado("SP")
                .build();

        usuario = usuarioRepository.save(Usuario.builder()
                .nome("Usuário Teste")
                .email("usuario@email.com")
                .cpf("935.449.680-61")
                .telefone("(11)98888-0000")
                .role(Role.ADMIN)
                .ativo(true)
                .loginSocial(false)
                .endereco(endereco)
                .build());

        accessToken = jwtService.gerarToken(usuario);
        tokenService.salvarAccessToken(usuario, accessToken);

        turma = turmaRepository.save(Turma.builder()
                .nome("Turma de Teste")
                .horario("Segunda 19:00")
                .ativo(true)
                .alunos(new ArrayList<>())
                .build());

        aula = aulaRepository.save(Aula.builder()
                .titulo("Aula Inicial")
                .descricao("Descrição da Aula")
                .data(LocalDateTime.now().plusDays(1))
                .status(ClassStatus.DISPONIVEL)
                .limiteAlunos(20)
                .ativo(true)
                .turma(turma)
                .build());
    }

    @Test
    void deveCriarAulaComSucesso() throws Exception {
        AulaRequest request = new AulaRequest("Nova Aula", "Conteúdo", LocalDateTime.now().plusDays(5), turma.getId(), 15);

        mockMvc.perform(post("/aulas")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void deveCriarAulaSemTurma() throws Exception {
        AulaRequest request = new AulaRequest("Aula Solta", "Sem turma", LocalDateTime.now().plusDays(3), null, 10);

        mockMvc.perform(post("/aulas")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void deveListarAulasComSucesso() throws Exception {
        mockMvc.perform(get("/aulas")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].titulo", is(aula.getTitulo())));
    }

    @Test
    void deveBuscarAulaPorIdComSucesso() throws Exception {
        mockMvc.perform(get("/aulas/{id}", aula.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo", is(aula.getTitulo())));
    }

    @Test
    void deveRetornar404_QuandoBuscarAulaInexistente() throws Exception {
        mockMvc.perform(get("/aulas/{id}", UUID.randomUUID())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveAtualizarAulaComSucesso() throws Exception {
        AulaUpdateCompletoRequest request = new AulaUpdateCompletoRequest(
                "Aula Atualizada", "Nova descrição", LocalDateTime.now().plusDays(2), turma.getId(), 30, true);

        mockMvc.perform(put("/aulas/{id}", aula.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo", is("Aula Atualizada")))
                .andExpect(jsonPath("$.descricao", is("Nova descrição")));
    }

    @Test
    void deveAtualizarStatusAulaComSucesso() throws Exception {
        AulaUpdateStatusRequest request = new AulaUpdateStatusRequest(ClassStatus.CANCELADA);

        mockMvc.perform(patch("/aulas/{id}/status", aula.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELADA")));
    }

    @Test
    void deveExcluirAulaComSucesso() throws Exception {
        mockMvc.perform(delete("/aulas/{id}", aula.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        assertFalse(aulaRepository.findById(aula.getId()).isPresent());
    }

    @Test
    void deveRetornar404_AoExcluirAulaInexistente() throws Exception {
        mockMvc.perform(delete("/aulas/{id}", UUID.randomUUID())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveVincularTurmaComSucesso() throws Exception {
        Turma novaTurma = turmaRepository.save(Turma.builder()
                .nome("Turma X")
                .horario("Quarta 18:00")
                .ativo(true)
                .build());

        mockMvc.perform(patch("/aulas/{idAula}/turmas/{idTurma}", aula.getId(), novaTurma.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    void deveDesvincularTurmaComSucesso() throws Exception {
        mockMvc.perform(delete("/aulas/{idAula}/turmas/{idTurma}", aula.getId(), turma.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        var atualizada = aulaRepository.findById(aula.getId()).orElseThrow();
        assertNull(atualizada.getTurma());
    }

    @Test
    void deveRetornar404_AoVincularTurmaInexistente() throws Exception {
        mockMvc.perform(patch("/aulas/{idAula}/turmas/{idTurma}", aula.getId(), UUID.randomUUID())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar404_AoDesvincularTurmaInexistente() throws Exception {
        mockMvc.perform(delete("/aulas/{idAula}/turmas/{idTurma}", aula.getId(), UUID.randomUUID())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveListarAulasDisponiveisParaAlunoComSucesso() throws Exception {
        Endereco enderecoAluno = Endereco.builder()
                .cep("22222-222")
                .logradouro("Rua do Aluno")
                .numero("45")
                .bairro("Bairro")
                .cidade("Cidade")
                .estado("SP")
                .build();

        Usuario usuarioAluno = usuarioRepository.save(Usuario.builder()
                .nome("Aluno Teste")
                .email("aluno@email.com")
                .cpf("222.222.222-22")
                .telefone("(11)97777-0000")
                .role(Role.ALUNO)
                .ativo(true)
                .loginSocial(false)
                .endereco(enderecoAluno)
                .build());

        com.fighthub.model.Aluno aluno = com.fighthub.model.Aluno.builder()
                .usuario(usuarioAluno)
                .dataMatricula(java.time.LocalDate.now())
                .dataNascimento(java.time.LocalDate.now().minusYears(18))
                .matriculaAtiva(true)
                .responsaveis(new java.util.ArrayList<>())
                .build();
        aluno = alunoRepository.save(aluno);

        turma.getAlunos().add(aluno);
        turmaRepository.save(turma);
        alunoRepository.save(aluno);

        Aula aulaAluno = aulaRepository.save(Aula.builder()
                .titulo("Aula para Aluno")
                .descricao("Descrição")
                .data(java.time.LocalDateTime.now().plusDays(2))
                .status(ClassStatus.DISPONIVEL)
                .limiteAlunos(10)
                .ativo(true)
                .turma(turma)
                .build());

        String tokenAluno = jwtService.gerarToken(usuarioAluno);
        tokenService.salvarAccessToken(usuarioAluno, tokenAluno);

        mockMvc.perform(get("/aulas/alunos")
                        .header("Authorization", "Bearer " + tokenAluno)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].titulo", hasItem(is(aulaAluno.getTitulo()))));
    }

    @Test
    void deveListarAulasDisponiveisParaProfessorComSucesso() throws Exception {
        Endereco enderecoProf = Endereco.builder()
                .cep("33333-333")
                .logradouro("Rua do Professor")
                .numero("99")
                .bairro("Centro")
                .cidade("Cidade")
                .estado("SP")
                .build();

        Usuario usuarioProf = usuarioRepository.save(Usuario.builder()
                .nome("Professor Teste")
                .email("professor@email.com")
                .cpf("333.333.333-33")
                .telefone("(11)96666-0000")
                .role(Role.PROFESSOR)
                .ativo(true)
                .loginSocial(false)
                .endereco(enderecoProf)
                .build());

        com.fighthub.model.Professor professor = com.fighthub.model.Professor.builder()
                .usuario(usuarioProf)
                .build();
        professor = professorRepository.save(professor);

        turma.setProfessor(professor);
        turmaRepository.save(turma);
        professorRepository.save(professor);

        Aula aulaProf = aulaRepository.save(Aula.builder()
                .titulo("Aula do Professor")
                .descricao("Descrição")
                .data(java.time.LocalDateTime.now().plusDays(3))
                .status(ClassStatus.DISPONIVEL)
                .limiteAlunos(15)
                .ativo(true)
                .turma(turma)
                .build());

        String tokenProf = jwtService.gerarToken(usuarioProf);
        tokenService.salvarAccessToken(usuarioProf, tokenProf);

        mockMvc.perform(get("/aulas/professores")
                        .header("Authorization", "Bearer " + tokenProf)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].titulo", hasItem(is(aulaProf.getTitulo()))));
    }

    @Test
    void deveNegarCriacaoAulaParaAluno() throws Exception {
        Endereco endereco = Endereco.builder().cep("11111-111").logradouro("R").numero("1").bairro("B").cidade("C").estado("SP").build();
        Usuario aluno = usuarioRepository.save(Usuario.builder()
                .nome("Aluno")
                .email("aluno2@email.com")
                .cpf("111.111.111-11")
                .telefone("1111")
                .role(Role.ALUNO)
                .ativo(true)
                .loginSocial(false)
                .endereco(endereco)
                .build());

        String token = jwtService.gerarToken(aluno);
        tokenService.salvarAccessToken(aluno, token);

        AulaRequest request = new AulaRequest("Titulo", "Desc", LocalDateTime.now().plusDays(2), null, 10);

        mockMvc.perform(post("/aulas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveRetornar401_QuandoTokenAusente() throws Exception {
        mockMvc.perform(get("/aulas"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar401_QuandoTokenRevogado() throws Exception {
        Endereco endereco = Endereco.builder().cep("33333-333").logradouro("R").numero("3").bairro("B").cidade("C").estado("SP").build();
        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .nome("UsuarioRevogado")
                .email("revogado@email.com")
                .cpf("333.333.333-33")
                .telefone("3333")
                .role(Role.ADMIN)
                .ativo(true)
                .loginSocial(false)
                .endereco(endereco)
                .build());

        String token = jwtService.gerarToken(usuario);
        tokenService.salvarAccessToken(usuario, token);

        tokenRepository.findByToken(token).ifPresent(t -> {
            try {
                t.setExpired(true);
                t.setRevoked(true);
                tokenRepository.save(t);
            } catch (Exception ignored) {}
        });

        mockMvc.perform(get("/aulas")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar400_QuandoCriarAulaComDadosInvalidos() throws Exception {
        Endereco endereco = Endereco.builder().cep("44444-444").logradouro("R").numero("4").bairro("B").cidade("C").estado("SP").build();
        Usuario admin = usuarioRepository.save(Usuario.builder()
                .nome("AdminVal")
                .email("adminval@email.com")
                .cpf("444.444.444-44")
                .telefone("4444")
                .role(Role.ADMIN)
                .ativo(true)
                .loginSocial(false)
                .endereco(endereco)
                .build());

        String token = jwtService.gerarToken(admin);
        tokenService.salvarAccessToken(admin, token);

        AulaRequest invalid = new AulaRequest("", "Desc", LocalDateTime.now().minusDays(1), null, -5);

        mockMvc.perform(post("/aulas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar409_QuandoVincularTurmaJaVinculada() throws Exception {
        Endereco endereco = Endereco.builder().cep("55555-555").logradouro("R").numero("5").bairro("B").cidade("C").estado("SP").build();
        Usuario admin = usuarioRepository.save(Usuario.builder()
                .nome("Admin409")
                .email("admin409@email.com")
                .cpf("555.555.555-55")
                .telefone("5555")
                .role(Role.ADMIN)
                .ativo(true)
                .loginSocial(false)
                .endereco(endereco)
                .build());

        String token = jwtService.gerarToken(admin);
        tokenService.salvarAccessToken(admin, token);

        Turma turma = turmaRepository.save(Turma.builder()
                .nome("Turma409")
                .horario("Sexta 10:00")
                .ativo(true)
                .build());

        var aula = aulaRepository.save(com.fighthub.model.Aula.builder()
                .titulo("Aula409")
                .descricao("D")
                .data(LocalDateTime.now().plusDays(2))
                .status(com.fighthub.model.enums.ClassStatus.DISPONIVEL)
                .limiteAlunos(10)
                .ativo(true)
                .turma(turma)
                .build());

        mockMvc.perform(patch("/aulas/{idAula}/turmas/{idTurma}", aula.getId(), turma.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornar409_QuandoDesvincularTurmaNaoVinculada() throws Exception {
        Endereco endereco = Endereco.builder().cep("66666-666").logradouro("R").numero("6").bairro("B").cidade("C").estado("SP").build();
        Usuario admin = usuarioRepository.save(Usuario.builder()
                .nome("Admin409b")
                .email("admin409b@email.com")
                .cpf("666.666.666-66")
                .telefone("6666")
                .role(Role.ADMIN)
                .ativo(true)
                .loginSocial(false)
                .endereco(endereco)
                .build());

        String token = jwtService.gerarToken(admin);
        tokenService.salvarAccessToken(admin, token);

        Turma turmaA = turmaRepository.save(Turma.builder().nome("A").horario("X").ativo(true).build());
        Turma turmaB = turmaRepository.save(Turma.builder().nome("B").horario("Y").ativo(true).build());

        var aula = aulaRepository.save(com.fighthub.model.Aula.builder()
                .titulo("AulaNotLinked")
                .descricao("D")
                .data(LocalDateTime.now().plusDays(3))
                .status(com.fighthub.model.enums.ClassStatus.DISPONIVEL)
                .limiteAlunos(12)
                .ativo(true)
                .turma(turmaA)
                .build());

        mockMvc.perform(delete("/aulas/{idAula}/turmas/{idTurma}", aula.getId(), turmaB.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornarPaginaComMetadadosCorretos() throws Exception {
        Endereco endereco = Endereco.builder().cep("77777-777").logradouro("R").numero("7").bairro("B").cidade("C").estado("SP").build();
        Usuario admin = usuarioRepository.save(Usuario.builder()
                .nome("AdminPage")
                .email("adminpage@email.com")
                .cpf("777.777.777-77")
                .telefone("7777")
                .role(Role.ADMIN)
                .ativo(true)
                .loginSocial(false)
                .endereco(endereco)
                .build());

        String token = jwtService.gerarToken(admin);
        tokenService.salvarAccessToken(admin, token);

        for (int i = 0; i < 3; i++) {
            aulaRepository.save(Aula.builder()
                    .titulo("PgAula" + i)
                    .descricao("D")
                    .data(LocalDateTime.now().plusDays(1 + i))
                    .status(ClassStatus.DISPONIVEL)
                    .limiteAlunos(10)
                    .ativo(true)
                    .build());
        }

        mockMvc.perform(get("/aulas")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "0")
                        .param("size", "2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(4)))
                .andExpect(jsonPath("$.size", is(2)))
                .andExpect(jsonPath("$.totalPages", is(2)))
                .andExpect(jsonPath("$.content[*].titulo", hasItem("PgAula0")));
    }
}
