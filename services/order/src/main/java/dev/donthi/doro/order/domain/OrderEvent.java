package dev.donthi.doro.order.domain;

import java.util.UUID;

public sealed interface OrderEvent permits OrderPlaced {
    UUID orderId();
}
