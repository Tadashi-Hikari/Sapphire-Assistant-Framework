# MycroftModule

## Overview
The MycroftModule provides utilities for parsing and expanding Mycroft AI-style intent syntax. It enables compatibility with Mycroft Padatious intent definitions by converting bracket-style syntax into expanded sentence variations.

## Key Components

### BracketExpander.kt
**Purpose**: Parses Mycroft-style intent syntax with bracket expansion.

**Key Methods**:
- `parseSentenceList()`: Processes lists of sentences with bracket syntax
- `expandBrackets()`: Recursively expands bracket expressions (A | B) syntax

**Functionality**: Converts Mycroft Padatious-style intent definitions into expanded sentence variations.

## Module Type
**UTILITY** - Provides parsing and expansion utilities for other modules.

## Key Features
- **Bracket Expansion**: Converts `(option A | option B)` syntax into multiple sentences
- **Recursive Processing**: Handles nested bracket expressions
- **Mycroft Compatibility**: Enables use of existing Mycroft intent definitions
- **Sentence Generation**: Automatically generates all possible sentence variations

## Syntax Support
The module supports Mycroft's bracket expansion syntax:
- `(hello | hi | hey)` expands to three separate variations
- `turn (on | off) the (lights | lamp)` expands to four combinations
- Nested brackets are supported for complex expressions

## Use Cases
- **Intent Definition**: Converting Mycroft intent files for use in the framework
- **Training Data Generation**: Expanding limited training examples into comprehensive datasets
- **Template Processing**: Processing template-based intent definitions
- **Migration Support**: Helping migrate from Mycroft to Sapphire Framework

## Dependencies
- Standard Kotlin/Java libraries for string processing
- Regular expression support for bracket parsing

## Integration
Other modules can use the BracketExpander to:
- Process intent definition files
- Generate training data variations
- Parse configuration files with bracket syntax
- Expand template-based responses

## Example Usage
```kotlin
val expander = BracketExpander()
val input = listOf("turn (on | off) the lights")
val expanded = expander.parseSentenceList(input)
// Result: ["turn on the lights", "turn off the lights"]
```

## Status
**Active** - Fully implemented bracket expansion functionality for Mycroft compatibility.