package com.fighthub.service;

import com.fighthub.dto.endereco.EnderecoRequest;
import com.fighthub.dto.usuario.UpdateRoleRequest;
import com.fighthub.dto.usuario.UpdateStatusRequest;
import com.fighthub.dto.usuario.UsuarioUpdateCompletoRequest;
import com.fighthub.dto.usuario.UsuarioUpdateParcialRequest;
import com.fighthub.exception.UsuarioNaoEncontradoException;
import com.fighthub.exception.ValidacaoException;
import com.fighthub.model.Aluno;
import com.fighthub.model.Endereco;
import com.fighthub.model.Usuario;
import com.fighthub.model.enums.Role;
import com.fighthub.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;
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
    }

    @Test
    void deveRetornarPageComTodosUsuarios() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Usuario> page = new PageImpl<>(List.of(usuario));
        when(usuarioRepository.findAll(pageable)).thenReturn(page);

        var result = usuarioService.obterTodosUsuarios(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(usuarioRepository).findAll(pageable);
    }

    @Test
    void deveRetornarPageVazia() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Usuario> page = new PageImpl<>(List.of());
        when(usuarioRepository.findAll(pageable)).thenReturn(page);

        var result = usuarioService.obterTodosUsuarios(pageable);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        verify(usuarioRepository).findAll(pageable);
    }

    @Test
    void deveRetornarUsuarioBuscado() {
        var userId = usuario.getId();
        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(usuario));

        var result = usuarioService.obterUsuario(userId);

        assertNotNull(result);
        assertEquals("Usuario Teste", usuario.getNome());
        assertEquals("email@teste.com", usuario.getEmail());
        assertEquals("111.111.111-11", usuario.getCpf());
        verify(usuarioRepository).findById(userId);
    }

    @Test
    void deveLancarExcecao_QuandoUsuarioNaoExistir_AoObterUsuario() {
        var userId = usuario.getId();
        when(usuarioRepository.findById(userId)).thenReturn(Optional.empty());

        var ex = assertThrows(UsuarioNaoEncontradoException.class,
                () -> usuarioService.obterUsuario(userId));

        assertEquals("Usuário não encontrado.", ex.getMessage());
        verify(usuarioRepository).findById(userId);
    }

    @Test
    void deveAtualizarRoleDoUsuario() {
        var userId = usuario.getId();
        var request = new UpdateRoleRequest(Role.PROFESSOR);
        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(usuario));

        var result = usuarioService.updateRole(userId, request);

        assertNotNull(result);
        assertEquals(Role.PROFESSOR, request.role());
        verify(usuarioRepository).findById(userId);
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void deveLancarExcecao_QuandoUsuarioNaoExistir_AoAtualizarRole() {
        var userId = usuario.getId();
        var request = new UpdateRoleRequest(Role.PROFESSOR);
        when(usuarioRepository.findById(userId)).thenReturn(Optional.empty());

        var ex = assertThrows(UsuarioNaoEncontradoException.class,
                () -> usuarioService.updateRole(userId, request));

        assertEquals("Usuário não encontrado.", ex.getMessage());
        verify(usuarioRepository).findById(userId);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoUsuarioJaPossuirRoleDaRequest_AoAtualizarRole() {
        var userId = usuario.getId();
        var request = new UpdateRoleRequest(Role.ALUNO);
        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(usuario));

        var ex = assertThrows(ValidacaoException.class,
                () -> usuarioService.updateRole(userId, request));

        assertEquals("Usuário já cadastrado como " + request.role(), ex.getMessage());
        verify(usuarioRepository).findById(userId);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveAtualizarStatusDoUsuario() {
        var userId = usuario.getId();
        var request = new UpdateStatusRequest(true);
        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(usuario));

        var result = usuarioService.updateStatus(userId, request);

        assertNotNull(result);
        assertTrue(request.usuarioAtivo());
        verify(usuarioRepository).findById(userId);
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void deveLancarExcecao_QuandoUsuarioJaEstiverAtivo_AoAtualizarStatus() {
        var userId = usuario.getId();
        var request = new UpdateStatusRequest(true);
        usuario.setAtivo(true);
        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(usuario));

        var ex = assertThrows(ValidacaoException.class,
                () -> usuarioService.updateStatus(userId, request));

        assertEquals("Usuário já está ativo", ex.getMessage());
        verify(usuarioRepository).findById(userId);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoUsuarioJaEstiverInativo_AoAtualizarStatus() {
        var userId = usuario.getId();
        var request = new UpdateStatusRequest(false);
        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(usuario));

        var ex = assertThrows(ValidacaoException.class,
                () -> usuarioService.updateStatus(userId, request));

        assertEquals("Usuário já está inativo", ex.getMessage());
        verify(usuarioRepository).findById(userId);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveAtualizarUsuarioPorCompleto() {
        var userId = usuario.getId();
        var enderecoRequest = new EnderecoRequest(
                "12345-677",
                "Rua da Flor",
                "113",
                "Apto 44",
                "Centro",
                "São Paulo",
                "SP"
        );
        var request = new UsuarioUpdateCompletoRequest(
                "Nome Atualizado",
                "email_att@example.com",
                null,
                "(11)12346-5897",
                "111.111.111-22",
                enderecoRequest,
                Role.ALUNO,
                true
        );
        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(usuario));

        var result = usuarioService.updateUsuarioCompleto(userId, request);

        assertNotNull(result);
        assertEquals("Nome Atualizado", result.nome());
        assertEquals("email_att@example.com", result.email());
        assertEquals("111.111.111-22", result.cpf());
        verify(usuarioRepository).findById(userId);
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void deveAtualizarUsuarioParcialmente() {
        var userId = usuario.getId();
        var request = new UsuarioUpdateParcialRequest(
                "Nome Atualizacao Parcial",
                null,
                null,
                null,
                null,
                null,
                null,
                true
        );
        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(usuario));

        var result = usuarioService.updateUsuarioParcial(userId, request);

        assertNotNull(result);
        assertEquals("Nome Atualizacao Parcial", result.nome());
        verify(usuarioRepository).findById(userId);
        verify(usuarioRepository).save(usuario);
    }
}