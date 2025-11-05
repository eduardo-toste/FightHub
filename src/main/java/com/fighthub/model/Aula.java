package com.fighthub.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "aulas")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class Aula {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String titulo;
    private String descricao;
    private LocalDate data;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turma_id", nullable = false)
    private Turma turma;

    @Column(name = "limite_alunos")
    private int limiteAlunos;

    private boolean ativo;

    public Aula(String titulo, String descricao, LocalDate data, Turma turma, int limiteAlunos) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.data = data;
        this.turma = turma;
        this.limiteAlunos = limiteAlunos;
    }
}
