# Sapphire Assistant Framework - Testing Strategy

## Overview

The Sapphire Assistant Framework currently has minimal test coverage. This document outlines a comprehensive testing strategy to ensure reliability, maintainability, and quality across all modules. The framework's service-oriented architecture with Intent-based communication presents unique testing challenges that require specific approaches.

## Current State

- **Existing Tests**: Only template/example tests (ExampleUnitTest, ExampleInstrumentedTest)
- **Test Infrastructure**: Basic JUnit 4 + Espresso setup
- **Coverage**: Near zero meaningful test coverage
- **Challenges**: Complex inter-service communication, Android-specific components, ML/NLP dependencies

## Testing Pyramid Strategy

### 1. Unit Tests (Foundation - 70%)
Focus on testing individual components in isolation with mocked dependencies.

### 2. Integration Tests (Middle - 20%)  
Test interactions between services and modules within the framework.

### 3. End-to-End Tests (Top - 10%)
Test complete user workflows from voice input to skill execution.

## Module-by-Module Testing Strategy

### Core Module (`packages/core/CoreModule`)

**Key Components to Test:**
- `CoreService`: Central orchestration service
- `CoreRegistrationService`: Module registration system
- `CoreVoiceInteractionService/Session`: Voice interaction handling

**Unit Tests:**
- **Intent Validation** (`CoreService:76-90`):
  ```kotlin
  @Test
  fun validateIntent_withValidSapphireInitialize_returnsTrue()
  @Test  
  fun validateIntent_withMissingFromExtra_returnsFalse()
  @Test
  fun validateIntent_withNullIntent_returnsFalse()
  ```

- **Route Handling** (`CoreService:425-497`):
  ```kotlin
  @Test
  fun handleRoute_withExistingRoute_callsNextModule()
  @Test
  fun handleNewInput_withValidRouteTable_setsCorrectRoute()
  @Test  
  fun expandRoute_withAliases_returnsExpandedRoute()
  ```

- **File Service Operations** (`CoreService:136-423`):
  ```kotlin
  @Test
  fun newCheckForLocal_withExistingFiles_attachesUris()
  @Test
  fun newCheckForLocal_withMissingFiles_requestsTransfer()
  @Test
  fun requestTransfer_withValidModule_sendsPendingIntent()
  ```

- **Service Lifecycle Management** (`CoreService:511-551`):
  ```kotlin
  @Test
  fun startBackgroundServices_withValidConfig_startsAllServices()
  @Test
  fun initialize_withPendingIntents_populatesLedger()
  ```

**Integration Tests:**
- Test full registration flow with mock modules
- Test file transfer operations between core and modules
- Test voice interaction service integration

**Instrumented Tests:**  
- Test foreground notification creation
- Test service binding lifecycle
- Test file provider URI generation

### Component Framework (`packages/framework/ComponentFramework`)

**Key Components:**
- `SapphireCoreService`: Base service class
- `SapphireFrameworkService`: Framework service base
- `SapphireFrameworkRegistrationService`: Registration utilities

**Unit Tests:**
- Test base service constants and utilities
- Test registration service methods
- Test common framework functionality

### VoskSTTModule (`packages/processing/VoskSTTModule`)

**Key Components:**
- `KaldiService`: Speech recognition service  
- `CustomSpeechRecognizer`: Vosk integration
- Model loading and management

**Unit Tests:**
- **Speech Recognition Lifecycle** (`KaldiService:97-125`):
  ```kotlin
  @Test
  fun onResult_withValidHypothesis_sendsUtterance()
  @Test
  fun onResult_withHotword_triggersVoiceInteraction()
  @Test
  fun sendUtterance_withNonEmptyText_callsReturnSapphireService()
  ```

- **Model Management** (`KaldiService:67-95`):
  ```kotlin
  @Test 
  fun setup_withValidAssets_initializesRecognizer()
  @Test
  fun setup_withMissingModel_throwsException()
  ```

**Integration Tests:**
- Test Vosk library integration with mock audio input
- Test service communication with CoreService

**Instrumented Tests:**
- Test asset synchronization
- Test microphone permissions and audio recording
- Test model loading from assets

### ProcessorModule (`packages/processing/ProcessorModule`)

**Key Components:**
- `ProcessorCentralService`: NLP processing orchestration
- Stanford CoreNLP integration
- Intent classification

**Unit Tests:**
- **Text Processing** (`ProcessorCentralService:33-70`):
  ```kotlin
  @Test
  fun process_withValidUtterance_classifiesIntent()
  @Test
  fun process_withHighConfidence_setsCorrectRoute()
  @Test
  fun process_withLowConfidence_usesDefaultRoute()
  ```

- **Classifier Management** (`ProcessorCentralService:72-83`):
  ```kotlin
  @Test
  fun loadClassifier_withExistingFile_returnsClassifier()
  @Test
  fun loadClassifier_withMissingFile_requestsFiles()
  @Test
  fun deleteClassifier_removesFile()
  ```

**Integration Tests:**
- Test Stanford CoreNLP integration
- Test file request/response with CoreService
- Test classification accuracy with sample data

**Instrumented Tests:**
- Test classifier file operations
- Test asset loading from APK

### MycroftModule (`packages/processing/MycroftModule`)

**Key Components:**
- `BracketExpander`: Intent template parsing

**Unit Tests:**
- **Bracket Expansion Logic** (`BracketExpander:35-141`):
  ```kotlin
  @Test
  fun expandBrackets_withSimpleOr_returnsAllVariations()
  @Test  
  fun expandBrackets_withNestedBrackets_handlesRecursion()
  @Test
  fun expandBrackets_withEmptyOption_includesBlankVariation()
  @Test
  fun parseSentenceList_withMultipleSentences_expandsAll()
  ```

