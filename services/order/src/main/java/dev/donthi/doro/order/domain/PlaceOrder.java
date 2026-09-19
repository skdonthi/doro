package dev.donthi.doro.order.domain;

import java.util.UUID;

public record PlaceOrder(UUID commandId, UUID orderId, String customerRef, Stop pickup, Stop delivery, Load load, String actor) {
}
