# ADR-0008: Use Linear Regression for Battery Prediction

- **Status:** Accepted
- **Date:** 2026-08-03
- **Author:** @peterandree

## Context

Battery Guardian needs to predict when devices will reach low battery levels.

Requirements:
- Accurate enough to be useful
- Fast enough for mobile
- Works offline
- No LLM/AI models
- Handles edge cases

## Decision

Use **Simple Linear Regression** to predict battery drain.

## Consequences

### Positive
- Accuracy: Good for most use cases
- Performance: Fast computation
- Simplicity: Easy to understand
- Offline: Works completely offline
- No AI: No LLM/ML required
- Adaptability: Updates as new data arrives

### Negative
- Limited Accuracy: May not capture all patterns
- Cold Start: Requires initial data
- Edge Cases: Needs special handling

## Alternatives Considered
1. Exponential Regression: More complex
2. Machine Learning: Violates constraints
3. Moving Average: Doesn't predict future
4. Kalman Filter: Overkill

## References
- [Linear Regression](https://en.wikipedia.org/wiki/Linear_regression)
- [BTChargeTrayWatcher](https://github.com/peterandree/BTChargeTrayWatcher)