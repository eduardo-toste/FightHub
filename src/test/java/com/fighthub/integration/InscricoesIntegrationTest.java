package com.fighthub.integration;

import com.fighthub.model.*;
import com.fighthub.model.enums.ClassStatus;
import com.fighthub.model.enums.Role;
import com.fighthub.model.enums.SubscriptionStatus;
import com.fighthub.repository.InscricaoRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class InscricoesIntegrationTest extends IntegrationTestBase {

    @Autowired
    private JwtService jwtService;

    @SpyBean
    private TokenService tokenService;

    @Autowired
    private InscricaoRepository inscricaoRepository;

    private Usuario admin;
    private Usuario alunoUsuario;
    private Aluno aluno;
    private Aula aula;
    private String tokenAdmin;
    private String tokenAluno;

    @BeforeEach
    void setupInscricoes() {
        Endereco endereco = Endereco.builder()
                .cep("01000-000")
                .logradouro("Rua Teste")
                .numero("123")
                .bairro("Centro")
                .cidade("São Paulo")
                .estado("SP")
                .build();

        admin = usuarioRepository.save(Usuario.builder()
                .id(UUID.randomUUID())
                .nome("Admin User")
                .email("admin@email.com")
                .cpf("111.111.111-11")
                .telefone("(11)99999-0000")
                .role(Role.ADMIN)
                .ativo(true)
                .senha("123456")
                .endereco(endereco)
                .build());

        alunoUsuario = usuarioRepository.save(Usuario.builder()
                .id(UUID.randomUUID())
                .nome("Aluno User")
                .email("aluno@email.com")
                .cpf("222.222.222-22")
                .telefone("(11)98888-0000")
                .role(Role.ALUNO)
                .ativo(true)
                .senha("123456")
                .endereco(endereco)
                .build());

        aluno = alunoRepository.save(Aluno.builder()
                .id(UUID.randomUUID())
                .usuario(alunoUsuario)
                .dataNascimento(LocalDate.of(2000, 1, 1))
                .dataMatricula(LocalDate.now())
                .matriculaAtiva(true)
                .build());

        // make aula far enough in the future and available so inscriptions work in tests
        aula = aulaRepository.save(Aula.builder()
                .id(UUID.randomUUID())
                .titulo("Aula Teste")
                .data(LocalDateTime.now().plusHours(2))
                .status(ClassStatus.DISPONIVEL)
                .build());

        tokenAdmin = jwtService.gerarToken(admin);
        tokenAluno = jwtService.gerarToken(alunoUsuario);

        tokenService.salvarAccessToken(admin, tokenAdmin);
        tokenService.salvarAccessToken(alunoUsuario, tokenAluno);
    }

    @Test
    void deveInscreverAlunoComSucesso() throws Exception {
        mockMvc.perform(post("/aulas/{idAula}/inscricoes", aula.getId())
                        .header("Authorization", "Bearer " + tokenAluno)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        var opt = inscricaoRepository.findByAulaAndAluno(aula, aluno);
        assertTrue(opt.isPresent());
        assertEquals(SubscriptionStatus.INSCRITO, opt.get().getStatus());
    }

    @Test
    void deveRetornar409_AoInscreverQuandoJaInscrito() throws Exception {
        inscricaoRepository.save(new Inscricao(aluno, aula, SubscriptionStatus.INSCRITO, LocalDateTime.now()));

        mockMvc.perform(post("/aulas/{idAula}/inscricoes", aula.getId())
                        .header("Authorization", "Bearer " + tokenAluno)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornar403_AoInscrever_QuandoUsuarioNaoForAluno() throws Exception {
        mockMvc.perform(post("/aulas/{idAula}/inscricoes", aula.getId())
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveRetornar401_AoInscrever_SemToken() throws Exception {
        mockMvc.perform(post("/aulas/{idAula}/inscricoes", aula.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveCancelarInscricaoComSucesso() throws Exception {
        inscricaoRepository.save(new Inscricao(aluno, aula, SubscriptionStatus.INSCRITO, LocalDateTime.now()));

        mockMvc.perform(delete("/aulas/{idAula}/inscricoes", aula.getId())
                        .header("Authorization", "Bearer " + tokenAluno))
                .andExpect(status().isNoContent());

        var opt = inscricaoRepository.findByAulaAndAluno(aula, aluno);
        assertTrue(opt.isPresent());
        assertEquals(SubscriptionStatus.CANCELADO, opt.get().getStatus());
    }

    @Test
    void deveRetornar200_AoBuscarInscricoesPorAula_ComPermissao() throws Exception {
        inscricaoRepository.save(new Inscricao(aluno, aula, SubscriptionStatus.INSCRITO, LocalDateTime.now()));

        mockMvc.perform(get("/aulas/{idAula}/inscricoes", aula.getId())
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void deveRetornar404_AoInscrever_QuandoAulaInexistente() throws Exception {
        mockMvc.perform(post("/aulas/{idAula}/inscricoes", UUID.randomUUID())
                        .header("Authorization", "Bearer " + tokenAluno)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar409_AoCancelar_QuandoSemInscricao() throws Exception {
        mockMvc.perform(delete("/aulas/{idAula}/inscricoes", aula.getId())
                        .header("Authorization", "Bearer " + tokenAluno))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornar404_AoBuscarInscricoesPorAula_Inexistente() throws Exception {
        mockMvc.perform(get("/aulas/{idAula}/inscricoes", UUID.randomUUID())
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isNotFound());
    }

    @Test
    void devePermitirBuscarInscricoesPorAula_ParaProfessorECoordenador() throws Exception {
        Usuario professor = usuarioRepository.save(Usuario.builder()
                .id(UUID.randomUUID())
                .nome("Prof User")
                .email("prof@example.com")
                .cpf("333.333.333-33")
                .telefone("(11)97777-0000")
                .role(Role.PROFESSOR)
                .ativo(true)
                .senha("123456")
                .endereco(Endereco.builder().cep("00000-000").logradouro("Rua").numero("1").bairro("B").cidade("C").estado("SP").build())
                .build());

        Usuario coordenador = usuarioRepository.save(Usuario.builder()
                .id(UUID.randomUUID())
                .nome("Coord User")
                .email("coord@example.com")
                .cpf("444.444.444-44")
                .telefone("(11)96666-0000")
                .role(Role.COORDENADOR)
                .ativo(true)
                .senha("123456")
                .endereco(Endereco.builder().cep("00000-000").logradouro("Rua").numero("1").bairro("B").cidade("C").estado("SP").build())
                .build());

        String tokenProf = jwtService.gerarToken(professor);
        String tokenCoord = jwtService.gerarToken(coordenador);
        tokenService.salvarAccessToken(professor, tokenProf);
        tokenService.salvarAccessToken(coordenador, tokenCoord);

        inscricaoRepository.save(new Inscricao(aluno, aula, SubscriptionStatus.INSCRITO, LocalDateTime.now()));

        mockMvc.perform(get("/aulas/{idAula}/inscricoes", aula.getId())
                        .header("Authorization", "Bearer " + tokenProf)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/aulas/{idAula}/inscricoes", aula.getId())
                        .header("Authorization", "Bearer " + tokenCoord)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void deveRetornar200_AoBuscarInscricoesProprias_ComPermissao() throws Exception {
        inscricaoRepository.save(new Inscricao(aluno, aula, SubscriptionStatus.INSCRITO, LocalDateTime.now()));

        mockMvc.perform(get("/aulas/inscricoes/minhas")
                        .header("Authorization", "Bearer " + tokenAluno)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void deveRetornar401_AoBuscarInscricoesProprias_SemToken() throws Exception {
        mockMvc.perform(get("/aulas/inscricoes/minhas")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar500_ParaUUIDInvalido_AoInscrever() throws Exception {
        mockMvc.perform(post("/aulas/{idAula}/inscricoes", "invalid-uuid")
                        .header("Authorization", "Bearer " + tokenAluno)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void deveRetornar409_AoCancelar_QuandoAlunoDiferenteTentaCancelar() throws Exception {
        Usuario alunoUsuario2 = usuarioRepository.save(Usuario.builder()
                .id(UUID.randomUUID())
                .nome("Outro Aluno")
                .email("outro@example.com")
                .cpf("555.555.555-55")
                .telefone("(11)95555-0000")
                .role(Role.ALUNO)
                .ativo(true)
                .senha("123456")
                .endereco(Endereco.builder().cep("00000-000").logradouro("Rua").numero("2").bairro("B").cidade("C").estado("SP").build())
                .build());

        Aluno aluno2 = alunoRepository.save(Aluno.builder()
                .id(UUID.randomUUID())
                .usuario(alunoUsuario2)
                .dataNascimento(LocalDate.of(2001, 2, 2))
                .dataMatricula(LocalDate.now())
                .matriculaAtiva(true)
                .build());

        String tokenAluno2 = jwtService.gerarToken(alunoUsuario2);
        tokenService.salvarAccessToken(alunoUsuario2, tokenAluno2);

        inscricaoRepository.save(new Inscricao(aluno, aula, SubscriptionStatus.INSCRITO, LocalDateTime.now()));

        mockMvc.perform(delete("/aulas/{idAula}/inscricoes", aula.getId())
                        .header("Authorization", "Bearer " + tokenAluno2))
                .andExpect(status().isConflict());
    }

    @Test
    void deveReativarInscricaoCanceladaComSucesso() throws Exception {
        // existing canceled inscription
        inscricaoRepository.save(new Inscricao(aluno, aula, SubscriptionStatus.CANCELADO, LocalDateTime.of(2020, 1, 1, 0, 0)));

        mockMvc.perform(post("/aulas/{idAula}/inscricoes", aula.getId())
                        .header("Authorization", "Bearer " + tokenAluno)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        var opt = inscricaoRepository.findByAulaAndAluno(aula, aluno);
        assertTrue(opt.isPresent());
        assertEquals(SubscriptionStatus.INSCRITO, opt.get().getStatus());
    }

    @Test
    void deveBloquearReativacao_QuandoInscricoesEncerradas() throws Exception {
        Aula aulaSoon = aulaRepository.save(Aula.builder()
                .id(UUID.randomUUID())
                .titulo("Aula Soon")
                .data(LocalDateTime.now().plusMinutes(30))
                .status(ClassStatus.DISPONIVEL)
                .build());

        inscricaoRepository.save(new Inscricao(aluno, aulaSoon, SubscriptionStatus.CANCELADO, LocalDateTime.of(2020, 1, 1, 0, 0)));

        mockMvc.perform(post("/aulas/{idAula}/inscricoes", aulaSoon.getId())
                        .header("Authorization", "Bearer " + tokenAluno)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornar409_AoInscrever_QuandoAulaNaoDisponivel() throws Exception {
        Aula aulaIndisponivel = aulaRepository.save(Aula.builder()
                .id(UUID.randomUUID())
                .titulo("Indisponivel")
                .data(LocalDateTime.now().plusHours(2))
                .status(ClassStatus.CANCELADA)
                .build());

        mockMvc.perform(post("/aulas/{idAula}/inscricoes", aulaIndisponivel.getId())
                        .header("Authorization", "Bearer " + tokenAluno)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }
}