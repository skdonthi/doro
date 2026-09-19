package dev.donthi.doro.order.domain;

import java.time.Instant;
import java.util.UUID;

public record OrderPlaced(UUID commandId, UUID orderId, String customerRef, Stop pickup, Stop delivery, Load load, String actor, Instant occurredAt) implements OrderEvent {}
