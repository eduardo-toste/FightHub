package com.fighthub.model;

import com.fighthub.model.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "inscricoes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Inscricao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aluno_id")
    private Aluno aluno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aula_id")
    private Aula aula;

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;

    @Column(name = "inscrito_em", nullable = false)
    private LocalDateTime inscritoEm;

    public Inscricao(Aluno aluno, Aula aula, SubscriptionStatus status, LocalDateTime inscritoEm) {
        this.aluno = aluno;
        this.aula = aula;
        this.status = status;
        this.inscritoEm = inscritoEm;
    }
}
