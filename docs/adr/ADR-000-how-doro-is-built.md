# ADR-000: How DoRo is built: spec-driven, agent-assisted, verified

**Status:** Accepted (2026-09-19)
**Date:** 2026-09-19
**Deciders:** Shiva Krishna Donthi

## Context

DoRo is built by one engineer in evenings and weekends, with AI coding agents as daily tools. The interesting question is not whether agents write code. It is how a senior engineer steers them so the result is correct, consistent and explainable. The profession is shifting from authorship to steering: whoever masters context, constraints and verification owns the outcome.

This ADR fixes the working method so that every later ADR, spec and commit follows it.

## Decision

1. **Spec before code.** Every feature gets a short spec in `docs/specs/` with acceptance criteria. Acceptance criteria become test names. Agents receive the spec, not a chat prompt.
2. **Constraints live in files, not in heads.** `AGENTS.md` carries architecture rules. ADRs carry decisions. Agents read both before editing. A rule that only exists in a conversation does not exist.
3. **Verification is mechanical.** CI runs unit and integration tests, contract linting (OpenAPI, AsyncAPI), evals for prompt and tool changes, and an image scan. A green check means something specific.
4. **Human owns judgement.** The engineer writes specs, reviews diffs, decides trade-offs, and signs ADRs. Agents draft, implement, and propose. The same split applies to the product: the copilot proposes, the dispatcher decides (ADR-009).
5. **Everything is traceable.** Commits reference specs. PRs state what was verified and how. Rejected ideas are written down in "Why not the others".

## Options considered

### Option A: Vibe coding
Fast start, no documents. Rejected: the result cannot be explained, reviewed or continued by anyone else, including the author two weeks later.

### Option B: Heavy process, full RFCs and sprint ceremonies
Rejected: one person, thirty hours. Ceremony without a team is theatre.

### Option C: Lightweight spec + ADR + mechanical gates (chosen)
Small documents, all in the repo, enforced by CI where possible. Cost: about 15 % of build time goes into writing. Benefit: agents produce consistent code, reviewers can follow the reasoning, and the repo itself demonstrates the method.

## Consequences

- Easier: onboarding an agent or a person, reviewing a change, explaining a decision in an interview.
- Harder: nothing ships without a spec, even when the spec is three lines.
- Revisit when: a second engineer joins, then ADR ownership and review rules need a paragraph.

## Open questions

- Which spec granularity is right for UI work? Decide after the first two UI specs (by 2026-09-22).
