package com.fighthub.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "modalidades")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
public class Modalidade {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String nome;

    @Column(name = "cor_padrao")
    private String corPadrao;

    @Column(name = "usa_graduacao")
    private boolean usaGraduacao;

    @OneToMany(mappedBy = "modalidade", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AlunoModalidade> alunos = new ArrayList<>();

}
