package com.fighthub.service;

import com.fighthub.model.Usuario;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void enviarEmailAtivacao(Usuario usuario, String token) {
        String link = "http://localhost:8080/ativar?token=" + token;
        String assunto = "Finalize seu cadastro no FightHub!";
        String corpo = String.format("""
                Olá %s,
                
                Seu cadastro como aluno foi iniciado por um professor ou coordenador.
                Para concluir, clique no link abaixo e defina sua senha:

                %s

                Este link expira em 24 horas.
                """, usuario.getNome(), link);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

            helper.setTo(usuario.getEmail());
            helper.setSubject(assunto);
            helper.setText(corpo, false);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Erro ao enviar e-mail de ativação", e);
        }
    }
}