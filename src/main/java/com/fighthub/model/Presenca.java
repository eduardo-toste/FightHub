package com.fighthub.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "presencas")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Presenca {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private boolean presente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inscricao_id")
    private Inscricao inscricao;

    @Column(name = "data_registro")
    private LocalDate dataRegistro;

}
