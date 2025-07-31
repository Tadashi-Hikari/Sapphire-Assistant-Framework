# HelloWorldSkill - Your First Sapphire Skill

This is a simple example skill that demonstrates the basic patterns for creating skills in the Sapphire Assistant Framework.

## What This Skill Does

- Responds to greetings like "hello", "hi", "good morning"
- Can include names in greetings: "hello John"
- Provides introductions when asked "who are you"
- Demonstrates basic intent processing and response generation

## Key Components

### 1. HelloWorldService.kt
The main skill service that processes user requests and generates responses.

**Key Methods:**
- `processIntent()` - Routes incoming intents to appropriate handlers
- `handleGreeting()` - Processes greeting requests
- `handleIntroduction()` - Handles introduction requests
- `returnResponse()` - Sends responses back to the framework

### 2. HelloWorldPostOfficeService.kt
Handles module registration and provides training data to the framework.

**Key Methods:**
- `registerModule()` - Registers the skill with the core
- `sendFileNames()` - Lists available configuration files
- `coreTransferFile()` - Transfers files to core for processing

### 3. Configuration Files

**hello.intent** - Intent patterns that trigger this skill
**greeting.conf** - Configuration and response templates

## How to Use This as a Template

### Step 1: Copy the Structure
```bash
cp -r examples/HelloWorldSkill packages/skills/YourSkillName
```

### Step 2: Rename Components
1. Rename package in all .kt files
2. Update AndroidManifest.xml with new service names
3. Change class names to match your skill

### Step 3: Modify Functionality
1. Update `processIntent()` to handle your skill's actions
2. Add new handler methods for your skill's features
3. Update intent patterns and configuration files

### Step 4: Register Your Skill
Add your skill to `settings.gradle`:
```gradle
include ':packages:skills:YourSkillName'
```

## Example Interactions

**User:** "Hello"
**Assistant:** "Hello! I'm the Sapphire Assistant. How can I help you today?"

**User:** "Hello Sarah"  
**Assistant:** "Hello Sarah! I'm the Sapphire Assistant. How can I help you today?"

**User:** "Who are you?"
**Assistant:** "I'm the Sapphire Assistant Framework, an open-source voice assistant that works completely offline!"

## Architecture Pattern

This skill follows the standard Sapphire Framework pattern:

1. **Intent Recognition** - Core processes user speech and identifies this skill
2. **Request Routing** - Core sends intent to HelloWorldService
3. **Processing** - Skill processes the request and generates response
4. **Response** - Skill sends response back through framework
5. **Output** - Core handles text-to-speech or display

## Next Steps

After understanding this example, you can:

1. **Study CalendarSkill** - More complex skill with entity extraction
2. **Explore ProcessorModule** - Understand NLP and intent classification  
3. **Create Your Own Skill** - Build something specific to your needs

## Common Patterns Demonstrated

- Extending `SapphireFrameworkService` and `SapphireFrameworkRegistrationService`
- Processing intents with parameters
- Generating dynamic responses
- Handling registration and file transfers
- Using configuration files for flexible behavior

This example provides the foundation for understanding how skills work in the Sapphire Assistant Framework!