# Architecture Decision Records

Format: see [0000-template.md](0000-template.md). Status flow: Proposed → Accepted → Deprecated | Superseded.

| # | Title | Status |
|---|---|---|
| [000](ADR-000-how-doro-is-built.md) | How DoRo is built: spec-driven, agent-assisted, verified | Accepted |
| [001](ADR-001-event-sourcing-cqrs-for-order.md) | Event Sourcing + CQRS for the Order context | Accepted |
| 002 | Kafka as integration bus, not as event store | planned |
| 003 | Internal vs public events, CloudEvents, AsyncAPI | planned |
| 004 | Polyglot: Java for the event-sourced core, NestJS at the edges | planned |
| 005 | Read models: MongoDB for the board, Elasticsearch for search | planned |
| 006 | Transactional outbox with poller, not Debezium | planned |
| 007 | Observability: OpenTelemetry, Prometheus, Grafana, Tempo | planned |
| 008 | LLM gateway, cost tracking, provider switch | planned |
| 009 | Agent authority: proposals, machine identity, human in the loop | planned |
| 010 | Evals as part of the lifecycle | planned |
| 011 | One tool layer, three faces: agent, MCP, CLI | planned |
| 012 | Kubernetes and AWS: kind locally, EKS Auto Mode transiently, one VM for the demo | planned |
| 013 | i18n and multi-tenant readiness | planned |
| 014 | Snowflake as context layer (spike) | optional |
