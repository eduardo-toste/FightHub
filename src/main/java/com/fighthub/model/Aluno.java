package com.fighthub.model;

import com.fighthub.dto.aluno.AlunoUpdateCompletoRequest;
import com.fighthub.dto.aluno.AlunoUpdateParcialRequest;
import com.fighthub.mapper.EnderecoMapper;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "alunos")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@EqualsAndHashCode(of = "id")
public class Aluno {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(name = "data_matricula")
    private LocalDate dataMatricula;

    @Setter
    @Column(name = "matricula_ativa")
    private boolean matriculaAtiva;

    @ManyToMany
    @JoinTable(
            name = "alunos_responsaveis",
            joinColumns = @JoinColumn(name = "aluno_id"),
            inverseJoinColumns = @JoinColumn(name = "responsavel_id")
    )
    private List<Responsavel> responsaveis = new ArrayList<>();

    public void putUpdate(AlunoUpdateCompletoRequest request, List<Responsavel> novosResponsaveis) {
        this.usuario.setNome(request.nome());
        this.usuario.setEmail(request.email());
        this.usuario.setFoto(request.foto());
        this.usuario.setTelefone(request.telefone());
        this.usuario.setEndereco(EnderecoMapper.toEntity(request.endereco()));
        this.dataNascimento = request.dataNascimento();
        this.responsaveis.clear();
        if (novosResponsaveis != null && !novosResponsaveis.isEmpty()) {
            this.responsaveis.addAll(novosResponsaveis);
        }
    }

    public void patchUpdate(AlunoUpdateParcialRequest request, List<Responsavel> novosResponsaveis) {
        if (request.nome() != null) { this.usuario.setNome(request.nome()); }
        if (request.email() != null) { this.usuario.setEmail(request.email()); }
        if (request.foto() != null) { this.usuario.setFoto(request.foto()); }
        if (request.telefone() != null) { this.usuario.setTelefone(request.telefone()); }
        if (request.endereco() != null && this.usuario.getEndereco() != null) {
            this.usuario.getEndereco().patchUpdate(request.endereco());
        }
        if (request.dataNascimento() != null) { this.dataNascimento = request.dataNascimento();}
        if (novosResponsaveis != null && !novosResponsaveis.isEmpty()) {
            this.responsaveis.clear();
            this.responsaveis.addAll(novosResponsaveis);
        }
    }

}
