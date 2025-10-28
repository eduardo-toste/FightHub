package com.fighthub.integration;

import com.fighthub.dto.professor.CriarProfessorRequest;
import com.fighthub.dto.responsavel.CriarResponsavelRequest;
import com.fighthub.model.Endereco;
import com.fighthub.model.Professor;
import com.fighthub.model.Usuario;
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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ProfessorIntegrationTest extends IntegrationTestBase {

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

        usuario = usuarioRepository.save(
                Usuario.builder()
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
                        .build()
        );

        accessToken = jwtService.gerarToken(usuario);
        tokenService.salvarAccessToken(usuario, accessToken);
    }

    @Test
    void deveCriarProfessorComDadosValidos() throws Exception {
        var request = new CriarProfessorRequest(
                "Professor novo",
                "professor@email.com",
                "571.320.290-96"
        );

        mockMvc.perform(post("/professores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        var usuarioSalvo = usuarioRepository.findByEmail("professor@email.com");
        assertTrue(usuarioSalvo.isPresent());

        var professorSalvo = professorRepository.findByUsuario(usuarioSalvo.get());
        assertTrue(professorSalvo.isPresent());

        verify(tokenService).salvarTokenAtivacao(usuarioSalvo.get());
        verify(emailService).enviarEmailAtivacao(eq(usuarioSalvo.get()), anyString());
    }

    @Test
    void deveRetornar400_aoCriarProfessor_quandoUsarDadosInvalidos() throws Exception {
        var request = new CriarProfessorRequest(
                "",
                "",
                "111.111.111-22"
        );

        mockMvc.perform(post("/professores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar409_aoCriarProfessor_quandoEmailJaCadastrado() throws Exception {
        var request = new CriarResponsavelRequest(
                "Professor novo",
                "usuario@email.com",
                "571.320.290-96"
        );

        mockMvc.perform(post("/professores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornar409_aoCriarProfessor_quandoCpfJaCadastrado() throws Exception {
        var request = new CriarProfessorRequest(
                "Professor novo",
                "professor@email.com",
                "935.449.680-61"
        );

        mockMvc.perform(post("/professores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornar401_quandoNaoAutenticado() throws Exception {
        var request = new CriarProfessorRequest(
                "Professor",
                "email@naoautenticado.com",
                "123.456.789-00"
        );

        mockMvc.perform(post("/professores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar403_quandoUsuarioSemPermissao() throws Exception {
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

        var request = new CriarProfessorRequest(
                "Prof não autorizado",
                "prof@sempermissao.com",
                "935.449.680-61"
        );

        mockMvc.perform(post("/professores")
                        .header("Authorization", "Bearer " + tokenAluno)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveRetornarPageDeProfessoresComConteudoCorreto() throws Exception {
        // Arrange
        Usuario u1 = usuarioRepository.save(Usuario.builder()
                .nome("Prof Um")
                .email("um@email.com")
                .cpf("123.456.789-00")
                .telefone("(11)98888-0001")
                .role(Role.PROFESSOR)
                .ativo(true)
                .loginSocial(false)
                .senha("123456")
                .build());

        professorRepository.save(new Professor(
                UUID.randomUUID(),
                u1
        ));

        Usuario u2 = usuarioRepository.save(Usuario.builder()
                .nome("Prof Dois")
                .email("dois@email.com")
                .cpf("987.654.321-00")
                .telefone("(11)98888-0002")
                .role(Role.PROFESSOR)
                .ativo(true)
                .loginSocial(false)
                .senha("123456")
                .build());

        professorRepository.save(new Professor(
                UUID.randomUUID(),
                u2
        ));

        // Act + Assert
        mockMvc.perform(get("/professores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].nome").value("Prof Um"))
                .andExpect(jsonPath("$.content[0].email").value("um@email.com"))
                .andExpect(jsonPath("$.content[1].nome").value("Prof Dois"))
                .andExpect(jsonPath("$.content[1].email").value("dois@email.com"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    void deveRetornarPageVaziaDeProfessoresComConteudoCorreto() throws Exception {
        mockMvc.perform(get("/professores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void deveRetornarDadosDoProfessorBuscadoPorId() throws Exception {
        var professor = professorRepository.save(new Professor(
                UUID.randomUUID(),
                usuario
        ));
        mockMvc.perform(get("/professores/{id}", professor.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(professor.getId().toString()))
                .andExpect(jsonPath("$.nome").value("Usuário Teste"))
                .andExpect(jsonPath("$.email").value("usuario@email.com"));;
    }

    @Test
    void deveRetornar404_AoBuscarProfessorPorId_QuandoProfessorNaoExistir() throws Exception {
        var usuario = Usuario.builder()
                .nome("Prof Dois")
                .email("dois@email.com")
                .cpf("987.654.321-00")
                .telefone("(11)98888-0002")
                .role(Role.PROFESSOR)
                .ativo(true)
                .loginSocial(false)
                .senha("123456")
                .build();

        var professor = new Professor(
                UUID.randomUUID(),
                usuario
        );

        mockMvc.perform(get("/professores/{id}", professor.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

}
