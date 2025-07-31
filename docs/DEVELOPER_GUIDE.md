# Sapphire Assistant Framework - Developer Guide

Welcome to the Sapphire Assistant Framework! This guide will help you understand the project structure, get set up for development, and create your first skill.

## Quick Start (15 minutes)

### 1. Understanding the Architecture (5 minutes)

The Sapphire Framework uses a **modular, service-oriented architecture**:

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   User Speech   │───▶│  VoskSTTModule  │───▶│ ProcessorModule │
└─────────────────┘    │ (Speech-to-Text)│    │ (NLP/Intent)    │
                       └─────────────────┘    └─────────────────┘
                                                        │
┌─────────────────┐    ┌─────────────────┐             ▼
│   Text Response │◀───│   CoreModule    │◀────────────────────┐
└─────────────────┘    │  (Coordinator)  │                     │
                       └─────────────────┘                     ▼
                                ▲                    ┌─────────────────┐
                                │                    │   Skill Module  │
                                │                    │ (CalendarSkill) │
                                └────────────────────┘                 │
                                                     └─────────────────┘
```

**Key Concept**: Each module is an independent Android service that communicates via Intents.

### 2. Project Structure (5 minutes)

```
sapphire-assistant-framework/
├── packages/
│   ├── core/                    # 🏠 Central coordinator
│   │   └── CoreModule/
│   ├── framework/               # 🔧 Base classes & utilities
│   │   └── ComponentFramework/
│   ├── processing/              # 🧠 AI/ML processing
│   │   ├── ProcessorModule/     # NLP & intent classification
│   │   ├── VoskSTTModule/       # Speech-to-text
│   │   ├── MycroftModule/       # Intent parsing utilities
│   │   └── MultiprocessModule/  # Parallel processing
│   ├── skills/                  # 🎯 User-facing functionality
│   │   └── CalendarSkill/
│   └── integrations/            # 🔌 External system connections
│       ├── TermuxModule/
│       └── TaskerModule/
├── examples/                    # 📚 Learning examples
│   └── HelloWorldSkill/         # Start here!
├── docs/                        # 📖 Documentation
└── tools/                       # 🛠️ Development utilities
```

### 3. Your First Build (5 minutes)

**Prerequisites:**
- Android Studio
- Android SDK 24+ (API level 24)
- Java 8+

**Build Steps:**
```bash
# Clone and enter directory
cd Sapphire-Assistant-Framework

# Build the core modules first
./gradlew :packages:framework:ComponentFramework:build
./gradlew :packages:core:CoreModule:build

# Build the HelloWorld example
./gradlew :examples:HelloWorldSkill:build
```

## Development Learning Path

### Phase 1: Understanding (Week 1)

#### Day 1-2: Architecture Overview
1. **Read the documentation**:
   - [README.md](../README.md) - Project overview
   - [ComponentFramework.md](ComponentFramework.md) - Base architecture
   - [CoreModule.md](CoreModule.md) - Central coordination

2. **Study the HelloWorld example**:
   - Read `examples/HelloWorldSkill/README.md`
   - Examine `HelloWorldService.kt` and `HelloWorldPostOfficeService.kt`
   - Understand the intent patterns in `hello.intent`

#### Day 3-5: Core Concepts
1. **Service Communication**: How modules talk to each other
2. **Intent Processing**: How user requests are handled
3. **Registration System**: How modules join the framework
4. **File Sharing**: How modules exchange data

**Hands-on Exercise**: Build and run HelloWorldSkill
```bash
./gradlew :examples:HelloWorldSkill:assembleDebug
```

### Phase 2: Development Setup (Week 2)

#### Development Environment Setup

1. **Android Studio Configuration**:
   ```
   File → Project Structure → Modules
   - Verify all package modules are recognized
   - Check build configurations
   ```

2. **Create Your Workspace**:
   ```bash
   # Create your development branch
   git checkout -b feature/my-first-skill
   
   # Set up your skill directory
   cp -r examples/HelloWorldSkill packages/skills/MyFirstSkill
   ```

3. **Update settings.gradle**:
   ```gradle
   include ':packages:skills:MyFirstSkill'
   ```

#### Your First Custom Skill

Follow the **"Your First Skill Tutorial"** below.

### Phase 3: Advanced Development (Month 2+)

1. **Study Complex Examples**:
   - [CalendarSkill.md](CalendarSkill.md) - Entity extraction and CRUD operations
   - [ProcessorModule.md](ProcessorModule.md) - NLP and machine learning

2. **Integration Development**:
   - [TermuxModule.md](TermuxModule.md) - External app integration
   - Create your own integrations

3. **Processing Pipeline**:
   - [VoskSTTModule.md](VoskSTTModule.md) - Speech recognition
   - [MycroftModule.md](MycroftModule.md) - Intent parsing

## Your First Skill Tutorial

Let's create a **CalculatorSkill** that can perform basic math operations.

### Step 1: Create the Skill Structure

```bash
# Copy HelloWorld template
cp -r examples/HelloWorldSkill packages/skills/CalculatorSkill

