# VoskSTTModule

**Location**: `packages/processing/VoskSTTModule/`  
**Package Category**: **Processing** - Speech recognition

## Overview
The VoskSTTModule provides offline speech-to-text capabilities using the Vosk speech recognition toolkit. It enables continuous voice recognition with hotword detection for the Sapphire Assistant Framework.

## Key Components

### KaldiService.kt
**Location**: `packages/processing/VoskSTTModule/src/main/java/com/example/vosksttmodule/KaldiService.kt`

**Purpose**: Speech-to-text service using Vosk/Kaldi libraries.

**Key Methods**:
- `setup()`: Initializes Vosk model and speech recognizer
- `onResult()`: Handles speech recognition results
- `sendUtterance()`: Processes recognized speech and routes to appropriate services

**Functionality**: Continuous speech recognition with hotword detection and result processing.

### VoskPostOfficeService.kt
**Location**: `packages/processing/VoskSTTModule/src/main/java/com/example/vosksttmodule/VoskPostOfficeService.kt`

**Purpose**: Registration service for the Vosk STT module.

**Key Methods**:
- `registerModule()`: Registers as an INPUT module with background service configuration

**Functionality**: Module registration with background service setup for continuous listening.

### CustomSpeechRecognizer.java
**Location**: `packages/processing/VoskSTTModule/src/main/java/com/example/vosksttmodule/CustomSpeechRecognizer.java`

**Purpose**: Custom implementation bridging Vosk recognition with Android audio systems.

## Dependencies
**Locations**: `packages/processing/VoskSTTModule/aars/` and `src/main/assets/`

- `kaldi-android-5.2.aar`: Vosk/Kaldi Android library
- Pre-trained Vosk model files in `assets/sync/model-android/`:
  - `final.mdl`: Acoustic model
  - `Gr.fst`, `HCLr.fst`: Grammar and pronunciation graphs
  - `words.txt`: Vocabulary
  - `ivector/`: I-vector extractor files
  - Various configuration files

## Configuration Files
**Location**: `packages/processing/VoskSTTModule/src/main/assets/`

- `vosk.conf`: Configuration for speech recognition parameters
- `mfcc.conf`: Audio feature extraction configuration

## Module Type
**INPUT** - Provides speech input capabilities to the framework.

## Key Features
- **Offline Recognition**: Complete offline speech-to-text processing
- **Continuous Listening**: Always-on voice recognition capability
- **Hotword Detection**: Responds to wake words to activate the assistant
- **Real-time Processing**: Low-latency speech recognition
- **No Internet Required**: Fully offline operation for privacy
- **Multiple Languages**: Support for different language models

## Technical Specifications
- **Audio Format**: 16kHz, 16-bit mono audio
- **Model Type**: Kaldi-based statistical models
- **Recognition Type**: Continuous speech recognition
- **Latency**: Real-time processing with minimal delay
- **Accuracy**: Depends on model quality and audio conditions

## Processing Pipeline
1. **Audio Capture**: Continuous audio recording from microphone
2. **Feature Extraction**: MFCC feature extraction from audio
3. **Speech Recognition**: Vosk model processes audio features
4. **Result Processing**: Converts recognition results to text
5. **Intent Routing**: Sends recognized text to ProcessorModule
6. **Response Handling**: Manages system responses and feedback

## Package Dependencies
- `packages/framework/ComponentFramework/` - Base service functionality
- `packages/core/CoreModule/` - Routing and voice interaction
- `packages/processing/ProcessorModule/` - Intent classification
- Android AudioRecord for microphone access
- Android VoiceInteractionService

## Build Configuration
```gradle
// In settings.gradle
include ':packages:processing:VoskSTTModule'

// Build command
./gradlew :packages:processing:VoskSTTModule:build
```

## Integration Points
- **Audio System**: Android AudioRecord for microphone access
- **Core Communication**: Routes recognition results through `packages/core/CoreModule/`
- **NLP Processing**: Sends recognized text to `packages/processing/ProcessorModule/`
- **Voice Interaction**: Integrates with Android VoiceInteractionService in CoreModule
- **Framework Foundation**: Built on `packages/framework/ComponentFramework/`

## Model Management
- **Model Loading**: Loads pre-trained Vosk models from assets
- **Model Caching**: Caches models for improved startup time
- **Model Updates**: Support for updating recognition models
- **Language Selection**: Configurable language model selection

## Privacy Features
- **Offline Processing**: No data sent to external servers
- **Local Storage**: All processing and models stored locally
- **No Cloud Dependency**: Complete independence from internet services
- **Data Control**: User maintains complete control over voice data

## Performance Considerations
- **Memory Usage**: Large models require significant RAM
- **CPU Usage**: Continuous recognition impacts battery life
- **Storage**: Models require substantial storage space
- **Optimization**: Optimized for mobile device constraints

## Status
**Active** - Fully implemented offline speech recognition with Vosk integration and continuous listening capabilities.

## Development Notes
- **Processing Pipeline**: Entry point for voice input processing
- **Large Dependencies**: Vosk models are substantial (100MB+)
- **Performance Critical**: Real-time audio processing requirements
- **Privacy First**: Completely offline operation
- **Hardware Intensive**: Requires significant RAM and CPU for continuous recognition