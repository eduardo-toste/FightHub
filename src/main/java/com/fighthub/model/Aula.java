package com.fighthub.model;

import com.fighthub.dto.aula.AulaUpdateCompletoRequest;
import com.fighthub.model.enums.ClassStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "aulas")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode(of = "id")
public class Aula {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String titulo;
    private String descricao;
    private LocalDateTime data;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turma_id")
    private Turma turma;

    @Column(name = "limite_alunos")
    private int limiteAlunos;

    @Enumerated(EnumType.STRING)
    private ClassStatus status;

    private boolean ativo = true;

    public Aula(String titulo, String descricao, LocalDateTime data, Turma turma, int limiteAlunos) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.data = data;
        this.turma = turma;
        this.limiteAlunos = limiteAlunos;
        this.status = ClassStatus.DISPONIVEL;
    }

    public void putUpdate(AulaUpdateCompletoRequest request, Turma turma) {
        this.titulo = request.titulo();
        this.descricao = request.descricao();
        this.data = request.data();
        this.turma = turma;
        this.limiteAlunos = request.limiteAlunos();
        this.ativo = request.ativo();
    }
}
