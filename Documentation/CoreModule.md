# CoreModule

## Overview
The CoreModule is the central nervous system of the Sapphire Assistant Framework. It coordinates all module interactions, manages routing, handles voice interaction services, and provides the main user interface.

## Key Components

### CoreService.kt
**Purpose**: Central service that manages routing, module communication, and file services.

**Key Methods**:
- `sortMail()`: Routes incoming intents based on system state
- `handleRoute()`: Manages message routing between modules
- `fileService()`: Handles file operations and transfers
- `initialize()`: Initializes the system with registered modules
- `startBackgroundServices()`: Launches background services

**Functionality**: Acts as the central coordinator for all module interactions and system state management.

### CoreVoiceInteractionService.kt
**Purpose**: Voice interaction service that integrates with Android's voice assistant framework.

**Key Methods**:
- `onCreate()`: Initializes voice services and starts hotword detection
- `startVoskHotwordDetector()`: Launches speech recognition service
- `onVoskHotwordDetection()`: Handles hotword detection events

**Functionality**: Provides voice assistant integration using Android's VoiceInteractionService.

### CoreVoiceInteractionSession.kt
**Purpose**: Manages voice interaction sessions and UI components.

**Key Methods**:
- `onCreate()`: Sets up the voice interaction session

**Functionality**: Simple session manager for voice interactions.

## User Interface Components

### CoreSimpleActivity.kt & CoreSettingsActivity.kt
Main application activities providing user interface for:
- System configuration
- Module management
- Settings and preferences

## Configuration Files
- `sample-core-config.conf`: Sample configuration file for core settings

## Resources
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

## Dependencies
- ComponentFramework for base service functionality
- VoskSTTModule for speech recognition
- Android VoiceInteractionService
- Android Activities and UI components

## Architecture Role
The CoreModule serves as:
- **Message Router**: Routes all communications between modules
- **System Coordinator**: Manages overall system state and initialization
- **Voice Interface**: Provides voice interaction capabilities
- **User Interface**: Main application interface
- **Service Manager**: Coordinates background services across modules

## Status
**Active** - Fully implemented core functionality with voice integration and module coordination.