# ProcessorModule

## Overview
The ProcessorModule provides natural language processing capabilities using Stanford CoreNLP. It handles intent classification, named entity recognition (NER), and machine learning model training for the Sapphire Assistant Framework.

## Key Components

### ProcessorCentralService.kt
**Purpose**: Main text processing service using Stanford CoreNLP for intent classification.

**Key Methods**:
- `process()`: Classifies text input using trained models
- `loadClassifier()`: Loads or requests intent classification models
- `requestFiles()`: Requests training files from core

**Functionality**: Natural language processing for intent recognition using machine learning.

### EntityTrainingService.kt
**Purpose**: Named Entity Recognition (NER) training and processing.

**Key Methods**:
- `processEntities()`: Processes text for entity extraction
- `trainNERClassifier()`: Trains CRF classifier for entity recognition
- `knownEntityProperties()`: Configures NER training properties

**Functionality**: Handles entity extraction using Stanford NLP's CRF classifier.

### ProcessorTrainingService.kt
**Purpose**: Trains intent classification models from training data.

**Key Methods**:
- `train()`: Orchestrates the training process
- `trainIntentClassifier()`: Trains Stanford CoreNLP classifier
- `cacheTrainingFiles()`: Converts URI data to local files
- `combineFiles()`: Merges multiple training files

**Functionality**: Machine learning model training for intent classification.

## Dependencies
- `stanford-corenlp-4.1.0.jar`: Stanford CoreNLP library
- `english.all.3class.distsim.crf.ser.gz`: Pre-trained NER model
- `english.all.3class.distsim.prop`: NER configuration properties

## Configuration Files
- `processor.conf`: Configuration for processing operations

## Module Type
**PROCESSOR** - Handles natural language understanding and machine learning.

## Key Features
- **Intent Classification**: Machine learning-based intent recognition
- **Named Entity Recognition**: Extraction of entities (names, dates, locations, etc.)
- **Model Training**: Automated training of classification models
- **Stanford CoreNLP Integration**: Full integration with Stanford's NLP toolkit
- **File Management**: Handles training data and model files
- **Multi-language Support**: Configurable for different languages

## Processing Pipeline
1. **Text Input**: Receives raw text from voice or other input modules
2. **Preprocessing**: Cleans and prepares text for analysis
3. **Entity Extraction**: Identifies and extracts named entities
4. **Intent Classification**: Determines user intent using trained models
5. **Result Formatting**: Packages results for routing to appropriate skills

## Training Process
1. **Data Collection**: Gathers training examples from various modules
2. **File Combination**: Merges training data from multiple sources
3. **Model Training**: Uses Stanford CoreNLP to train classifiers
4. **Model Validation**: Tests trained models for accuracy
5. **Model Deployment**: Makes trained models available for processing

## Machine Learning Models
- **Intent Classifier**: Determines what the user wants to do
- **NER Classifier**: Extracts entities like dates, names, locations
- **Custom Models**: Support for domain-specific trained models

## Integration Points
- **Input Modules**: Receives text from VoskSTTModule and other sources
- **Skill Modules**: Routes classified intents to appropriate skills
- **Core Module**: Coordinates with core for file management and routing
- **Training Data**: Receives training examples from all skill modules

## Status
**Active** - Fully implemented NLP processing with Stanford CoreNLP integration and model training capabilities.