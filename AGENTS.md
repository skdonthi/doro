# Working in this repository as a coding agent

This file is read by Claude Code, Cursor and any other agent that edits code here. Humans should read it too.

## Before you write code

1. Read the ADRs in `docs/adr/`. They are constraints, not suggestions. If a task conflicts with an ADR, stop and say so; do not work around it.
2. Find or write the spec in `docs/specs/`. No feature without a spec. A spec has: context, acceptance criteria (each becomes a test name), out of scope, open questions.
3. Read `docs/domain/glossary.md`. Use the domain words. `Order`, not `Job`. `Tour`, not `Route`. `Proposal`, not `Suggestion`.

## Architecture rules

- State changes happen only through command handlers. No service writes another service's data.
- In `services/order`, the event store is the source of truth. Read models are disposable and rebuildable.
- Internal events stay internal. Anything published to Kafka is a **public event**: CloudEvents envelope, versioned type name (`…OrderPlaced.v1`), documented in `contracts/asyncapi.yaml`, changed only additively. See ADR-003.
- The copilot never writes domain state. It reads through tools and creates proposals. Humans approve. See ADR-009.
- Every model call goes through the gateway module in `services/copilot/src/gateway`. Never import the Anthropic SDK anywhere else. See ADR-008.
- Every event and every model call carries `actor`, `correlationId`, `causationId`. Traces must connect across the Kafka hop.

## Verification

- Tests first for aggregates and invariants. A PR that changes an invariant without a test is rejected.
- Contract files are linted in CI (`spectral`). If you add a public event or endpoint, update the contract in the same commit.
- Prompt or tool changes must run `make eval`. Include the score in the PR description.
- Do not add dependencies without a line in the relevant ADR or spec explaining why.

## Commits and PRs

- Conventional commits. Reference the spec: `feat(order): place order with loading metres`.
- PR template asks for: spec link, what you verified and how, eval result if relevant, ADR touched.

## Secrets

Never write secrets into files in this repository. Local secrets live in the macOS Keychain and are loaded via `.envrc` (see `.envrc.example`). Cloud secrets live in AWS SSM Parameter Store.
