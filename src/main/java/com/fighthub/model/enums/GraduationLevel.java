package com.fighthub.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@AllArgsConstructor
@Getter
public enum GraduationLevel {
    ZERO(0),
    I(1),
    II(2),
    III(3),
    IV(4);

    private final int order;

    public Optional<GraduationLevel> next() {
        return Arrays.stream(values())
                .filter(l -> l.order == this.order + 1)
                .findFirst();
    }

    public Optional<GraduationLevel> previous() {
        return Arrays.stream(values())
                .filter(l -> l.order == this.order - 1)
                .findFirst();
    }

    public static Optional<GraduationLevel> fromOrder(int order) {
        return Arrays.stream(values()).filter(l -> l.order == order).findFirst();
    }
}
