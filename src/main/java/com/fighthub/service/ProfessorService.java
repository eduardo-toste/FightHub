package com.fighthub.service;

import com.fighthub.dto.professor.CriarProfessorRequest;
import com.fighthub.dto.professor.ProfessorDetalhadoResponse;
import com.fighthub.dto.professor.ProfessorResponse;
import com.fighthub.exception.CpfExistenteException;
import com.fighthub.exception.ProfessorNaoEncontradoException;
import com.fighthub.exception.ValidacaoException;
import com.fighthub.mapper.ProfessorMapper;
import com.fighthub.model.Professor;
import com.fighthub.model.Usuario;
import com.fighthub.model.enums.Role;
import com.fighthub.repository.ProfessorRepository;
import com.fighthub.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfessorService {

    private final ProfessorRepository professorRepository;
    private final UsuarioRepository usuarioRepository;
    private final TokenService tokenService;
    private final EmailService emailService;

    public void criacaoProfessor(CriarProfessorRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new ValidacaoException("E-mail já cadastrado");
        }

        if (usuarioRepository.findByCpf(request.cpf()).isPresent()) {
            throw new CpfExistenteException();
        }

        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .nome(request.nome())
                .email(request.email())
                .cpf(request.cpf())
                .role(Role.PROFESSOR)
                .ativo(false)
                .loginSocial(false)
                .build());

        professorRepository.save(Professor.builder()
                .usuario(usuario)
                .build());

        String token = tokenService.salvarTokenAtivacao(usuario);
        emailService.enviarEmailAtivacao(usuario, token);
    }

    public Page<ProfessorResponse> buscarProfessores(Pageable pageable) {
        return ProfessorMapper.toPageDTO(professorRepository.findAll(pageable));
    }

    public ProfessorDetalhadoResponse buscarProfessorPorId(UUID id) {
        return ProfessorMapper.toDetailedDTO(professorRepository.findById(id)
                .orElseThrow(ProfessorNaoEncontradoException::new));
    }
}
