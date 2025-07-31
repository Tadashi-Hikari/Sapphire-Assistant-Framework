# Sapphire Assistant Framework Documentation

This documentation provides comprehensive information about the Sapphire Assistant Framework's modular architecture and development.

## 📚 Getting Started

- **[Developer Guide](DEVELOPER_GUIDE.md)** - Complete onboarding guide for new developers
- **[README](../README.md)** - Project overview and roadmap

## 🏗️ Architecture Documentation

### Core Framework
- **[CoreModule](CoreModule.md)** - Central coordinator and voice interaction services
- **[ComponentFramework](ComponentFramework.md)** - Base classes and communication protocols

### Processing Modules
- **[ProcessorModule](ProcessorModule.md)** - Natural language processing with Stanford CoreNLP
- **[VoskSTTModule](VoskSTTModule.md)** - Speech-to-text using Vosk offline recognition
- **[MycroftModule](MycroftModule.md)** - Mycroft AI integration utilities
- **[MultiprocessModule](MultiprocessModule.md)** - Parallel processing coordination

### Skills
- **[CalendarSkill](CalendarSkill.md)** - Calendar management and scheduling functionality

### Integrations
- **[TermuxModule](TermuxModule.md)** - Termux terminal emulator integration
- **[TaskerModule](TaskerModule.md)** - Android Tasker integration

## 🚀 New Project Structure

The framework has been refactored into a more developer-friendly monorepo structure:

```
sapphire-assistant-framework/
├── packages/                    # Main codebase
│   ├── core/                   # Central coordination
│   ├── framework/              # Base classes & utilities
│   ├── processing/             # AI/ML processing modules
│   ├── skills/                 # User-facing functionality
│   └── integrations/           # External system connections
├── examples/                   # Learning examples
│   └── HelloWorldSkill/        # Your first skill template
├── docs/                       # This documentation
└── tools/                      # Development utilities
```

## 🎯 For New Developers

1. **Start Here**: [Developer Guide](DEVELOPER_GUIDE.md)
2. **Example Code**: `examples/HelloWorldSkill/`
3. **Your First Skill**: Follow the tutorial in the Developer Guide
4. **Architecture**: Read the Core and Framework documentation

## 🔧 Architecture Overview

The Sapphire Assistant Framework uses a **modular, service-oriented architecture**:

- **Independent Modules**: Each package is a separate Android module
- **Service Communication**: Modules communicate via Android Intents  
- **Extensible Design**: Easy to add new skills and integrations
- **Offline-First**: Privacy-focused with local processing
- **Plugin Architecture**: Designed to work with [Athena](https://github.com/Tadashi-Hikari/Athena)

## 🛠️ Development Benefits

- **Simplified Onboarding**: HelloWorldSkill example and comprehensive guide
- **Modern Build System**: Updated Gradle with centralized dependency management
- **Clear Structure**: Logical grouping of related functionality
- **Better Documentation**: Module-specific guides with examples
- **Progressive Learning**: Learn one package at a time

## 📦 Package Categories

| Package | Purpose | Examples |
|---------|---------|----------|
| **Core** | Central coordination | CoreModule |
| **Framework** | Base classes | ComponentFramework |
| **Processing** | AI/ML capabilities | NLP, Speech Recognition |
| **Skills** | User functionality | Calendar, Weather, Timer |
| **Integrations** | External connections | Termux, Tasker, APIs |

## 🚦 Building the Project

```bash
# Build core framework first
./gradlew :packages:framework:ComponentFramework:build
./gradlew :packages:core:CoreModule:build

# Build example skill
./gradlew :examples:HelloWorldSkill:build

# Build specific packages
./gradlew :packages:skills:CalendarSkill:build
```

## 💡 Integration with Athena

This framework is designed as a plugin system for [Athena](https://github.com/Tadashi-Hikari/Athena), providing:
- Extensible assistant capabilities
- Modular skill development
- Advanced processing features
- Custom integration options