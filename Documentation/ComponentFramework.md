# ComponentFramework Module

## Overview
The ComponentFramework module provides the foundational services and abstract base classes that all other modules in the Sapphire Assistant Framework extend. It implements core routing, validation, and communication protocols.

## Key Components

### SapphireCoreService.kt
**Purpose**: Abstract base class for core services with routing and validation capabilities.

**Key Methods**:
- `passthroughService()`: Validates and forwards service requests
- `expandRoute()`: Expands route aliases to actual module paths
- `validatePostage()`: Validates message routing information
- `startRegistrationService()`: Initiates module registration process

**Functionality**: Provides core routing and validation logic for the framework.

### SapphireFrameworkService.kt
**Purpose**: Abstract base service class providing common framework functionality.

**Key Methods**:
- `loadTable()`: Loads JSON configuration tables
- `broadcastStatus()`: Sends status updates via broadcasts
- `validatePostage()`: Handles message routing validation
- `returnSapphireService()`: Routes messages back to core

**Functionality**: Foundation service class with logging, messaging, and routing capabilities.

### SapphireFrameworkRegistrationService.kt
**Purpose**: Handles module registration and data exchange between modules.

**Key Methods**:
- `registerModule()`: Registers modules with the core system
- `registerVersion()`, `registerModuleType()`: Sets module metadata
- `retrieveData()`: Extracts data from asset files for processing
- `convertAssetToFile()`: Converts asset files to temporary files

**Functionality**: Manages the registration lifecycle and data preparation for modules.

## Module Type
**FRAMEWORK** - Core infrastructure module that other modules depend on.

## Key Features
- **Service Routing**: Sophisticated message routing system between modules
- **Validation**: Request validation and error handling
- **Registration**: Module registration and discovery system
- **File Management**: Asset file handling and conversion
- **Logging**: Comprehensive logging throughout the framework
- **Broadcasting**: Status update broadcasting system

## Dependencies
- Android Services framework
- JSON processing capabilities
- File I/O operations

## Usage
Other modules extend the abstract classes provided by this framework:
- Extend `SapphireFrameworkService` for basic service functionality
- Extend `SapphireCoreService` for core routing capabilities
- Use `SapphireFrameworkRegistrationService` for module registration

## Architecture Role
This module forms the backbone of the inter-module communication system, providing:
- Standardized service interfaces
- Message routing and validation
- Module lifecycle management
- Common utilities and logging