# Add to build system
echo "include ':packages:skills:CalculatorSkill'" >> settings.gradle
```

### Step 2: Rename and Customize

**Update `build.gradle`** - Already configured correctly

**Update `AndroidManifest.xml`**:
```xml
<service android:name=".CalculatorService" ... />
<service android:name=".CalculatorPostOfficeService" ... />
```

**Rename Java files**:
```bash
cd packages/skills/CalculatorSkill/src/main/java/com/example/helloworldskill/
mv HelloWorldService.kt CalculatorService.kt
mv HelloWorldPostOfficeService.kt CalculatorPostOfficeService.kt
```

### Step 3: Implement Calculator Logic

**CalculatorService.kt**:
```kotlin
package com.example.calculatorskill

class CalculatorService : SapphireFrameworkService() {
    
    private fun processIntent(intent: Intent) {
        val action = intent.getStringExtra("action") ?: ""
        val utterance = intent.getStringExtra("utterance") ?: ""
        
        when (action) {
            "calculate" -> handleCalculation(intent)
            else -> handleUnknown(intent)
        }
    }
    
    private fun handleCalculation(intent: Intent) {
        val number1 = intent.getStringExtra("number1")?.toDoubleOrNull() ?: 0.0
        val number2 = intent.getStringExtra("number2")?.toDoubleOrNull() ?: 0.0
        val operation = intent.getStringExtra("operation") ?: ""
        
        val result = when (operation) {
            "plus", "add" -> number1 + number2
            "minus", "subtract" -> number1 - number2
            "times", "multiply" -> number1 * number2
            "divided by", "divide" -> if (number2 != 0.0) number1 / number2 else null
            else -> null
        }
        
        val response = if (result != null) {
            "$number1 $operation $number2 equals $result"
        } else {
            "I couldn't calculate that. Please try again."
        }
        
        returnResponse(intent, response)
    }
}
```

### Step 4: Create Intent Patterns

**assets/calculate.intent**:
```
what is (number1) (operation) (number2)
calculate (number1) (operation) (number2)
(number1) (operation) (number2)
what's (number1) (operation) (number2)
```

### Step 5: Test Your Skill

```bash
# Build your skill
./gradlew :packages:skills:CalculatorSkill:build

# Install and test
# (Actual testing requires full framework deployment)
```

## Common Patterns & Best Practices

### Service Architecture Pattern
```kotlin
class YourSkillService : SapphireFrameworkService() {
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let { processIntent(it) }
        return START_NOT_STICKY
    }
    
    private fun processIntent(intent: Intent) {
        val action = intent.getStringExtra("action") ?: ""
        // Route to appropriate handler
    }
    
    private fun returnResponse(originalIntent: Intent, response: String) {
        val responseIntent = Intent().apply {
            putExtra("response", response)
            putExtra("success", true)
        }
        returnSapphireService(originalIntent, responseIntent)
    }
}
```

### Registration Pattern
```kotlin
class YourSkillPostOfficeService : SapphireFrameworkRegistrationService() {
    
    override fun registerModule(intent: Intent) {
        registerModuleType("SKILL")
        registerVersion("1.0.0")
        
        val responseIntent = Intent().apply {
            putExtra("module", "YourSkill")
            putExtra("type", "SKILL")
            putExtra("route", "YourSkill")
        }
        
        returnSapphireService(intent, responseIntent)
    }
}
```

## Debugging & Development Tips

### 1. Logging
```kotlin
companion object {
    private const val TAG = "YourSkillService"
}

Log.d(TAG, "Processing intent: $action")
Log.i(TAG, "Response: $response")
Log.w(TAG, "Unknown action: $action")
Log.e(TAG, "Error processing: ${e.message}")
```

### 2. Testing Individual Modules
```bash
# Build specific module
./gradlew :packages:skills:YourSkill:build

# Run tests
./gradlew :packages:skills:YourSkill:test
```

### 3. Intent Debugging
Use Android Studio's logcat to monitor Intent flow between services.

## Getting Help

1. **Documentation**: Check `docs/` for module-specific information
2. **Examples**: Study `examples/HelloWorldSkill` and `packages/skills/CalendarSkill`
3. **Code Comments**: Most services have detailed inline documentation
4. **Community**: Join the community on Reddit or Matrix (see main README)

## What's Next?

After completing your first skill:

1. **Add Entity Recognition** - Extract dates, names, numbers from user input
2. **Create Integrations** - Connect to external APIs or Android apps
3. **Build Processing Modules** - Add AI/ML capabilities
4. **Contribute Back** - Share your skills with the community

Welcome to the Sapphire Assistant Framework community! 🚀