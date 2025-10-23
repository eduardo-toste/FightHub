package com.fighthub.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "alunos_modalidades")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class AlunoModalidade {

    @EmbeddedId
    private AlunoModalidadeId id = new AlunoModalidadeId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("alunoId")
    @JoinColumn(name = "aluno_id")
    private Aluno aluno;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("modalidadeId")
    @JoinColumn(name = "modalidade_id")
    private Modalidade modalidade;

    @Column(name = "faixa_atual")
    private String faixaAtual;

}
