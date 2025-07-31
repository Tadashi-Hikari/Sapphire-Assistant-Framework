# Sapphire Assistant Framework Documentation

This documentation module provides detailed information about each component of the Sapphire Assistant Framework.

## Module Documentation

- [CalendarSkill](CalendarSkill.md) - Calendar management and scheduling functionality
- [ComponentFramework](ComponentFramework.md) - Core framework services and registration
- [CoreModule](CoreModule.md) - Main application core with voice interaction services
- [MultiprocessModule](MultiprocessModule.md) - Multi-process communication handling
- [MycroftModule](MycroftModule.md) - Mycroft AI integration utilities
- [ProcessorModule](ProcessorModule.md) - Natural language processing with Stanford CoreNLP
- [TaskerModule](TaskerModule.md) - Android Tasker integration
- [TermuxModule](TermuxModule.md) - Termux terminal emulator integration
- [VoskSTTModule](VoskSTTModule.md) - Speech-to-text using Vosk offline recognition

## Architecture Overview

The Sapphire Assistant Framework follows a modular architecture where each module is an independent Android module that can be built and deployed separately. This design allows for:

- Independent testing and development of features
- Selective installation of functionality
- Better separation of concerns
- Easier maintenance and updates

## Building the Project

Each module must be built separately. The project supports both individual APK builds and single APK builds controlled by the `project.ext.single` flag in the root `build.gradle`.

## Integration with Athena

This framework is designed to work as a plugin system for [Athena](https://github.com/Tadashi-Hikari/Athena), providing extensible assistant capabilities.