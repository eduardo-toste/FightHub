package com.fighthub.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@AllArgsConstructor
@Getter
public enum BeltGraduation {
    BRANCA(1),
    CINZA(2),
    AMARELA(3),
    LARANJA(4),
    VERDE(5),
    AZUL(6),
    ROXA(7),
    MARROM(8),
    PRETA(9);

    private final int order;

    public Optional<BeltGraduation> next() {
        return Arrays.stream(values())
                .filter(b -> b.order == this.order + 1)
                .findFirst();
    }

    public Optional<BeltGraduation> previous() {
        return Arrays.stream(values())
                .filter(b -> b.order == this.order - 1)
                .findFirst();
    }

    public static Optional<BeltGraduation> fromOrder(int order) {
        return Arrays.stream(values()).filter(b -> b.order == order).findFirst();
    }
}
