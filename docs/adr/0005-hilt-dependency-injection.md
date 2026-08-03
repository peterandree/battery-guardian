# ADR-0005: Use Hilt for Dependency Injection

- **Status:** Accepted
- **Date:** 2026-08-03
- **Author:** @peterandree

## Context

Battery Guardian has multiple components with dependencies:
- ViewModels depend on Use Cases
- Use Cases depend on Repositories
- Repositories depend on DAOs and DataSources
- Services depend on various components

Manual dependency injection leads to:
- **Boilerplate:** Lots of code to create and pass dependencies
- **Hard to Test:** Difficult to mock dependencies for testing
- **Hard to Maintain:** Changes in dependencies require changes in multiple places
- **Error-Prone:** Easy to forget to pass a dependency

Dependency Injection (DI) frameworks solve these problems. Options include:
- **Dagger:** Mature, powerful, but verbose
- **Koin:** Simple, Kotlin-first, but less type-safe
- **Hilt:** Built on Dagger, Kotlin-first, recommended by Google

Hilt is successfully used in **Workday-Wake** and is the **recommended** DI solution for Android apps.

## Decision

Use **Hilt** for dependency injection in Battery Guardian.

### Key Implementation Points

1. **Application Setup:**
   - Annotate Application class with `@HiltAndroidApp`
   - Hilt generates necessary Dagger components

2. **Module Definition:**
   - Create Hilt modules with `@Module` and `@InstallIn`
   - Define `@Provides` methods for dependencies
   - Use `@Singleton` for shared instances
   - All bindings in one file (like Workday-Wake's `AppModule.kt`)

3. **Component Injection:**
   - Use `@AndroidEntryPoint` for Activities, Services, BroadcastReceivers
   - Use `@HiltViewModel` for ViewModels
   - Use `@Inject` for field injection

4. **Scopes:**
   - `@Singleton`: Database, Repositories, Readers, Engines, Managers
   - `@ActivityRetained`: ViewModels (handled automatically)
   - `@Service`: Service-specific components

5. **Testing:**
   - Use `@HiltAndroidTest` for instrumented tests
   - Use `HiltAndroidRule` to inject dependencies
   - Easy to mock dependencies

## Consequences

### Positive

1. **Reduced Boilerplate:** Automatic code generation for DI
2. **Type Safety:** Compile-time checking of dependencies
3. **Testability:** Easy to mock dependencies for testing
4. **Maintainability:** Centralized dependency configuration
5. **Consistency:** Follows Google's recommended patterns
6. **Integration:** Works seamlessly with Android components (ViewModel, Service, etc.)
7. **Consistency:** Aligns with Workday-Wake's DI approach
8. **Google Support:** Officially recommended and maintained by Google

### Negative

1. **Learning Curve:** Requires understanding Hilt/Dagger concepts
2. **Build Time:** Code generation adds to build time
3. **Complexity:** Can be complex for advanced use cases

## Alternatives Considered

### 1. Dagger
- **Rejected:** More verbose, requires more boilerplate
- **Issue:** Hilt provides the same functionality with less code
- **Note:** Hilt is built on Dagger

### 2. Koin
- **Rejected:** Less type-safe, not officially recommended by Google
- **Issue:** Runtime errors instead of compile-time errors
- **Note:** Good for Kotlin Multiplatform, but Hilt is better for Android-only

### 3. Manual DI
- **Rejected:** Too much boilerplate, doesn't scale well
- **Issue:** Hard to maintain as app grows

### 4. Anvil
- **Rejected:** Less mature, smaller community
- **Issue:** Hilt is more established and better supported

## References

- [Hilt Documentation](https://developer.android.com/training/dependency-injection/hilt-android)
- [Workday-Wake DI Setup](https://github.com/peterandree/Workday-Wake/blob/master/app/src/main/kotlin/com/workdaywake/di/AppModule.kt)
- [Hilt vs Dagger](https://medium.com/androiddevelopers/introducing-hilt-3434444b678c)
- [Hilt Testing Guide](https://developer.android.com/training/dependency-injection/hilt-android#test)
