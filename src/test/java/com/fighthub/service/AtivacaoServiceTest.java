package com.fighthub.service;

import com.fighthub.dto.auth.AtivacaoRequest;
import com.fighthub.dto.endereco.EnderecoRequest;
import com.fighthub.exception.AlunoNaoEncontradoException;
import com.fighthub.exception.TokenInvalidoException;
import com.fighthub.exception.ValidacaoException;
import com.fighthub.model.Aluno;
import com.fighthub.model.Endereco;
import com.fighthub.model.Token;
import com.fighthub.model.Usuario;
import com.fighthub.model.enums.Role;
import com.fighthub.model.enums.TokenType;
import com.fighthub.repository.AlunoRepository;
import com.fighthub.repository.TokenRepository;
import com.fighthub.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AtivacaoServiceTest {

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AlunoRepository alunoRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private JwtService jwtService;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AtivacaoService ativacaoService;

    private AtivacaoRequest request;
    private EnderecoRequest enderecoRequest;
    private Usuario usuario;
    private Endereco endereco;
    private Aluno aluno;

    @BeforeEach
    void setUp() {
        enderecoRequest = new EnderecoRequest(
                "12312-111",
                "Rua das Flores",
                "123",
                "Apto 45",
                "Centro",
                "São Paulo",
                "SP"
        );

        request = new AtivacaoRequest(
                "token-valido",
                "senhaUsada",
                "(11)12345-6789",
                enderecoRequest
        );

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
                .nome("João")
                .email("joao@email.com")
                .cpf("123.456.789-00")
                .role(Role.ALUNO)
                .ativo(false)
                .loginSocial(false)
                .endereco(endereco)
                .build();

        aluno = Aluno.builder()
                .id(UUID.randomUUID())
                .usuario(usuario)
                .dataNascimento(LocalDate.now().minusYears(20))
                .dataMatricula(LocalDate.now())
                .matriculaAtiva(false)
                .build();
    }

    @Test
    void deveAtivarAConta() {
        Token token = Token.builder()
                .token("token-valido")
                .tokenType(TokenType.ATIVACAO)
                .revoked(false)
                .expired(false)
                .usuario(usuario)
                .criadoEm(LocalDateTime.now())
                .expiraEm(LocalDateTime.now().plusDays(1))
                .build();

        when(tokenRepository.findByToken(request.token())).thenReturn(Optional.of(token));
        when(jwtService.tokenValido(token.getToken())).thenReturn(true);
        when(passwordEncoder.encode(request.senha())).thenReturn("senha-hash");
        when(alunoRepository.findByUsuarioId(usuario.getId())).thenReturn(Optional.of(aluno));

        ativacaoService.ativarConta(request);

        assertTrue(usuario.isAtivo());
        assertEquals("senha-hash", usuario.getSenha());
        assertEquals(request.telefone(), usuario.getTelefone());
        assertEquals(request.endereco().cep(), usuario.getEndereco().getCep());

        verify(passwordEncoder).encode(request.senha());
        verify(tokenService).revogarToken(usuario, TokenType.ATIVACAO);
        verify(usuarioRepository).save(usuario);
        verify(tokenRepository).save(token);
        verify(emailService).enviarEmailConfirmacao(usuario);
    }

    @Test
    void deveLancarExcecao_QuandoTokenNaoExistir() {
        when(tokenRepository.findByToken(request.token())).thenReturn(Optional.empty());

        var ex = assertThrows(TokenInvalidoException.class,
                () -> ativacaoService.ativarConta(request));

        assertEquals("Token inválido ou malformado.", ex.getMessage());
        verify(usuarioRepository, never()).save(any());
        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).enviarEmailConfirmacao(any());
    }

    @Test
    void deveLancarExcecao_QuandoTokenExpiradoOuRevogado() {
        Token token = Token.builder()
                .token("token-valido")
                .tokenType(TokenType.ATIVACAO)
                .usuario(usuario)
                .build();

        when(tokenRepository.findByToken(request.token())).thenReturn(Optional.of(token));
        when(jwtService.tokenValido(token.getToken())).thenReturn(false);

        assertThrows(ValidacaoException.class,
                () -> ativacaoService.ativarConta(request));

        verify(usuarioRepository, never()).save(any());
        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).enviarEmailConfirmacao(any());
    }

    @Test
    void deveLancarExcecao_QuandoAlunoNaoExistir() {
        Token token = Token.builder()
                .token("token-valido")
                .tokenType(TokenType.ATIVACAO)
                .revoked(false)
                .expired(false)
                .usuario(usuario)
                .criadoEm(LocalDateTime.now())
                .expiraEm(LocalDateTime.now().plusDays(1))
                .build();

        when(tokenRepository.findByToken(request.token())).thenReturn(Optional.of(token));
        when(jwtService.tokenValido(token.getToken())).thenReturn(true);
        when(passwordEncoder.encode(request.senha())).thenReturn("senha-hash");
        when(alunoRepository.findByUsuarioId(usuario.getId())).thenReturn(Optional.empty());

        var ex = assertThrows(AlunoNaoEncontradoException.class,
                () -> ativacaoService.ativarConta(request));

        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).enviarEmailConfirmacao(any());
        assertEquals("Aluno não encontrado.", ex.getMessage());
    }

}