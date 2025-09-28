package com.fighthub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fighthub.config.TestSecurityConfig;
import com.fighthub.dto.aluno.*;
import com.fighthub.dto.endereco.EnderecoRequest;
import com.fighthub.dto.endereco.EnderecoResponse;
import com.fighthub.exception.AlunoNaoEncontradoException;
import com.fighthub.exception.MatriculaInvalidaException;
import com.fighthub.model.Usuario;
import com.fighthub.model.enums.Role;
import com.fighthub.repository.TokenRepository;
import com.fighthub.repository.UsuarioRepository;
import com.fighthub.service.AlunoService;
import com.fighthub.service.AuthService;
import com.fighthub.service.JwtService;
import com.fighthub.utils.ErrorWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AlunoController.class)
@Import(TestSecurityConfig.class)
class AlunoControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private UsuarioRepository usuarioRepository;
    @MockBean private TokenRepository tokenRepository;
    @MockBean private AlunoService alunoService;
    @MockBean private AuthService authService;
    @MockBean private JwtService jwtService;
    @MockBean private ErrorWriter errorWriter;

    private static final String TOKEN = "token-valido";

    @BeforeEach
    void setupSecurity() {
        when(jwtService.tokenValido(anyString())).thenReturn(true);
        when(jwtService.extrairEmail(anyString())).thenReturn("usuario@teste.com");

        var usuarioMock = Usuario.builder()
                .id(UUID.randomUUID())
                .email("usuario@teste.com")
                .nome("Usuário Teste")
                .ativo(true)
                .role(Role.ADMIN)
                .build();

        when(usuarioRepository.findByEmail("usuario@teste.com"))
                .thenReturn(Optional.of(usuarioMock));

        when(tokenRepository.findByTokenAndExpiredFalseAndRevokedFalse(anyString()))
                .thenReturn(Optional.of(new com.fighthub.model.Token()));
    }

    @Test
    void deveCriarAluno() throws Exception {
        CriarAlunoRequest request = new CriarAlunoRequest(
                "João",
                "joao@email.com",
                "390.533.447-05",
                LocalDate.now().minusYears(17),
                List.of(UUID.randomUUID())
        );

        doNothing().when(alunoService).criarAluno(any(CriarAlunoRequest.class));

        mockMvc.perform(post("/alunos")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(alunoService).criarAluno(any(CriarAlunoRequest.class));
    }

    @Test
    void deveRetornarPaginaDeAlunos() throws Exception {
        Page<AlunoResponse> page = new PageImpl<>(List.of(
                new AlunoResponse(
                        UUID.randomUUID(),
                        "João",
                        "joao@email.com",
                        "(11)99999-9999",
                        null,
                        LocalDate.of(2003, 10, 15),
                        LocalDate.now(),
                        true
                )
        ));

        when(alunoService.obterTodos(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/alunos")
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("João"))
                .andExpect(jsonPath("$.content[0].email").value("joao@email.com"))
                .andExpect(jsonPath("$.content[0].matriculaAtiva").value(true));

        verify(alunoService).obterTodos(any(Pageable.class));
    }

    @Test
    void deveRetornarAlunoPorId() throws Exception {
        UUID id = UUID.randomUUID();
        AlunoDetalhadoResponse response = new AlunoDetalhadoResponse(
                id,
                "João",
                "joao@email.com",
                "(11)99999-9999",
                null,
                LocalDate.of(2003, 10, 15),
                LocalDate.now(),
                true,
                new EnderecoResponse("12345-678", "Rua das Flores", "123", "Apto 45", "Centro", "São Paulo", "SP"),
                List.of()
        );

        when(alunoService.obterAluno(id)).thenReturn(response);

        mockMvc.perform(get("/alunos/{id}", id)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.nome").value("João"));

        verify(alunoService).obterAluno(eq(id));
    }

    @Test
    void deveAtualizarAlunoCompletamente() throws Exception {
        UUID id = UUID.randomUUID();

        AlunoUpdateCompletoRequest request = new AlunoUpdateCompletoRequest(
                "João Atualizado",
                "joao@email.com",
                null,
                "(11)12345-6789",
                LocalDate.now().minusYears(17),
                List.of(),
                new EnderecoRequest(
                        "12345-678",
                        "Rua das Flores",
                        "123",
                        "Apto 45",
                        "Centro",
                        "São Paulo",
                        "SP"
                )
        );

        AlunoDetalhadoResponse response = new AlunoDetalhadoResponse(
                id,
                "João Atualizado",
                "joao@email.com",
                "(11)12345-6789",
                null,
                LocalDate.now().minusYears(17),
                LocalDate.now(),
                true,
                new EnderecoResponse(
                        "12345-678",
                        "Rua das Flores",
                        "123",
                        "Apto 45",
                        "Centro",
                        "São Paulo",
                        "SP"
                ),
                List.of()
        );

        when(alunoService.updateAlunoCompleto(eq(id), any(AlunoUpdateCompletoRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/alunos/{id}", id)
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("João Atualizado"))
                .andExpect(jsonPath("$.telefone").value("(11)12345-6789"))
                .andExpect(jsonPath("$.endereco.cep").value("12345-678"))
                .andExpect(jsonPath("$.endereco.cidade").value("São Paulo"))
                .andExpect(jsonPath("$.endereco.estado").value("SP"));

        verify(alunoService).updateAlunoCompleto(eq(id), any(AlunoUpdateCompletoRequest.class));
    }

    @Test
    void deveAtualizarAlunoParcialmente() throws Exception {
        UUID id = UUID.randomUUID();
        AlunoUpdateParcialRequest request = new AlunoUpdateParcialRequest(
                "Novo Nome", null, null, null, null, null, null
        );

        AlunoDetalhadoResponse response = new AlunoDetalhadoResponse(
                id,
                "Novo Nome",
                "joao@email.com",
                "(11)99999-9999",
                null,
                LocalDate.of(2003, 10, 15),
                LocalDate.now(),
                true,
                null,
                List.of()
        );

        when(alunoService.updateAlunoParcial(eq(id), any(AlunoUpdateParcialRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/alunos/{id}", id)
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Novo Nome"));

        verify(alunoService).updateAlunoParcial(eq(id), any(AlunoUpdateParcialRequest.class));
    }

    @Test
    void deveAtualizarStatusMatricula_QuandoSucesso() throws Exception {
        UUID id = UUID.randomUUID();
        var request = new AlunoUpdateMatriculaRequest(true);

        doNothing().when(alunoService).atualizarStatusMatricula(eq(id), eq(request));

        mockMvc.perform(patch("/alunos/{id}/matricula", id)
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(alunoService).atualizarStatusMatricula(eq(id), eq(request));
    }

    @Test
    void deveRetornarNotFound_QuandoAlunoNaoExistir_AoAtualizarMatricula() throws Exception {
        UUID id = UUID.randomUUID();
        var request = new AlunoUpdateMatriculaRequest(false);

        doThrow(new AlunoNaoEncontradoException())
                .when(alunoService).atualizarStatusMatricula(eq(id), eq(request));

        mockMvc.perform(patch("/alunos/{id}/matricula", id)
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Aluno não encontrado."));

        verify(alunoService).atualizarStatusMatricula(eq(id), eq(request));
    }

    @Test
    void deveRetornarConflict_QuandoStatusJaEstiverAtualizado() throws Exception {
        UUID id = UUID.randomUUID();
        var request = new AlunoUpdateMatriculaRequest(true);

        doThrow(new MatriculaInvalidaException())
                .when(alunoService).atualizarStatusMatricula(eq(id), eq(request));

        mockMvc.perform(patch("/alunos/{id}/matricula", id)
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("A situação atual da matricula já está neste estado."));

        verify(alunoService).atualizarStatusMatricula(eq(id), eq(request));
    }
}