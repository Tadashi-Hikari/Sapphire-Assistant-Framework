# CalendarSkill Module

## Overview
The CalendarSkill module provides calendar management functionality for the Sapphire Assistant Framework. It demonstrates how to implement a skill module that can handle calendar-related voice commands and operations.

## Key Components

### CalendarService.kt
**Purpose**: Main service for handling calendar operations including create, retrieve, update, and delete actions.

**Key Methods**:
- `onStartCommand()`: Processes calendar intents and performs CRUD operations
- `getManditoryVariables()`: Extracts required parameters (action, dateTime, event)
- `checkForOptional()`: Handles optional parameters like description
- `populateCalendar()`: Manages date/time ranges for events

**Functionality**: Currently logs calendar actions but appears to be a prototype without actual calendar integration.

### CalendarPostOfficeService.kt
**Purpose**: Handles module registration and file data management for the calendar skill.

**Key Methods**:
- `sendFileNames()`: Provides list of files this module offers
- `coreTransferFile()`: Manages file transfers between modules
- `registerModule()`: Registers the calendar module with the core

## Configuration Files
- `alarm.intent`: Intent patterns for alarm-related commands
- `calendar.conf`: Configuration for calendar operations
- `date.entity`: Date entity recognition patterns
- `get.intent`: Intent patterns for retrieving calendar information
- `set.intent`: Intent patterns for setting calendar events

## Module Type
**SKILL** - This module demonstrates the skill architecture for domain-specific functionality.

## Dependencies
- Android Calendar Provider (not currently implemented)
- ComponentFramework for base service functionality
- CoreModule for routing and communication

## Usage
This module serves as a template for implementing calendar functionality and can be extended to integrate with Android's Calendar Provider or other calendar systems.

## Status
**Prototype** - Basic structure implemented but lacks actual calendar integration.