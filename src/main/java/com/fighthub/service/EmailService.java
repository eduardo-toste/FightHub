package com.fighthub.service;

import com.fighthub.exception.EmailNaoEnviadoException;
import com.fighthub.model.Usuario;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Async
    public void enviarEmailAtivacao(Usuario usuario, String token) {
        String link = "http://localhost:3000/ativar?token=" + token;

        Context context = new Context();
        context.setVariable("nome", usuario.getNome());
        context.setVariable("link", link);

        String htmlContent = templateEngine.process("email-ativacao", context);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

            helper.setTo(usuario.getEmail());
            helper.setSubject("Finalize seu cadastro no FightHub!");
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException | MailException e) {
            throw new EmailNaoEnviadoException();
        }
    }

    @Async
    public void enviarEmailConfirmacao(Usuario usuario) {
        Context context = new Context();
        context.setVariable("nome", usuario.getNome());

        String htmlContent = templateEngine.process("email-confirmacao-cadastro", context);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

            helper.setTo(usuario.getEmail());
            helper.setSubject("Boas vindas ao FightHub!");
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException | MailException e) {
            throw new EmailNaoEnviadoException();
        }
    }

    @Async
    public void enviarEmailRecuperacaoSenha(Usuario usuario, String codigoRecuperacao) {
        Context context = new Context();
        context.setVariable("nome", usuario.getNome());
        context.setVariable("codigo", codigoRecuperacao);

        String htmlContent = templateEngine.process("email-recuperacao-senha", context);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

            helper.setTo(usuario.getEmail());
            helper.setSubject("Recuperação de senha solicitada!");
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException | MailException e) {
            throw new EmailNaoEnviadoException();
        }
    }
}