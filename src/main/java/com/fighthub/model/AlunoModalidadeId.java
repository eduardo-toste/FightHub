package com.fighthub.model;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class AlunoModalidadeId implements Serializable {

    private UUID alunoId;
    private UUID modalidadeId;

}
