# Cemnect Phase 1 - Architecture Summary

## Architecture Overview (Under 150 words)

Cemnect uses Clean Architecture with Jetpack Compose UI. The domain layer contains core models (`FlowNode`, `FlowEdge`, `Task`, `TaskExecution`) and the `FlowGraphStateEngine` that manages live flow graph state using Kotlin StateFlow. The engine tracks node status transitions (PENDING→RUNNING→COMPLETED/FAILED/BLOCKED), calculates progress, identifies blocking errors, and propagates downstream block states.

UI follows a single-activity architecture with Compose Navigation. `MainActivity` hosts the nav graph with two destinations: `HomeScreen` and `TaskExecutionScreen`. The TaskExecutionScreen reserves the top 25% for `LiveFlowGraphPanel`, which displays nodes via `FlowNodeCard` components showing real-time execution state, progress, and block reasons.

Hilt provides dependency injection readiness. The package structure separates concerns: `domain/model` (entities), `domain/engine` (state management), `ui/components` (reusable composables), `ui/screens` (feature screens), `navigation` (routing), and `di` (future DI modules). This skeleton is fully compilable and ready for AccessibilityService and Room integration in subsequent phases.

## Project Tree

```
cemnect/
├── settings.gradle.kts
├── build.gradle.kts
├── gradle.properties
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/cemnect/android/
│       │   ├── CemnectApplication.kt
│       │   ├── MainActivity.kt
│       │   ├── domain/
│       │   │   ├── model/
│       │   │   │   └── FlowModels.kt
│       │   │   └── engine/
│       │   │       └── FlowGraphStateEngine.kt
│       │   ├── navigation/
│       │   │   ├── NavGraph.kt
│       │   │   └── AppNavGraph.kt
│       │   ├── ui/
│       │   │   ├── theme/
│       │   │   │   ├── Color.kt
│       │   │   │   ├── Theme.kt
│       │   │   │   └── Type.kt
│       │   │   ├── components/
│       │   │   │   ├── FlowNodeCard.kt
│       │   │   │   └── LiveFlowGraphPanel.kt
│       │   │   └── screens/
│       │   │       ├── home/
│       │   │       │   └── HomeScreen.kt
│       │   │       └── execution/
│       │   │           └── TaskExecutionScreen.kt
│       │   └── di/
│       └── res/
│           └── values/
│               ├── colors.xml
│               ├── strings.xml
│               └── themes.xml
```

## Key Deliverables

1. **Architecture**: Clean Architecture, Hilt-ready, Compose-based
2. **Domain Models**: `FlowNode`, `FlowEdge`, `Task`, `TaskExecution`, enums for types/statuses
3. **FlowGraphStateEngine**: StateFlow-powered engine managing live graph state, node transitions, block detection
4. **UI Components**: `LiveFlowGraphPanel` (top 25%), `FlowNodeCard` with status visualization
5. **Screens**: `HomeScreen`, `TaskExecutionScreen` with 25%/75% split
6. **Navigation**: Compose Navigation with typed routes
7. **Theme**: Cemnect brand colors, Material3 theming

All files are real Kotlin with proper packages and imports, fully compilable.