**Integration Tests:**
- Test complex bracket expansion scenarios
- Test integration with intent parsing pipeline

### MultiprocessModule (`packages/processing/MultiprocessModule`)

**Key Components:**
- Cross-module coordination
- File transfer operations

**Unit Tests:**
- Test multiprocess coordination logic
- Test data serialization/deserialization
- Test error handling for failed transfers

**Integration Tests:**
- Test actual multiprocess communication
- Test file transfer between processes
- Test service orchestration

### CalendarSkill (`packages/skills/CalendarSkill`)

**Key Components:**
- `CalendarService`: Calendar operations
- `CalendarPostOfficeService`: Communication handling

**Unit Tests:**
- **Intent Processing** (`CalendarService:18-43`):
  ```kotlin
  @Test
  fun onStartCommand_withCreateAction_createsEvent()
  @Test
  fun getManditoryVariables_withValidIntent_extractsRequiredData()
  @Test
  fun checkForOptional_withOptionalExtras_extractsOptionalData()
  ```

**Integration Tests:**
- Test calendar provider integration
- Test skill registration with core

**Instrumented Tests:**
- Test calendar permissions
- Test actual calendar operations on device

### Integration Modules (TermuxModule, TaskerModule)

**Unit Tests:**
- Test external service communication
- Test data transformation
- Test error handling for external failures

**Integration Tests:**
- Test actual integration with Termux/Tasker (where available)
- Test fallback behavior when external apps unavailable

## Testing Infrastructure Improvements

### 1. Enhanced Build Configuration

Update `build.gradle` files to include:

```gradle
dependencies {
    // Enhanced testing dependencies
    testImplementation 'junit:junit:4.13.2'
    testImplementation 'org.mockito:mockito-core:5.1.1'
    testImplementation 'org.mockito:mockito-inline:5.1.1'
    testImplementation 'org.robolectric:robolectric:4.10.3'
    testImplementation 'androidx.test:core:1.5.0'
    testImplementation 'org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3'
    
    // Android instrumented tests
    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
    androidTestImplementation 'androidx.test:rules:1.5.0'
    androidTestImplementation 'androidx.test:runner:1.5.2'
    androidTestImplementation 'org.mockito:mockito-android:5.1.1'
}
```

### 2. Test Utilities and Helpers

Create shared test utilities:

**`TestServiceHelper.kt`**:
```kotlin
object TestServiceHelper {
    fun createMockIntent(action: String, extras: Map<String, Any>): Intent
    fun createTestPendingIntent(): PendingIntent
    fun mockCoreServiceIntents(): Map<String, PendingIntent>
}
```

**`MockAssetManager.kt`**:
```kotlin
class MockAssetManager {
    fun createMockModelFiles(): File
    fun createMockConfigFiles(): File
}
```

### 3. Test Data and Fixtures

Create standardized test data:
- Sample utterances for STT testing
- Mock NLP models and classifications  
- Test configuration files
- Sample calendar events and intents

### 4. CI/CD Integration

Add GitHub Actions workflow (`.github/workflows/test.yml`):

```yaml
name: Test Suite
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 11
        uses: actions/setup-java@v3
        with:
          java-version: '11'
      - name: Run unit tests
        run: ./gradlew test
      - name: Run instrumented tests
        uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: 30
          script: ./gradlew connectedAndroidTest
```

## Testing Priorities

### Phase 1: Foundation (Immediate)
1. Core Service intent validation and routing logic
2. Basic service lifecycle management  
3. File operations and URI handling
4. Framework base classes and utilities

### Phase 2: Processing (Short-term)
1. Speech recognition result handling
2. NLP classification accuracy
3. Bracket expansion parser
4. Error handling across modules

### Phase 3: Integration (Medium-term)  
1. Inter-service communication
2. Full voice-to-skill workflows
3. External integrations
4. Performance and reliability testing

### Phase 4: Advanced (Long-term)
1. Load testing and stress testing
2. Security testing for Intent handling
3. Memory leak detection
4. Battery usage optimization testing

## Test Execution Strategy

### Local Development
```bash
# Run all unit tests
./gradlew test

# Run tests for specific module  
./gradlew :packages:core:CoreModule:test

# Run instrumented tests
./gradlew connectedAndroidTest

# Generate coverage report
./gradlew jacocoTestReport  
```

### Continuous Integration
- Run unit tests on every commit
- Run integration tests on PR creation
- Run full end-to-end tests nightly
- Generate and publish coverage reports

## Mocking Strategy

### Service Dependencies
- Mock Android Context and system services
- Mock Intent handling and PendingIntent operations
- Mock file system operations and URI providers

### External Libraries
- Mock Vosk/Kaldi speech recognition
- Mock Stanford CoreNLP operations  
- Mock calendar provider operations

### Inter-Service Communication
- Mock service binding and Intent communication
- Mock file transfer operations
- Mock registration and discovery processes

## Success Metrics

### Coverage Targets
- **Unit Test Coverage**: 80% minimum
- **Integration Test Coverage**: 60% minimum  
- **Critical Path Coverage**: 95% minimum

### Quality Gates
- All tests must pass before merge
- No decrease in test coverage
- Performance tests within acceptable thresholds

### Monitoring
- Track test execution time trends
- Monitor flaky test rates
- Track coverage metrics over time

## Conclusion

This comprehensive testing strategy addresses the unique challenges of the Sapphire Assistant Framework's service-oriented architecture. Implementation should be phased, starting with critical core functionality and expanding to cover the full system. The focus on mocking and isolation will enable fast, reliable unit tests while integration tests ensure proper inter-service communication.

Regular review and updates of this strategy will be necessary as the framework evolves and new modules are added.