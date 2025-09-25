package com.fighthub.service;

import com.fighthub.model.Endereco;
import com.fighthub.model.Usuario;
import com.fighthub.model.enums.Role;
import com.fighthub.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CustomUserDetailsService service;

    private Usuario usuario;

    @BeforeEach
    void setup() {
        Endereco endereco = Endereco.builder()
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
                null, // foto
                Role.ALUNO,
                false, // loginSocial
                true,  // ativo
                "123.456.789-00", // cpf
                "(11)91234-5678", // telefone
                endereco
        );
    }

    @Test
    void deveCarrregarUsuarioCorretamente_QuandoEmailExistir() {
        // Arrange
        String email = "teste@gmail.com";
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));

        // Act
        var result = service.loadUserByUsername(email);

        // Assert
        assertEquals("teste@gmail.com", result.getUsername());
        assertEquals("senhaCriptografada", result.getPassword());
    }

    @Test
    void deveLancarExcecao_QuandoEmailNaoExistir() {
        // Arrange
        String email = "teste@gmail.com";
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        var ex = assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername(email));

        // Assert
        assertEquals("Usuário não encontrado", ex.getMessage());
    }

}