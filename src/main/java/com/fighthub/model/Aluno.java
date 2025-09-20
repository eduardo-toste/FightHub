package com.fighthub.model;

import com.fighthub.dto.aluno.AlunoUpdateCompletoRequest;
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

    @ManyToMany
    @JoinTable(
            name = "alunos_responsaveis",
            joinColumns = @JoinColumn(name = "aluno_id"),
            inverseJoinColumns = @JoinColumn(name = "responsavel_id")
    )
    private List<Responsavel> responsaveis = new ArrayList<>();

    public void updateCompleto(AlunoUpdateCompletoRequest request, List<Responsavel> novosResponsaveis) {
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

}
