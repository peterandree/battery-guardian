# ADR-0008: Use explicit lifecycle ownership, cooperative shutdown, and single-flight refreshes

- Status: Accepted
- Date: 2026-08-03

## Context

Polling, source discovery, settings writes, and notification delivery can run asynchronously. Shutdown or configuration changes during these operations must not leave orphan work, concurrent reads from the same source, or partially applied state.

## Decision

The application lifecycle owns a root cancellation mechanism and tracks background work.

Each source permits at most one in-flight refresh. A scheduled refresh that overlaps an existing refresh is skipped or coalesced. Shutdown cancels new work, waits for a bounded graceful completion period, and then disposes resources.

## Consequences

- Background work has clear ownership and termination semantics.
- Slow or faulty sources cannot accumulate unbounded concurrent operations.
- Source adapters must support cancellation where the underlying API permits it.
- Forced shutdown may still abandon work after the grace period.
