package com.fighthub.integration;

import com.fighthub.dto.aluno.AlunoUpdateDataMatriculaRequest;
import com.fighthub.dto.aluno.AlunoUpdateDataNascimentoRequest;
import com.fighthub.dto.aluno.AlunoUpdateMatriculaRequest;
import com.fighthub.dto.aluno.CriarAlunoRequest;
import com.fighthub.model.Aluno;
import com.fighthub.model.Endereco;
import com.fighthub.model.GraduacaoAluno;
import com.fighthub.model.Usuario;
import com.fighthub.model.enums.BeltGraduation;
import com.fighthub.model.enums.GraduationLevel;
import com.fighthub.model.enums.Role;
import com.fighthub.service.EmailService;
import com.fighthub.service.JwtService;
import com.fighthub.service.TokenService;
import com.fighthub.utils.IntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AlunoIntegrationTest extends IntegrationTestBase {

    @SpyBean private TokenService tokenService;
    @SpyBean private EmailService emailService;
    @Autowired private JwtService jwtService;

    private Usuario usuario;
    private String accessToken;

    @BeforeEach
    void setup() {
        Endereco endereco = Endereco.builder()
                .cep("12345-678")
                .logradouro("Rua das Flores")
                .numero("123")
                .complemento("Apto 45")
                .bairro("Centro")
                .cidade("São Paulo")
                .estado("SP")
                .build();

        usuario = usuarioRepository.save(Usuario.builder()
                .id(UUID.randomUUID())
                .nome("Usuário Teste")
                .email("usuario@email.com")
                .senha(null)
                .foto(null)
                .role(Role.ADMIN)
                .loginSocial(false)
                .ativo(true)
                .telefone("(11)98888-0000")
                .cpf("935.449.680-61")
                .endereco(endereco)
                .build());

        accessToken = jwtService.gerarToken(usuario);
        tokenService.salvarAccessToken(usuario, accessToken);
    }

    @Test
    void deveCriarAlunoComDadosValidos() throws Exception {
        var request = new CriarAlunoRequest(
                "João",
                "joao@email.com",
                "107.031.010-72",
                LocalDate.now().minusYears(17),
                List.of(UUID.randomUUID())
        );

        mockMvc.perform(post("/alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        var usuarioSalvo = usuarioRepository.findByEmail("joao@email.com");
        assertTrue(usuarioSalvo.isPresent());

        var alunoSalvo = alunoRepository.findByUsuarioId(usuarioSalvo.get().getId());
        assertTrue(alunoSalvo.isPresent());

        verify(tokenService).salvarTokenAtivacao(usuarioSalvo.get());
        verify(emailService).enviarEmailAtivacao(eq(usuarioSalvo.get()), anyString());
    }

    @Test
    void deveRetornar400_AoCriarAluno_QuandoDadosForemInvalidos() throws Exception {
        var request = new CriarAlunoRequest(
                "",
                "",
                "111.111.111-22",
                LocalDate.now().minusYears(17),
                List.of(UUID.randomUUID())
        );

        mockMvc.perform(post("/alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar401_AoCriarAluno_QuandoNaoAutenticado() throws Exception {
        var request = new CriarAlunoRequest(
                "João",
                "joao@email.com",
                "107.031.010-72",
                LocalDate.now().minusYears(17),
                List.of(UUID.randomUUID())
        );

        mockMvc.perform(post("/alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar403_AoCriarAluno_QuandoUsuarioSemPermissao() throws Exception {
        var usuarioAluno = usuarioRepository.save(
                Usuario.builder()
                        .nome("Aluno")
                        .email("aluno@email.com")
                        .cpf("302.514.990-10")
                        .role(Role.ALUNO)
                        .ativo(true)
                        .build()
        );

        var tokenAluno = jwtService.gerarToken(usuarioAluno);
        tokenService.salvarAccessToken(usuarioAluno, tokenAluno);

        var request = new CriarAlunoRequest(
                "João",
                "joao@email.com",
                "107.031.010-72",
                LocalDate.now().minusYears(17),
                List.of(UUID.randomUUID())
        );

        mockMvc.perform(post("/alunos")
                        .header("Authorization", "Bearer " + tokenAluno)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveRetornar409_AoCriarAluno_QuandoForMenorSemResponsavel() throws Exception {
        var request = new CriarAlunoRequest(
                "João",
                "joao@email.com",
                "107.031.010-72",
                LocalDate.now().minusYears(15),
                List.of()
        );

        mockMvc.perform(post("/alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornar409_AoCriarAluno_QuandoEmailJaCadastrado() throws Exception {
        usuarioRepository.save(usuario);
        var request = new CriarAlunoRequest(
                "João",
                "usuario@email.com",
                "107.031.010-72",
                LocalDate.now().minusYears(20),
                List.of()
        );

        mockMvc.perform(post("/alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornar409_aoCriarAluno_quandoCpfJaCadastrado() throws Exception {
        usuarioRepository.save(usuario);
        var request = new CriarAlunoRequest(
                "João",
                "usuario@email.com",
                "935.449.680-61",
                LocalDate.now().minusYears(20),
                List.of()
        );

        mockMvc.perform(post("/alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornarPageDeAlunosComConteudoCorreto() throws Exception {
        // Arrange
        Usuario u1 = usuarioRepository.save(Usuario.builder()
                .nome("Aluno Um")
                .email("um@email.com")
                .cpf("123.456.789-00")
                .telefone("(11)98888-0001")
                .role(Role.ALUNO)
                .ativo(true)
                .loginSocial(false)
                .senha("123456")
                .build());

        alunoRepository.save(new Aluno(
                UUID.randomUUID(),
                u1,
                LocalDate.now().minusYears(20),
                LocalDate.now(),
                true,
                List.of(),
                List.of(),
                new GraduacaoAluno(
                        BeltGraduation.BRANCA,
                        GraduationLevel.ZERO
                )
        ));

        Usuario u2 = usuarioRepository.save(Usuario.builder()
                .nome("Aluno Dois")
                .email("dois@email.com")
                .cpf("987.654.321-00")
                .telefone("(11)98888-0002")
                .role(Role.ALUNO)
                .ativo(true)
                .loginSocial(false)
                .senha("123456")
                .build());

        alunoRepository.save(new Aluno(
                UUID.randomUUID(),
                u2,
                LocalDate.now().minusYears(20),
                LocalDate.now(),
                true,
                List.of(),
                List.of(),
                new GraduacaoAluno(
                        BeltGraduation.BRANCA,
                        GraduationLevel.ZERO
                )
        ));

        // Act + Assert
        mockMvc.perform(get("/alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].nome").value("Aluno Um"))
                .andExpect(jsonPath("$.content[0].email").value("um@email.com"))
                .andExpect(jsonPath("$.content[1].nome").value("Aluno Dois"))
                .andExpect(jsonPath("$.content[1].email").value("dois@email.com"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    void deveRetornarPageVaziaDeAlunosComConteudoCorreto() throws Exception {
        mockMvc.perform(get("/alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void deveRetornarDadosDoAlunoBuscadoPorId() throws Exception {
        var aluno = alunoRepository.save(new Aluno(
                UUID.randomUUID(),
                usuario,
                LocalDate.now().minusYears(20),
                LocalDate.now(),
                true,
                List.of(),
                List.of(),
                new GraduacaoAluno(
                        BeltGraduation.BRANCA,
                        GraduationLevel.ZERO
                )
        ));
        mockMvc.perform(get("/alunos/{id}", aluno.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(aluno.getId().toString()))
                .andExpect(jsonPath("$.nome").value("Usuário Teste"))
                .andExpect(jsonPath("$.email").value("usuario@email.com"));;
    }

    @Test
    void deveRetornar404_AoBuscarAlunosPorId_QuandoResponsavelNaoExistir() throws Exception {
        var usuario = Usuario.builder()
                .nome("Aluno Dois")
                .email("dois@email.com")
                .cpf("987.654.321-00")
                .telefone("(11)98888-0002")
                .role(Role.ALUNO)
                .ativo(true)
                .loginSocial(false)
                .senha("123456")
                .build();

        var aluno = new Aluno(
                UUID.randomUUID(),
                usuario,
                LocalDate.now().minusYears(20),
                LocalDate.now(),
                true,
                List.of(),
                List.of(),
                new GraduacaoAluno(
                        BeltGraduation.BRANCA,
                        GraduationLevel.ZERO
                )
        );

        mockMvc.perform(get("/alunos/{id}", aluno.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveAtualizarStatusMatriculaComSucesso() throws Exception {
        var aluno = alunoRepository.save(new Aluno(
                UUID.randomUUID(),
                usuario,
                LocalDate.now().minusYears(20),
                LocalDate.now(),
                true,
                List.of(),
                List.of(),
                new GraduacaoAluno(
                        BeltGraduation.BRANCA,
                        GraduationLevel.ZERO
                )
        ));
        var request = new AlunoUpdateMatriculaRequest(false);

        mockMvc.perform(patch("/alunos/{id}/matricula", aluno.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        var alunoSalvo = alunoRepository.findById(aluno.getId());
        assertFalse(alunoSalvo.get().isMatriculaAtiva());
    }

    @Test
    void deveRetornar404_AoAtualizarStatusMatricula_QuandoAlunoNaoExistir() throws Exception {
        var aluno = new Aluno(
                UUID.randomUUID(),
                usuario,
                LocalDate.now().minusYears(20),
                LocalDate.now(),
                true,
                List.of(),
                List.of(),
                new GraduacaoAluno(
                        BeltGraduation.BRANCA,
                        GraduationLevel.ZERO
                )
        );
        var request = new AlunoUpdateMatriculaRequest(false);

        mockMvc.perform(patch("/alunos/{id}/matricula", aluno.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar409_AoAtualizarStatusMatricula_QuandoStatusForIgualAoAtual() throws Exception {
        var aluno = alunoRepository.save(new Aluno(
                UUID.randomUUID(),
                usuario,
                LocalDate.now().minusYears(20),
                LocalDate.now(),
                true,
                List.of(),
                List.of(),
                new GraduacaoAluno(
                        BeltGraduation.BRANCA,
                        GraduationLevel.ZERO
                )
        ));
        var request = new AlunoUpdateMatriculaRequest(true);

        mockMvc.perform(patch("/alunos/{id}/matricula", aluno.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void deveAtualizarDataMatriculaComSucesso() throws Exception {
        var aluno = alunoRepository.save(new Aluno(
                UUID.randomUUID(),
                usuario,
                LocalDate.now().minusYears(20),
                LocalDate.now(),
                true,
                List.of(),
                List.of(),
                new GraduacaoAluno(
                        BeltGraduation.BRANCA,
                        GraduationLevel.ZERO
                )
        ));
        var request = new AlunoUpdateDataMatriculaRequest(LocalDate.now().minusDays(1));

        mockMvc.perform(patch("/alunos/{id}/data-matricula", aluno.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        var alunoSalvo = alunoRepository.findById(aluno.getId());
        assertEquals(LocalDate.now().minusDays(1), alunoSalvo.get().getDataMatricula());
    }

    @Test
    void deveRetornar404_AoAtualizarDataMatricula_QuandoAlunoNaoExistir() throws Exception {
        var aluno = new Aluno(
                UUID.randomUUID(),
                usuario,
                LocalDate.now().minusYears(20),
                LocalDate.now(),
                true,
                List.of(),
                List.of(),
                new GraduacaoAluno(
                        BeltGraduation.BRANCA,
                        GraduationLevel.ZERO
                )
        );
        var request = new AlunoUpdateDataMatriculaRequest(LocalDate.now().minusDays(1));

        mockMvc.perform(patch("/alunos/{id}/data-matricula", aluno.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveAtualizarDataNascimentoComSucesso() throws Exception {
        var aluno = alunoRepository.save(new Aluno(
                UUID.randomUUID(),
                usuario,
                LocalDate.now().minusYears(20),
                LocalDate.now(),
                true,
                List.of(),
                List.of(),
                new GraduacaoAluno(
                        BeltGraduation.BRANCA,
                        GraduationLevel.ZERO
                )
        ));
        var request = new AlunoUpdateDataNascimentoRequest(LocalDate.now().minusYears(18));

        mockMvc.perform(patch("/alunos/{id}/data-nascimento", aluno.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        var alunoSalvo = alunoRepository.findById(aluno.getId());
        assertEquals(LocalDate.now().minusYears(18), alunoSalvo.get().getDataNascimento());
    }

    @Test
    void deveRetornar404_AoAtualizarDataNascimento_QuandoAlunoNaoExistir() throws Exception {
        var aluno = new Aluno(
                UUID.randomUUID(),
                usuario,
                LocalDate.now().minusYears(20),
                LocalDate.now(),
                true,
                List.of(),
                List.of(),
                new GraduacaoAluno(
                        BeltGraduation.BRANCA,
                        GraduationLevel.ZERO
                )
        );
        var request = new AlunoUpdateDataNascimentoRequest(LocalDate.now().minusYears(18));

        mockMvc.perform(patch("/alunos/{id}/data-nascimento", aluno.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void devePromoverAlunoParaFaixaSeguinteComSucesso() throws Exception {
        var aluno = alunoRepository.save(new Aluno(
                UUID.randomUUID(),
                usuario,
                LocalDate.now().minusYears(15),
                LocalDate.now(),
                true,
                List.of(),
                List.of(),
                new GraduacaoAluno(
                        BeltGraduation.BRANCA,
                        GraduationLevel.IV
                )
        ));

        mockMvc.perform(patch("/alunos/{id}/promover/faixa", aluno.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        var alunoSalvo = alunoRepository.findById(aluno.getId());
        assertEquals(BeltGraduation.CINZA, alunoSalvo.get().getGraduacao().getBelt());
        assertEquals(GraduationLevel.ZERO, alunoSalvo.get().getGraduacao().getLevel());
    }

    @Test
    void devePromoverAlunoAdultoParaFaixaSeguinteComSucesso() throws Exception {
        var aluno = alunoRepository.save(new Aluno(
                UUID.randomUUID(),
                usuario,
                LocalDate.now().minusYears(18),
                LocalDate.now(),
                true,
                List.of(),
                List.of(),
                new GraduacaoAluno(
                        BeltGraduation.BRANCA,
                        GraduationLevel.IV
                )
        ));

        mockMvc.perform(patch("/alunos/{id}/promover/faixa", aluno.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        var alunoSalvo = alunoRepository.findById(aluno.getId());
        assertEquals(BeltGraduation.AZUL, alunoSalvo.get().getGraduacao().getBelt());
        assertEquals(GraduationLevel.ZERO, alunoSalvo.get().getGraduacao().getLevel());
    }

    @Test
    void deveRetornar404_AoPromoverFaixa_QuandoAlunoNaoExistir() throws Exception {
        var aluno = new Aluno(
                UUID.randomUUID(),
                usuario,
                LocalDate.now().minusYears(20),
                LocalDate.now(),
                true,
                List.of(),
                List.of(),
                new GraduacaoAluno(
                        BeltGraduation.BRANCA,
                        GraduationLevel.IV
                )
        );

        mockMvc.perform(patch("/alunos/{id}/promover/faixa", aluno.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar409_AoPromoverFaixa_QuandoGraduacaoNaoEstiverInicializada() throws Exception {
        var aluno = alunoRepository.save(new Aluno(
                UUID.randomUUID(),
                usuario,
                LocalDate.now().minusYears(20),
                LocalDate.now(),
                true,
                List.of(),
                List.of(),
                null
        ));

        mockMvc.perform(patch("/alunos/{id}/promover/faixa", aluno.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornar409_AoPromoverFaixa_QuandoAlunoEstiverNaFaixaPreta() throws Exception {
        var aluno = alunoRepository.save(new Aluno(
                UUID.randomUUID(),
                usuario,
                LocalDate.now().minusYears(20),
                LocalDate.now(),
                true,
                List.of(),
                List.of(),
                new GraduacaoAluno(
                        BeltGraduation.PRETA,
                        GraduationLevel.IV
                )
        ));

        mockMvc.perform(patch("/alunos/{id}/promover/faixa", aluno.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornar409_AoPromoverFaixa_QuandoAlunoTiverMenosDe4Graus() throws Exception {
        var aluno = alunoRepository.save(new Aluno(
                UUID.randomUUID(),
                usuario,
                LocalDate.now().minusYears(20),
                LocalDate.now(),
                true,
                List.of(),
                List.of(),
                new GraduacaoAluno(
                        BeltGraduation.BRANCA,
                        GraduationLevel.III
                )
        ));

        mockMvc.perform(patch("/alunos/{id}/promover/faixa", aluno.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRebaixarAlunoParaFaixaAnteriorComSucesso() throws Exception {
        var aluno = alunoRepository.save(new Aluno(
                UUID.randomUUID(),
                usuario,
                LocalDate.now().minusYears(15),
                LocalDate.now(),
                true,
                List.of(),
                List.of(),
                new GraduacaoAluno(
                        BeltGraduation.CINZA,
                        GraduationLevel.ZERO
                )
        ));

        mockMvc.perform(patch("/alunos/{id}/rebaixar/faixa", aluno.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        var alunoSalvo = alunoRepository.findById(aluno.getId());
        assertEquals(BeltGraduation.BRANCA, alunoSalvo.get().getGraduacao().getBelt());
        assertEquals(GraduationLevel.IV, alunoSalvo.get().getGraduacao().getLevel());
    }

    @Test
    void deveRebaixarAlunoAdultoParaFaixaAnteriorComSucesso() throws Exception {
        var aluno = alunoRepository.save(new Aluno(
                UUID.randomUUID(),
                usuario,
                LocalDate.now().minusYears(18),
                LocalDate.now(),
                true,
                List.of(),
                List.of(),
                new GraduacaoAluno(
                        BeltGraduation.AZUL,
                        GraduationLevel.ZERO
                )
        ));

        mockMvc.perform(patch("/alunos/{id}/rebaixar/faixa", aluno.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        var alunoSalvo = alunoRepository.findById(aluno.getId());
        assertEquals(BeltGraduation.BRANCA, alunoSalvo.get().getGraduacao().getBelt());
        assertEquals(GraduationLevel.IV, alunoSalvo.get().getGraduacao().getLevel());
    }

    @Test
    void deveRetornar404_AoRebaixarFaixa_QuandoAlunoNaoExistir() throws Exception {
        var aluno = new Aluno(
                UUID.randomUUID(),
                usuario,
                LocalDate.now().minusYears(20),
                LocalDate.now(),
                true,
                List.of(),
                List.of(),
                new GraduacaoAluno(
                        BeltGraduation.CINZA,
                        GraduationLevel.ZERO
                )
        );

        mockMvc.perform(patch("/alunos/{id}/rebaixar/faixa", aluno.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar409_AoRebaixarFaixa_QuandoGraduacaoNaoEstiverInicializada() throws Exception {
        var aluno = alunoRepository.save(new Aluno(
                UUID.randomUUID(),
                usuario,
                LocalDate.now().minusYears(20),
                LocalDate.now(),
                true,
                List.of(),
                List.of(),
                null
        ));

        mockMvc.perform(patch("/alunos/{id}/rebaixar/faixa", aluno.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornar409_AoRebaixarFaixa_QuandoAlunoTiverMaisDe0Graus() throws Exception {
        var aluno = alunoRepository.save(new Aluno(
                UUID.randomUUID(),
                usuario,
                LocalDate.now().minusYears(20),
                LocalDate.now(),
                true,
                List.of(),
                List.of(),
                new GraduacaoAluno(
                        BeltGraduation.CINZA,
                        GraduationLevel.II
                )
        ));

        mockMvc.perform(patch("/alunos/{id}/rebaixar/faixa", aluno.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornar409_AoRebaixarFaixa_QuandoAlunoEstiverNaFaixaBranca() throws Exception {
        var aluno = alunoRepository.save(new Aluno(
                UUID.randomUUID(),
                usuario,
                LocalDate.now().minusYears(20),
                LocalDate.now(),
                true,
                List.of(),
                List.of(),
                new GraduacaoAluno(
                        BeltGraduation.BRANCA,
                        GraduationLevel.ZERO
                )
        ));

        mockMvc.perform(patch("/alunos/{id}/rebaixar/faixa", aluno.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isConflict());
    }

    @Test
    void devePromoverGrauComSucesso() throws Exception {
        var aluno = alunoRepository.save(new Aluno(
                UUID.randomUUID(),
                usuario,
                LocalDate.now().minusYears(20),
                LocalDate.now(),
                true,
                List.of(),
                List.of(),
                new GraduacaoAluno(
                        BeltGraduation.BRANCA,
                        GraduationLevel.II
                )
        ));

        mockMvc.perform(patch("/alunos/{id}/promover/grau", aluno.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        var alunoSalvo = alunoRepository.findById(aluno.getId());
        assertEquals(GraduationLevel.III, alunoSalvo.get().getGraduacao().getLevel());
    }

    @Test
    void deveRetornar404_AoPromoverGrau_QuandoAlunoNaoExistir() throws Exception {
        var aluno = new Aluno(
                UUID.randomUUID(),
                usuario,
                LocalDate.now().minusYears(20),
                LocalDate.now(),
                true,
                List.of(),
                List.of(),
                new GraduacaoAluno(
                        BeltGraduation.BRANCA,
                        GraduationLevel.II
                )
        );

        mockMvc.perform(patch("/alunos/{id}/promover/grau", aluno.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar409_AoPromoverGrau_QuandoGraduacaoNaoEstiverInicializada() throws Exception {
        var aluno = alunoRepository.save(new Aluno(
                UUID.randomUUID(),
                usuario,
                LocalDate.now().minusYears(20),
                LocalDate.now(),
                true,
                List.of(),
                List.of(),
                null
        ));

        mockMvc.perform(patch("/alunos/{id}/promover/grau", aluno.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornar409_AoPromoverGrau_QuandoAlunoEstiverNoGrauMaximo() throws Exception {
        var aluno = alunoRepository.save(new Aluno(
                UUID.randomUUID(),
                usuario,
                LocalDate.now().minusYears(20),
                LocalDate.now(),
                true,
                List.of(),
                List.of(),
                new GraduacaoAluno(
                        BeltGraduation.BRANCA,
                        GraduationLevel.IV
                )
        ));

        mockMvc.perform(patch("/alunos/{id}/promover/grau", aluno.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRebaixarGrauComSucesso() throws Exception {
        var aluno = alunoRepository.save(new Aluno(
                UUID.randomUUID(),
                usuario,
                LocalDate.now().minusYears(20),
                LocalDate.now(),
                true,
                List.of(),
                List.of(),
                new GraduacaoAluno(
                        BeltGraduation.BRANCA,
                        GraduationLevel.III
                )
        ));

        mockMvc.perform(patch("/alunos/{id}/rebaixar/grau", aluno.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        var alunoSalvo = alunoRepository.findById(aluno.getId());
        assertEquals(GraduationLevel.II, alunoSalvo.get().getGraduacao().getLevel());
    }

    @Test
    void deveRetornar404_AoRebaixarGrau_QuandoAlunoNaoExistir() throws Exception {
        var aluno = new Aluno(
                UUID.randomUUID(),
                usuario,
                LocalDate.now().minusYears(20),
                LocalDate.now(),
                true,
                List.of(),
                List.of(),
                new GraduacaoAluno(
                        BeltGraduation.BRANCA,
                        GraduationLevel.III
                )
        );

        mockMvc.perform(patch("/alunos/{id}/rebaixar/grau", aluno.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar409_AoRebaixarGrau_QuandoGraduacaoNaoEstiverInicializada() throws Exception {
        var aluno = alunoRepository.save(new Aluno(
                UUID.randomUUID(),
                usuario,
                LocalDate.now().minusYears(20),
                LocalDate.now(),
                true,
                List.of(),
                List.of(),
                null
        ));

        mockMvc.perform(patch("/alunos/{id}/rebaixar/grau", aluno.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornar409_AoRebaixarGrau_QuandoAlunoEstiverNoGrauMinimo() throws Exception {
        var aluno = alunoRepository.save(new Aluno(
                UUID.randomUUID(),
                usuario,
                LocalDate.now().minusYears(20),
                LocalDate.now(),
                true,
                List.of(),
                List.of(),
                new GraduacaoAluno(
                        BeltGraduation.BRANCA,
                        GraduationLevel.ZERO
                )
        ));

        mockMvc.perform(patch("/alunos/{id}/rebaixar/grau", aluno.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isConflict());
    }
}
