package com.fighthub.service;

import com.fighthub.exception.EmailNaoEnviadoException;
import com.fighthub.model.Endereco;
import com.fighthub.model.Token;
import com.fighthub.model.Usuario;
import com.fighthub.model.enums.Role;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private SpringTemplateEngine templateEngine;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    private Usuario usuario;
    private Endereco endereco;
    private String token;

    @BeforeEach
    void setUp() {
        endereco = Endereco.builder()
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
                null, Role.ALUNO,
                false, true, "123.456.789-00",
                "(11)91234-5678", endereco
        );

        token = "token-valido";
    }

    @Test
    void deveEnviarEmailAtivacao() throws Exception {
        String html = "<html><body>Email de ativação</body></html>";

        when(templateEngine.process(eq("email-ativacao"), any(Context.class))).thenReturn(html);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.enviarEmailAtivacao(usuario, token);

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("email-ativacao"), contextCaptor.capture());

        Context contextUsado = contextCaptor.getValue();
        assertEquals(usuario.getNome(), contextUsado.getVariable("nome"));
        assertTrue(contextUsado.getVariable("link").toString().contains(token));

        verify(javaMailSender).createMimeMessage();
        verify(javaMailSender).send(mimeMessage);
    }

    @Test
    void deveLancarExcecao_QuandoEmailAtivacaoNaoForEnviado() {
        String html = "<html><body>Email de ativação</body></html>";

        when(templateEngine.process(eq("email-ativacao"), any(Context.class))).thenReturn(html);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        doThrow(new MailSendException("Falha ao enviar"))
                .when(javaMailSender).send(mimeMessage);

        var ex = assertThrows(EmailNaoEnviadoException.class,
                () -> emailService.enviarEmailAtivacao(usuario, token));

        assertEquals("Erro ao enviar e-mail", ex.getMessage());
    }

    @Test
    void deveEnviarEmailConfirmacao() {
        String html = "<html><body>Email de confirmação</body></html>";

        when(templateEngine.process(eq("email-confirmacao-cadastro"), any(Context.class))).thenReturn(html);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.enviarEmailConfirmacao(usuario);

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("email-confirmacao-cadastro"), contextCaptor.capture());

        Context contextUsado = contextCaptor.getValue();
        assertEquals(usuario.getNome(), contextUsado.getVariable("nome"));

        verify(javaMailSender).createMimeMessage();
        verify(javaMailSender).send(mimeMessage);
    }

    @Test
    void deveLancarExcecao_QuandoEmailConfirmacaoNaoForEnviado() {
        String html = "<html><body>Email de ativação</body></html>";

        when(templateEngine.process(eq("email-confirmacao-cadastro"), any(Context.class))).thenReturn(html);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        doThrow(new MailSendException("Falha ao enviar"))
                .when(javaMailSender).send(mimeMessage);

        var ex = assertThrows(EmailNaoEnviadoException.class,
                () -> emailService.enviarEmailConfirmacao(usuario));

        assertEquals("Erro ao enviar e-mail", ex.getMessage());
    }

}