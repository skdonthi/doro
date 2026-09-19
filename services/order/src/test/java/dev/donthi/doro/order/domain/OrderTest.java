package dev.donthi.doro.order.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;


class OrderTest {

    private static final UUID ORDER_ID = UUID.randomUUID();

    private static PlaceOrder place(Load load, TimeWindow pickup, TimeWindow delivery) {
        return new PlaceOrder(
            UUID.randomUUID(), ORDER_ID, "CUST-4711",
            new Stop("Billstraße 1, 20539 Hamburg", pickup),
            new Stop("Landsberger Str. 5, 80339 München", delivery),
            load, "user:shiva");
    }

    private static TimeWindow window(String from, String to) {
        return new TimeWindow(Instant.parse(from), Instant.parse(to));
    }

    private static final TimeWindow PICKUP = window("2026-09-25T08:00:00Z", "2026-09-25T10:00:00Z");
    private static final TimeWindow DELIVERY = window("2026-09-26T07:00:00Z", "2026-09-26T12:00:00Z");

    @Test
    void placeOrder_withLoadingMetres_emitsOrderPlaced() {
        var cmd = place(new Load(6.0, null, null), PICKUP, DELIVERY);

        List<OrderEvent> events = Order.empty().handle(cmd);

        assertThat(events).singleElement().isInstanceOf(OrderPlaced.class);
        var placed = (OrderPlaced) events.getFirst();
        assertThat(placed.orderId()).isEqualTo(ORDER_ID);
        assertThat(placed.customerRef()).isEqualTo("CUST-4711");
        assertThat(placed.load().loadingMetres()).isEqualTo(6.0);
        assertThat(placed.pickup().window()).isEqualTo(PICKUP);
        assertThat(placed.delivery().window()).isEqualTo(DELIVERY);
    }

    @Test
    void placeOrder_pickupAfterDelivery_isRejected() {
        var latePickup = window("2026-09-27T08:00:00Z", "2026-09-27T10:00:00Z");
        var cmd = place(new Load(6.0, null, null), latePickup, DELIVERY);

        assertThatThrownBy(() -> Order.empty().handle(cmd))
            .isInstanceOf(DomainException.class)
            .extracting("reason").isEqualTo(RejectionReason.PICKUP_AFTER_DELIVERY);
    }

    @Test
    void placeOrder_withoutAnyQuantity_isRejected() {
        var cmd = place(new Load(null, null, null), PICKUP, DELIVERY);

        assertThatThrownBy(() -> Order.empty().handle(cmd))
            .isInstanceOf(DomainException.class)
            .extracting("reason").isEqualTo(RejectionReason.EMPTY_LOAD);
    }

    @Test
    void placeOrder_onExistingOrder_isRejected() {
        var first = place(new Load(6.0, null, null), PICKUP, DELIVERY);
        var history = Order.empty().handle(first);
        var second = place(new Load(2.0, null, null), PICKUP, DELIVERY);

        assertThatThrownBy(() -> Order.rehydrate(history).handle(second))
            .isInstanceOf(DomainException.class)
            .extracting("reason").isEqualTo(RejectionReason.ORDER_ALREADY_EXISTS);
    }
}
