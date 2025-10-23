package com.fighthub.model;

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

    @Setter
    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Setter
    @Column(name = "data_matricula")
    private LocalDate dataMatricula;

    @Setter
    @Column(name = "matricula_ativa")
    private boolean matriculaAtiva;

    @ManyToMany(mappedBy = "alunos")
    private List<Responsavel> responsaveis = new ArrayList<>();

    @OneToMany(mappedBy = "aluno", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AlunoModalidade> modalidades = new ArrayList<>();

}
