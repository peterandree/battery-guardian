# ADR-0002: Use Kotlin with Coroutines for Asynchronous Programming

- **Status:** Accepted
- **Date:** 2026-08-03
- **Author:** @peterandree

## Context

Battery Guardian requires extensive asynchronous operations:
- Reading battery levels from Bluetooth devices (I/O operations)
- Database operations (Room)
- Background monitoring
- Scheduling and alerts
- UI updates based on data changes

Traditional approaches to async programming in Android include:
- **Callbacks:** Leads to "callback hell" with nested callbacks
- **RxJava:** Adds dependency, has learning curve
- **Thread pools:** Manual thread management is error-prone

Kotlin Coroutines provide a modern, idiomatic solution that addresses these issues. This decision aligns with **Workday-Wake**, which successfully uses coroutines for similar async operations (alarm scheduling, database access).

## Decision

Use **Kotlin with Coroutines** for all asynchronous programming in Battery Guardian.

### Key Implementation Points

1. **Coroutines Scopes:**
   - **ViewModels:** `viewModelScope`
   - **BroadcastReceivers:** `CoroutineScope(SupervisorJob()) + Dispatchers.IO` (reusing Workday-Wake pattern)
   - **Services:** `CoroutineScope(SupervisorJob()) + Dispatchers.IO`
   - **Use Cases:** `Dispatchers.Default`

2. **Dispatchers:**
   - **Main:** UI updates
   - **IO:** Bluetooth operations, database access, file I/O
   - **Default:** CPU-intensive operations (e.g., regression calculations)

3. **Flow vs StateFlow:**
   - Use `Flow` for cold asynchronous streams (e.g., database queries)
   - Use `StateFlow` for hot asynchronous state holders (e.g., UI state)
   - Never use `LiveData`

4. **Error Handling:**
   - Use `try-catch` blocks in coroutine builders
   - Propagate errors appropriately
   - Never swallow exceptions

## Consequences

### Positive
- **Readability:** Code is sequential and easy to understand
- **Maintainability:** Less boilerplate than callbacks or RxJava
- **Error Handling:** Structured error handling with `try-catch`
- **Cancellation:** Automatic cancellation with structured concurrency
- **Performance:** Efficient thread usage with dispatcher switching
- **Consistency:** Aligns with Workday-Wake patterns
- **Modern:** Industry-standard approach for Kotlin

### Negative
- **Learning Curve:** Developers unfamiliar with coroutines need to learn
- **Debugging:** Coroutine debugging can be challenging
- **Memory Leaks:** Improper scope usage can cause memory leaks

## Alternatives Considered

1. **RxJava:** Adds significant dependency, steeper learning curve
2. **Java Threads / Executors:** Manual thread management is error-prone
3. **Callbacks:** Leads to callback hell, hard to maintain
4. **Flow + Reactive Streams:** Unnecessary complexity

## References
- [Kotlin Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html)
- [Android Coroutines Guide](https://developer.android.com/kotlin/coroutines)
- [Workday-Wake Coroutine Usage](https://github.com/peterandree/Workday-Wake/blob/master/docs/architecture.md)