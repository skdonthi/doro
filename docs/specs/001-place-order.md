# 001: Place order

## Context

A customer (or a clerk on their behalf) books a direct load: pickup address and window, delivery address and window, and what is being shipped. Placing the order is the first event in an order's life and the first public event dispatch consumes. See `docs/domain/glossary.md`.

## Command

`PlaceOrder { commandId, orderId, customerRef, pickup: {address, windowFrom, windowTo}, delivery: {address, windowFrom, windowTo}, load: {loadingMetres?, pallets?, weightKg?}, actor }`

## Acceptance criteria

- AC1: Given no history, when `PlaceOrder` with loading metres, then exactly one `OrderPlaced` event with the full order snapshot, version 1.
  → `placeOrder_withLoadingMetres_emitsOrderPlaced`
- AC2: Given no history, when `PlaceOrder` where pickup window ends after delivery window ends, then rejected with `PickupAfterDelivery`, no events.
  → `placeOrder_pickupAfterDelivery_isRejected`
- AC3: Given no history, when `PlaceOrder` without loading metres, pallets and weight, then rejected with `EmptyLoad`, no events.
  → `placeOrder_withoutAnyQuantity_isRejected`
- AC4: Given an order already placed, when the same `PlaceOrder` (same `commandId`) arrives again, then no new events and the original result is returned.
  → `placeOrder_duplicateCommand_isIdempotent`
- AC5: Given an order already placed, when a different `PlaceOrder` for the same `orderId` arrives, then rejected with `OrderAlreadyExists`.
  → `placeOrder_onExistingOrder_isRejected`
- AC6: Events appended to the store carry metadata `actor`, `commandId`, `correlationId`, `causationId`.
  → `append_storesMetadata` (event store test, Testcontainers)
- AC7: Appending with a stale expected version fails with a concurrency conflict and leaves the stream unchanged.
  → `append_withStaleVersion_conflicts`

## Out of scope

Amend, cancel, assignment, outbox publication (spec 002), HTTP endpoint (spec 003).

## Open questions

- Pallets and loading metres both given but inconsistent (33 pallets, 2 LDM). Reject, warn, or trust the caller? Decide with spec 002.
