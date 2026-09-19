package dev.donthi.doro.order.domain;

import java.time.Instant;


public record TimeWindow(Instant from, Instant to){
    public TimeWindow {
        if (to.isBefore(from)) throw new IllegalArgumentException("window ends before it starts");
    }
}
