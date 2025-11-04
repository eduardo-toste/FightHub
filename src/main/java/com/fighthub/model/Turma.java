package com.fighthub.model;

import com.fighthub.dto.turma.TurmaUpdateCompletoRequest;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "turmas")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Turma {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 255)
    private String horario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id")
    private Professor professor;

    @Column(nullable = false)
    private boolean ativo;

    @ManyToMany
    @JoinTable(
            name = "alunos_turmas",
            joinColumns = @JoinColumn(name = "turma_id"),
            inverseJoinColumns = @JoinColumn(name = "aluno_id")
    )
    private List<Aluno> alunos = new ArrayList<>();

    public Turma(String nome, String horario, Professor professor) {
        this.nome = nome;
        this.horario = horario;
        this.professor = professor;
        this.ativo = true;
    }

    public void putUpdate(TurmaUpdateCompletoRequest request, Professor professor) {
        this.nome = request.nome();
        this.horario = request.horario();
        this.professor = professor;
        this.ativo = request.ativo();
    }
}
