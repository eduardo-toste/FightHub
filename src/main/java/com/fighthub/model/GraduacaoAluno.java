// java
package com.fighthub.model;

import com.fighthub.model.enums.BeltGraduation;
import com.fighthub.model.enums.GraduationLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GraduacaoAluno {

    @Enumerated(EnumType.STRING)
    @Column(name = "graduacao_faixa")
    private BeltGraduation belt;

    @Enumerated(EnumType.STRING)
    @Column(name = "graduacao_nivel")
    private GraduationLevel level;

    public void promoteBelt() {
        if (belt != null) belt.next().ifPresent(b -> this.belt = b);
    }

    public void demoteBelt() {
        if (belt != null) belt.previous().ifPresent(b -> this.belt = b);
    }

    public void promoteLevel() {
        if (level != null) level.next().ifPresent(l -> this.level = l);
    }

    public void demoteLevel() {
        if (level != null) level.previous().ifPresent(l -> this.level = l);
    }

}
