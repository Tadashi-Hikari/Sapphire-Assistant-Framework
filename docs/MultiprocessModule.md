# MultiprocessModule

**Location**: `packages/processing/MultiprocessModule/`  
**Package Category**: **Processing** - Parallel coordination

## Overview
The MultiprocessModule enables parallel processing of requests across multiple modules in the Sapphire Assistant Framework. It allows for concurrent execution of multiple skills or services and aggregates their results.

## Key Components

### MultiprocessService.kt
**Location**: `packages/processing/MultiprocessModule/src/main/java/com/example/multiprocessmodule/MultiprocessService.kt`

**Purpose**: Manages parallel processing and coordination of multiple module requests.

**Key Methods**:
- `handleNewMultiprocessIntent()`: Processes new multiprocess requests
- `evaluateReturningIntent()`: Handles responses from parallel processes
- `sendUltimateResult()`: Combines results from multiple modules
- `regexRouteString()`: Parses multiprocess routing syntax

**Functionality**: Enables parallel processing of requests across multiple modules with result aggregation.

### MultiprocessPostOfficeService.kt
**Location**: `packages/processing/MultiprocessModule/src/main/java/com/example/multiprocessmodule/MultiprocessPostOfficeService.kt`

**Purpose**: Simple registration service for the multiprocess module.

**Key Methods**:
- `registerModule()`: Registers the multiprocess module as a MULTIPROCESS type

**Functionality**: Handles module registration and routing passthrough.

## Configuration Files
**Location**: `packages/processing/MultiprocessModule/src/main/assets/`

- `multiprocess.conf`: Configuration for parallel processing operations

## Module Type
**MULTIPROCESS** - Specialized module for coordinating parallel execution across other modules.

## Key Features
- **Parallel Execution**: Simultaneous processing of requests across multiple modules
- **Result Aggregation**: Combines results from multiple parallel processes
- **Route Parsing**: Interprets multiprocess routing syntax
- **Coordination**: Manages timing and synchronization of parallel operations
- **Response Handling**: Processes and combines responses from multiple sources

## Use Cases
- **Multi-skill Queries**: Questions that might be answered by multiple skills
- **Redundant Processing**: Running the same query through multiple processors for reliability
- **Parallel Validation**: Validating results across multiple modules
- **Performance Optimization**: Executing independent operations simultaneously

## Package Dependencies
- `packages/framework/ComponentFramework/` - Base service functionality
- `packages/core/CoreModule/` - Routing and communication
- `packages/skills/*` - Various skill modules for parallel execution
- `packages/processing/*` - Other processing modules

## Build Configuration
```gradle
// In settings.gradle
include ':packages:processing:MultiprocessModule'

// Build command
./gradlew :packages:processing:MultiprocessModule:build
```

## Architecture Role
The MultiprocessModule serves as:
- **Parallel Coordinator**: Manages simultaneous execution across modules
- **Result Aggregator**: Combines outputs from multiple parallel processes
- **Performance Enhancer**: Reduces overall processing time through parallelization
- **Reliability Improver**: Enables redundant processing for critical operations

## Integration Points
- **Core Communication**: Routes through `packages/core/CoreModule/`
- **Skill Coordination**: Manages parallel execution of `packages/skills/*`
- **Processing Pipeline**: Works with other `packages/processing/*` modules
- **Framework Foundation**: Built on `packages/framework/ComponentFramework/`

## Processing Flow
1. Receives multiprocess request with routing information
2. Parses routing syntax to identify target modules
3. Dispatches requests to multiple modules simultaneously
4. Collects responses as they arrive
5. Aggregates results when all processes complete
6. Returns combined result to requesting module

## Development Notes
- **Processing Category**: Part of the processing pipeline architecture
- **Parallel Execution**: Enables simultaneous module operations
- **Result Coordination**: Handles complex response aggregation
- **Performance Critical**: Optimizes overall system response time

## Status
**Active** - Implemented with parallel processing capabilities and result aggregation.