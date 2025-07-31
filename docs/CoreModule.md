# CoreModule

**Location**: `packages/core/CoreModule/`  
**Package Category**: **Core** - Central coordination

## Overview
The CoreModule is the central nervous system of the Sapphire Assistant Framework. It coordinates all module interactions, manages routing, handles voice interaction services, and provides the main user interface.

## Key Components

### CoreService.kt
**Location**: `packages/core/CoreModule/src/main/java/com/example/sapphireassistantframework/CoreService.kt`

**Purpose**: Central service that manages routing, module communication, and file services.

**Key Methods**:
- `sortMail()`: Routes incoming intents based on system state
- `handleRoute()`: Manages message routing between modules
- `fileService()`: Handles file operations and transfers
- `initialize()`: Initializes the system with registered modules
- `startBackgroundServices()`: Launches background services

**Functionality**: Acts as the central coordinator for all module interactions and system state management.

### CoreVoiceInteractionService.kt
**Location**: `packages/core/CoreModule/src/main/java/com/example/sapphireassistantframework/voiceassistant/CoreVoiceInteractionService.kt`

**Purpose**: Voice interaction service that integrates with Android's voice assistant framework.

**Key Methods**:
- `onCreate()`: Initializes voice services and starts hotword detection
- `startVoskHotwordDetector()`: Launches speech recognition service
- `onVoskHotwordDetection()`: Handles hotword detection events

**Functionality**: Provides voice assistant integration using Android's VoiceInteractionService.

### CoreVoiceInteractionSession.kt
**Location**: `packages/core/CoreModule/src/main/java/com/example/sapphireassistantframework/voiceassistant/CoreVoiceInteractionSession.kt`

**Purpose**: Manages voice interaction sessions and UI components.

**Key Methods**:
- `onCreate()`: Sets up the voice interaction session

**Functionality**: Simple session manager for voice interactions.

## User Interface Components

### CoreSimpleActivity.kt & CoreSettingsActivity.kt
**Location**: `packages/core/CoreModule/src/main/java/com/example/sapphireassistantframework/`

Main application activities providing user interface for:
- System configuration
- Module management
- Settings and preferences

## Configuration Files
**Location**: `packages/core/CoreModule/src/main/assets/`

- `sample-core-config.conf`: Sample configuration file for core settings

## Resources
**Location**: `packages/core/CoreModule/src/main/res/`

- Complete Android app resources including:
  - Launcher icons
  - UI layouts (`core_activity.xml`, `settings_activity.xml`)
  - Themes and styling
  - String resources
  - File provider configuration

## Module Type
**CORE** - The central coordination module that all other modules communicate through.

## Key Features
- **Central Routing**: All inter-module communication passes through the core
- **Voice Integration**: Android VoiceInteractionService integration
- **Module Management**: Registration and lifecycle management of all modules
- **File Services**: Centralized file sharing between modules
- **Background Services**: Management of background processing services
- **User Interface**: Main application interface and settings

## Package Dependencies
- `packages/framework/ComponentFramework/` - Base service functionality
- `packages/processing/VoskSTTModule/` - Speech recognition
- Android VoiceInteractionService
- Android Activities and UI components

## Build Configuration
```gradle
// In settings.gradle
include ':packages:core:CoreModule'

// Build command
./gradlew :packages:core:CoreModule:build
```

## Architecture Role
The CoreModule serves as:
- **Message Router**: Routes all communications between modules
- **System Coordinator**: Manages overall system state and initialization
- **Voice Interface**: Provides voice interaction capabilities
- **User Interface**: Main application interface
- **Service Manager**: Coordinates background services across modules

## Integration Points
- **Skills**: Routes processed intents to `packages/skills/*`
- **Processing**: Coordinates with `packages/processing/*` modules
- **Integrations**: Manages `packages/integrations/*` connections
- **Framework**: Built on `packages/framework/ComponentFramework/`

## Development Notes
- **Central Hub**: All module communication passes through here
- **Build Dependency**: Requires ComponentFramework to be built first
- **Main Application**: Contains the primary user interface
- **Voice Integration**: Handles Android's VoiceInteractionService

## Status
**Active** - Fully implemented core functionality with voice integration and module coordination.