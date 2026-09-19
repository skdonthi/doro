# DoRo — Donthi Road

An event-sourced mini Transport Management System (TMS) for direct road loads, with an agent platform on top.

**Status:** work in progress, started 2026-09-19. Built in public, one vertical slice at a time. Every architectural decision is recorded in [docs/adr](docs/adr/README.md).

## Why this exists

Modern European road-freight platforms are built on event sourcing, CQRS, Kafka and CloudEvents, run on Kubernetes in AWS, and are starting to put AI agents into core operational processes. DoRo is a small, honest replica of that shape: small enough to read in an evening, real enough to run, observe and break.

It is also a learning log. The ADRs argue why each choice was made, what was rejected, and what would change at scale.

## What it does

A dispatcher (Disponent) receives transport orders, plans tours on vehicles, and tracks execution. An AI copilot answers questions about the order book and **proposes** tour assignments. Humans approve or reject. Every proposal, approval and rejection is an event with an actor, so the audit trail is free and rejected proposals become evaluation cases.

## Architecture

```
 Angular 22 + Material (de/en)
        │ REST (OpenAPI)                 ▲ SSE
        ▼                                │
 ┌──────────────┐   Kafka · CloudEvents · AsyncAPI   ┌──────────────────┐
 │ doro-order   │──── order.public.v1 ──────────────▶│ doro-dispatch     │
 │ Java 21      │                                    │ NestJS            │
 │ event store: │◀─── dispatch.public.v1 ───────────│ tours, vehicles,  │
 │ PostgreSQL   │                                    │ proposals         │
 └──────────────┘                                    └──────────────────┘
        │ outbox                                              │ outbox
        ▼                                                     ▼
 read models: MongoDB (dispatch board) · Elasticsearch (order search)
        ▲
 ┌──────┴────────┐  Claude via one gateway: cost, budget, telemetry
 │ doro-copilot  │  one tool layer, three faces: in-process agent, MCP server, `doro` CLI
 └───────────────┘
 Runtime: Docker Compose → kind → EKS (Terraform). OpenTelemetry → Prometheus, Grafana, Tempo.
```

## Run modes

| Mode | Command | Auth | LLM |
|---|---|---|---|
| Local | `make up` | dev issuer, seeded users | `LLM_PROVIDER=anthropic` or `mock` (no key needed) |
| kind | `make kind-up` | dev issuer | same |
| AWS | `terraform apply` in `infra/terraform` | Cognito | `anthropic` or `bedrock` |

## Repository layout

```
AGENTS.md          rules for coding agents working in this repo
docs/adr/          architecture decision records
docs/specs/        one spec per feature, written before code
docs/domain/       glossary and domain model
contracts/         openapi.yaml, asyncapi.yaml, CloudEvents schemas
apps/ui            Angular
services/order     Java 21, Spring Boot, event-sourced
services/dispatch  NestJS
services/copilot   Node/TypeScript, agent + MCP + CLI
infra/compose      local stack
infra/helm         Kubernetes
infra/terraform    AWS (demo VM, transient EKS, Cognito, DNS)
evals/             copilot evaluation cases and runner
```

## Author

Shiva Krishna Donthi · Hamburg · [github.com/skdonthi](https://github.com/skdonthi) · [linkedin.com/in/skdonthi](https://linkedin.com/in/skdonthi)
