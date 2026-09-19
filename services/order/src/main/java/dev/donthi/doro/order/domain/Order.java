package dev.donthi.doro.order.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static dev.donthi.doro.order.domain.RejectionReason.*;


public class Order {

    private UUID id;
    private boolean placed;

    public static Order empty() {
        return new Order();
    }
    public static Order rehydrate(List<OrderEvent> events) {
        var order = new Order();
        events.forEach(order::apply);
        return order;
    }

    public List<OrderEvent> handle(PlaceOrder cmd) {
        if (placed) throw new DomainException(ORDER_ALREADY_EXISTS);
        if (cmd.load().isEmpty()) throw new DomainException(EMPTY_LOAD);
        if (cmd.pickup().window().to().isAfter(cmd.delivery().window().to())) throw new DomainException(PICKUP_AFTER_DELIVERY);
        return List.of(new OrderPlaced(
            cmd.commandId(), cmd.orderId(), cmd.customerRef(),
            cmd.pickup(), cmd.delivery(), cmd.load(), cmd.actor(),
            Instant.now()));
    }

    private void apply(OrderEvent e) {
        switch (e) {
            case OrderPlaced p -> {
                id = p.orderId();
                placed = true;
            }
        }
    }

}
