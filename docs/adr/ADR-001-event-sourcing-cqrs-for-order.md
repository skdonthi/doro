# ADR-001: Event Sourcing + CQRS for the Order context

**Status:** Accepted (2026-09-19)
**Date:** 2026-09-19
**Deciders:** Shiva Krishna Donthi

## Context

A transport order (Transportauftrag) lives for days. Pickup windows move, weights get corrected, carriers get swapped, customers dispute who changed the delivery date. Dispatch, tracking and billing all react to order changes. Auditability and traceability are first-class requirements in logistics, not nice-to-haves.

Constraints: one engineer, evenings and weekends, PostgreSQL available, Kafka planned as the integration bus (ADR-002).

## Decision

The Order context is **event-sourced**: the append-only event stream per order is the source of truth, current state is derived by replaying events. Reads are served from separate **read models** (CQRS), built by projecting events. The event store is a hand-rolled table in PostgreSQL. Kafka is not the event store (ADR-002).

Event sourcing is applied only where lifecycle and audit matter. Reference data such as vehicles is plain state (ADR-004).

## Options considered

### Option A: CRUD with an audit table and outbox
| Dimension | Assessment |
|---|---|
| Complexity | Low |
| Learning value | Low |
| Audit quality | Partial, audit table drifts from state |
| Fit | Weak, downstream integration needs events anyway |

### Option B: Axon Framework
| Dimension | Assessment |
|---|---|
| Complexity | Medium, framework conventions |
| Learning value | Medium, mechanics hidden |
| Audit quality | Full |
| Fit | Good, but opaque when debugging |

### Option C: Kafka as the event store
| Dimension | Assessment |
|---|---|
| Complexity | Medium |
| Learning value | Negative, teaches an anti-pattern |
| Audit quality | Full but hard to query per aggregate |
| Fit | Poor: no optimistic concurrency per stream, no per-aggregate reads, retention tension |

### Option D: Hand-rolled event store on PostgreSQL, Kafka as bus (chosen)
| Dimension | Assessment |
|---|---|
| Complexity | Medium |
| Learning value | High, every mechanism is explicit |
| Audit quality | Full |
| Fit | Strong, aligns with ADR-002 and ADR-003 |

## Why not the others

- A: history is a second-class citizen; dispute resolution and temporal queries need reconstruction work later.
- B: fine for a team that already knows the mechanics; here the mechanics are the point, and interviewers probe them.
- C: Kafka is a log, not a database. Per-stream optimistic concurrency and "load this order's history" are awkward. Teams that tried this accumulated debt.

## How

```sql
create table events (
  global_seq   bigserial primary key,
  stream_id    uuid        not null,
  version      int         not null,
  event_type   text        not null,
  payload      jsonb       not null,
  metadata     jsonb       not null,   -- actor, correlationId, causationId, tenant
  occurred_at  timestamptz not null default now(),
  unique (stream_id, version)          -- optimistic concurrency
);

create table outbox (
  id           uuid primary key,
  aggregate_id uuid not null,
  event_type   text not null,
  cloud_event  jsonb not null,
  created_at   timestamptz not null default now(),
  published_at timestamptz
);
```

Write path: load stream → fold into `Order` → `handle(command)` returns new events → append with expected version → write public CloudEvent(s) to outbox in the same transaction → commit. A poller publishes outbox rows to Kafka and marks them published. The unique constraint rejects concurrent writers; the command layer retries once.

Read path: projections consume events and maintain documents in MongoDB (dispatch board) and Elasticsearch (search). The UI never reads the events table.

## Consequences

- Easier: audit, temporal queries, rebuilding read models, integrating downstream contexts, attributing actions to actors (including agents).
- Harder: eventual consistency between write and read side, event schema evolution, debugging without tooling, first-week learning curve.
- Revisit when: a stream exceeds ~200 events (introduce snapshots); a second event-sourced context appears (extract shared event store library).

## Resolved questions (2026-09-19)

**1. Concurrency conflict is not idempotency.** Two *different* commands on the same order with the same expected version: the second `INSERT` violates `unique(stream_id, version)`. Discarding it silently would lose a legitimate change made on stale state. Recovery: catch the conflict, reload the stream (now one version ahead), re-run the command handler against the fresh state, append with the new expected version. One retry, then surface `409 Conflict` to the caller. Duplicate delivery of the *same* command (network retry) is a separate mechanism: every command carries a `commandId`, stored in event metadata; the handler checks it before doing work and returns the original result. Two problems, two mechanisms. The unique constraint is the only thing that makes the check real; never compute `version = max+1` inside SQL.

**2. Outbox in the same transaction, because of the dual-write problem.** Database and Kafka do not share a transaction. Commit then publish: a crash in between loses the event forever. Publish then commit: a crash produces a phantom event for state that never existed. One outbox row written in the same transaction as the events is atomic with the state change: both or neither. The poller then publishes at-least-once, so consumers deduplicate by event id. Asynchrony is a consequence of this, not the reason for it.

**3. `OrderPlaced.v1` carries the full order snapshot.** Dispatch needs addresses, time windows, loading metres, pallets and weight to plan and to run the capacity invariant. A thin "id only" event would force a synchronous call back into the order service and reintroduce coupling plus a race with the read model. Fat events cost schema discipline: the public event is a contract, versioned, additive changes only, curated separately from internal events. Amendments (`OrderAmended.v1`) also carry the full new snapshot, not deltas. Details in ADR-003.

## Open questions

- Snapshotting strategy once streams grow. Decide when a stream exceeds ~200 events.
