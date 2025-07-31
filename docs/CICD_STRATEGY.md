# Sapphire Assistant Framework - CI/CD Strategy

## Overview

This document outlines a comprehensive CI/CD strategy for the Sapphire Assistant Framework, a multi-module Android project with complex dependencies including native libraries (Vosk/Kaldi), ML models, and modular architecture. The strategy addresses the unique challenges of building, testing, and distributing a service-oriented Android assistant framework.

## Current State Analysis

### Project Structure
- **Multi-module Gradle project**: 11 modules across core, processing, skills, and integrations
- **Hybrid build modes**: Single APK vs. individual module APKs (controlled by `project.single` flag)
- **Native dependencies**: Kaldi speech recognition, Stanford CoreNLP
- **Large assets**: ML models and language data files
- **Target platforms**: Android 7.1+ (API 25-30)

### Existing Infrastructure
- **Repository**: GitHub at `https://github.com/Tadashi-Hikari/Sapphire-Assistant-Framework`
- **Build system**: Gradle 6.5 with Android Gradle Plugin
- **CI/CD**: None currently implemented
- **Distribution**: Manual builds only

### Build Challenges
- Large asset files require special handling
- Native library compilation and packaging
- Complex module dependencies and optional builds
- Multi-variant builds (single vs. modular)
- Long build times due to ML model processing

## CI/CD Architecture

### Pipeline Overview
```
┌─────────────┐    ┌──────────────┐    ┌─────────────┐    ┌──────────────┐
│   Source    │───▶│      CI      │───▶│     CD      │───▶│ Distribution │
│   Control   │    │   Pipeline   │    │  Pipeline   │    │   Channels   │
└─────────────┘    └──────────────┘    └─────────────┘    └──────────────┘
      │                     │                 │                    │
      │              ┌──────▼──────┐   ┌─────▼─────┐       ┌──────▼──────┐
      │              │   Build     │   │  Deploy   │       │   GitHub    │
      │              │   Test      │   │  Package  │       │  Releases   │
      │              │   Analyze   │   │  Sign     │       │     +       │
      │              └─────────────┘   └───────────┘       │   F-Droid   │
      │                                                    └─────────────┘
      ▼
┌─────────────┐
│   Branch    │
│  Strategy   │
└─────────────┘
```

## Branch Strategy

### GitFlow Model
- **`main`**: Production-ready releases only
- **`development`**: Integration branch for ongoing work  
- **`feature/*`**: Individual features and modules
- **`release/*`**: Release preparation and stabilization
- **`hotfix/*`**: Critical production fixes

### Protection Rules
```yaml
main:
  - Require PR reviews (2 reviewers)
  - Require status checks (CI must pass)
  - Require up-to-date branches
  - Restrict force pushes
  - No direct commits

development:
  - Require PR reviews (1 reviewer)
  - Require status checks (CI must pass)
  - Allow force pushes from admins only
```

## Continuous Integration (CI)

### GitHub Actions Workflow Structure

#### 1. Build Matrix Strategy
```yaml
strategy:
  matrix:
    build-mode: [single, modular]
    api-level: [25, 30]
    arch: [x86, x86_64]
  fail-fast: false
```

#### 2. Core CI Workflow (`.github/workflows/ci.yml`)

```yaml
name: CI Pipeline
on:
  push:
    branches: [main, development]
  pull_request:
    branches: [main, development]

env:
  GRADLE_OPTS: -Dorg.gradle.daemon=false -Dorg.gradle.workers.max=2
  
jobs:
  # Job 1: Code Quality and Static Analysis
  static-analysis:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Setup JDK 11
        uses: actions/setup-java@v4
        with:
          java-version: '11'
          distribution: 'temurin'
      
      - name: Cache Gradle dependencies
        uses: actions/cache@v3
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: gradle-${{ runner.os }}-${{ hashFiles('**/*.gradle*') }}
          
      - name: Run Kotlin linting
        run: ./gradlew ktlintCheck
        
      - name: Run Android Lint
        run: ./gradlew lint
        
      - name: Upload lint reports
        uses: actions/upload-artifact@v3
        if: always()
        with:
          name: lint-reports
          path: "**/build/reports/lint-results-*.html"

  # Job 2: Unit Tests
  unit-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Setup JDK 11
        uses: actions/setup-java@v4
        with:
          java-version: '11'
          distribution: 'temurin'
          
      - name: Cache Gradle dependencies
        uses: actions/cache@v3
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: gradle-${{ runner.os }}-${{ hashFiles('**/*.gradle*') }}
      
      - name: Run unit tests
        run: ./gradlew testDebugUnitTest
        
      - name: Generate test report
        run: ./gradlew jacocoTestReport
        
      - name: Upload test results
        uses: actions/upload-artifact@v3
        if: always()
        with:
          name: test-results
          path: "**/build/test-results/test*/*.xml"
          
      - name: Upload coverage reports
        uses: codecov/codecov-action@v3
        with:
          files: "**/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml"

  # Job 3: Build APKs
  build:
    runs-on: ubuntu-latest
    needs: [static-analysis, unit-tests]
    strategy:
      matrix:
        build-mode: [single, modular]
    steps:
      - uses: actions/checkout@v4
        with:
          lfs: true  # For large ML model files
          
      - name: Setup JDK 11
        uses: actions/setup-java@v4
        with:
          java-version: '11'
          distribution: 'temurin'
          
      - name: Cache Gradle dependencies
        uses: actions/cache@v3
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: gradle-${{ runner.os }}-${{ hashFiles('**/*.gradle*') }}
          
      - name: Setup Android SDK
        uses: android-actions/setup-android@v2
        
      - name: Cache ML Models
        uses: actions/cache@v3
        with:
          path: "**/src/main/assets/**/*.mdl"
          key: ml-models-${{ hashFiles('**/assets/**/*.mdl') }}
      
      - name: Build APK (${{ matrix.build-mode }})
        run: |
          if [ "${{ matrix.build-mode }}" = "single" ]; then
            ./gradlew -Psingle=true assembleDebug
          else
            ./gradlew -Psingle=false assembleDebug
          fi
          
      - name: Upload APK artifacts
        uses: actions/upload-artifact@v3
        with:
          name: apk-${{ matrix.build-mode }}
          path: |
            **/build/outputs/apk/debug/*.apk
          retention-days: 14

  # Job 4: Instrumented Tests
  instrumented-tests:
    runs-on: macos-latest  # Required for Android emulator with hardware acceleration
    needs: [static-analysis, unit-tests]
    strategy:
      matrix:
        api-level: [25, 30]
    steps:
      - uses: actions/checkout@v4
        with:
          lfs: true
          
      - name: Setup JDK 11
        uses: actions/setup-java@v4
        with:
          java-version: '11'
          distribution: 'temurin'
          
      - name: Cache Gradle dependencies
        uses: actions/cache@v3
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: gradle-${{ runner.os }}-${{ hashFiles('**/*.gradle*') }}
          
      - name: Cache AVD
        uses: actions/cache@v3
        id: avd-cache
        with:
          path: |
            ~/.android/avd/*
            ~/.android/adb*
          key: avd-${{ matrix.api-level }}
          
      - name: Create AVD and generate snapshot for caching
        if: steps.avd-cache.outputs.cache-hit != 'true'
        uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: ${{ matrix.api-level }}
          force-avd-creation: false
          emulator-options: -no-window -gpu swiftshader_indirect -noaudio -no-boot-anim -camera-back none
          disable-animations: false
          script: echo "Generated AVD snapshot for caching."
          
      - name: Run instrumented tests
        uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: ${{ matrix.api-level }}
          script: ./gradlew connectedDebugAndroidTest
          
      - name: Upload instrumented test results
        uses: actions/upload-artifact@v3
        if: always()
        with:
          name: instrumented-test-results-${{ matrix.api-level }}
          path: "**/build/reports/androidTests/connected/**"
```

#### 3. Security and Dependency Scanning (`.github/workflows/security.yml`)

```yaml
name: Security Scan
on:
  push:
    branches: [main, development]
  schedule:
    - cron: '0 0 * * 1'  # Weekly on Monday

jobs:
  dependency-scan:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Run Snyk to check for vulnerabilities
        uses: snyk/actions/gradle@master
        env:
          SNYK_TOKEN: ${{ secrets.SNYK_TOKEN }}
        with:
          args: --severity-threshold=high
          
  codeql-analysis:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Initialize CodeQL
        uses: github/codeql-action/init@v2
        with:
          languages: java, kotlin
      - name: Perform CodeQL Analysis
        uses: github/codeql-action/analyze@v2
```

## Continuous Deployment (CD)

### Release Pipeline (`.github/workflows/release.yml`)

```yaml
name: Release Pipeline
on:
  push:
    tags:
      - 'v*'

jobs:
  release:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with:
          lfs: true
          
      - name: Setup JDK 11
        uses: actions/setup-java@v4
        with:
          java-version: '11'
          distribution: 'temurin'
          
      - name: Cache Gradle dependencies
        uses: actions/cache@v3
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: gradle-${{ runner.os }}-${{ hashFiles('**/*.gradle*') }}
          
      - name: Decode signing key
        run: |
          echo ${{ secrets.SIGNING_KEY_BASE64 }} | base64 -d > release-key.jks
          
      - name: Build release APKs
        run: |
          ./gradlew -Psingle=true assembleRelease \
            -Pandroid.injected.signing.store.file=release-key.jks \
            -Pandroid.injected.signing.store.password=${{ secrets.SIGNING_STORE_PASSWORD }} \
            -Pandroid.injected.signing.key.alias=${{ secrets.SIGNING_KEY_ALIAS }} \
            -Pandroid.injected.signing.key.password=${{ secrets.SIGNING_KEY_PASSWORD }}
          
          ./gradlew -Psingle=false assembleRelease \
            -Pandroid.injected.signing.store.file=release-key.jks \
            -Pandroid.injected.signing.store.password=${{ secrets.SIGNING_STORE_PASSWORD }} \
            -Pandroid.injected.signing.key.alias=${{ secrets.SIGNING_KEY_ALIAS }} \
            -Pandroid.injected.signing.key.password=${{ secrets.SIGNING_KEY_PASSWORD }}
            
      - name: Generate release notes
        id: release_notes
        run: |
          echo "RELEASE_NOTES<<EOF" >> $GITHUB_OUTPUT
          git log $(git describe --tags --abbrev=0 HEAD^)..HEAD --pretty=format:"- %s" >> $GITHUB_OUTPUT
          echo "" >> $GITHUB_OUTPUT
          echo "EOF" >> $GITHUB_OUTPUT
          
      - name: Create GitHub Release
        uses: softprops/action-gh-release@v1
        with:
          files: |
            **/build/outputs/apk/release/*.apk
          body: ${{ steps.release_notes.outputs.RELEASE_NOTES }}
          draft: false
          prerelease: ${{ contains(github.ref, 'alpha') || contains(github.ref, 'beta') }}
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
          
      - name: Prepare F-Droid metadata
        run: |
          mkdir -p fdroid/metadata/com.example.sapphireassistantframework
          cat > fdroid/metadata/com.example.sapphireassistantframework.yml << EOF
          Categories:
            - System
          License: Apache-2.0
          AuthorName: Tadashi-Hikari
          AuthorEmail: 
          SourceCode: https://github.com/Tadashi-Hikari/Sapphire-Assistant-Framework
          IssueTracker: https://github.com/Tadashi-Hikari/Sapphire-Assistant-Framework/issues
          
          AutoName: Sapphire Assistant Framework
          Description: |-
            Open source Android assistant framework that works entirely offline.
            
          RepoType: git
          Repo: https://github.com/Tadashi-Hikari/Sapphire-Assistant-Framework.git
          
          Builds:
            - versionName: ${{ github.ref_name }}
              versionCode: ${{ github.run_number }}
              commit: ${{ github.sha }}
              subdir: .
              gradle:
                - yes
          EOF
          
      - name: Upload F-Droid metadata
        uses: actions/upload-artifact@v3
        with:
          name: fdroid-metadata
          path: fdroid/
```

### Beta Distribution (`.github/workflows/beta.yml`)

```yaml
name: Beta Distribution
on:
  push:
    branches: [development]
  workflow_dispatch:

jobs:
  beta-release:
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/development'
    steps:
      - uses: actions/checkout@v4
        with:
          lfs: true
          
      - name: Setup JDK 11
        uses: actions/setup-java@v4
        with:
          java-version: '11'
          distribution: 'temurin'
          
      - name: Build debug APK
        run: ./gradlew -Psingle=true assembleDebug
        
      - name: Create beta release
        uses: softprops/action-gh-release@v1
        with:
          tag_name: beta-${{ github.run_number }}
          name: Beta Build ${{ github.run_number }}
          files: "**/build/outputs/apk/debug/*.apk"
          body: |
            Beta build from development branch
            Commit: ${{ github.sha }}
            
            ⚠️ This is a pre-release build for testing purposes only
          prerelease: true
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

## Build Optimization Strategies

### 1. Gradle Build Optimization

**Root `build.gradle` enhancements:**
```gradle
allprojects {
    // Enable build cache
    buildCache {
        local {
            enabled = true
        }
    }
    
    // Optimize compilation
    tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile).configureEach {
        kotlinOptions {
            jvmTarget = "11"
            freeCompilerArgs += [
                "-Xuse-experimental=kotlin.ExperimentalStdlibApi",
                "-Xjvm-default=all"
            ]
        }
    }
}

// Build scan for performance insights
plugins {
    id 'com.gradle.build-scan' version '3.16.2'
}

buildScan {
    termsOfServiceUrl = 'https://gradle.com/terms-of-service'
    termsOfServiceAgree = 'yes'
    publishAlways()
}
```

**Enhanced `gradle.properties`:**
```properties
# Performance optimizations
org.gradle.parallel=true
org.gradle.configureondemand=true
org.gradle.caching=true
org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=1g -XX:+HeapDumpOnOutOfMemoryError

# Android optimizations
android.enableJetifier=true
android.useAndroidX=true
android.enableD8.desugaring=true
android.enableR8.fullMode=true
```

### 2. Asset Management Strategy

**Large file handling:**
- Use Git LFS for ML models and language files
- Implement asset downloading during first app launch
- Create separate APK variants for different language packs
- Use asset compression and lazy loading

**Asset build script (Gradle):**
```gradle
task downloadAssets(type: Download) {
    src 'https://example.com/models/vosk-model-android.zip'
    dest 'src/main/assets/sync/'
    onlyIfModified true
}

task verifyAssets(type: Verify) {
    dependsOn downloadAssets
    src 'src/main/assets/sync/vosk-model-android.zip'
    checksum 'abcd1234567890...'
}

preBuild.dependsOn verifyAssets
```

### 3. Multi-Variant Build Strategy

**Dynamic build configuration:**
```gradle
android {
    flavorDimensions "deployment", "features"
    
    productFlavors {
        // Deployment variants
        standalone {
            dimension "deployment"
            applicationIdSuffix ".standalone"
        }
        modular {
            dimension "deployment"
            applicationIdSuffix ".modular"
        }
        
        // Feature variants
        full {
            dimension "features"
            // All modules included
        }
        minimal {
            dimension "features"
            // Core modules only
        }
    }
    
    // Generate APKs for specific combinations
    variantFilter { variant ->
        def deployment = variant.getFlavors().get(0).name
        def features = variant.getFlavors().get(1).name
        
        // Skip certain combinations
        if (deployment == "modular" && features == "minimal") {
            variant.setIgnore(true)
        }
    }
}
```

## Distribution Strategy

### 1. Release Channels

**Stable Releases:**
- GitHub Releases (primary)
- F-Droid repository submission
- Direct APK downloads from project website

**Beta Releases:**
- GitHub Pre-releases
- Automated builds from `development` branch
- Community testing program

**Nightly Builds:**
- Automated builds from feature branches
- Internal testing only
- Retention: 7 days

### 2. APK Signing and Security

**Signing configuration:**
```gradle
android {
    signingConfigs {
        release {
            storeFile file('../release-key.jks')
            storePassword System.getenv('SIGNING_STORE_PASSWORD')
            keyAlias System.getenv('SIGNING_KEY_ALIAS')
            keyPassword System.getenv('SIGNING_KEY_PASSWORD')
        }
    }
    
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt')
        }
    }
}
```

**Security measures:**
- Signing keys stored in GitHub Secrets
- ProGuard/R8 code obfuscation enabled
- Security scanning with Snyk and CodeQL
- Dependency vulnerability monitoring

### 3. F-Droid Integration

**Metadata preparation:**
- Automated F-Droid metadata generation
- License compliance verification
- Anti-features declaration (if any)
- Build reproducibility configuration

**F-Droid build configuration:**
```yaml
# metadata/com.example.sapphireassistantframework.yml
Categories:
  - System
  - Multimedia

License: Apache-2.0
AuthorName: Tadashi-Hikari
SourceCode: https://github.com/Tadashi-Hikari/Sapphire-Assistant-Framework
IssueTracker: https://github.com/Tadashi-Hikari/Sapphire-Assistant-Framework/issues

AutoName: Sapphire Assistant Framework
Description: |-
    The Sapphire Assistant Framework is a plugin framework for open source
    assistant applications. It provides speech recognition, natural language
    processing, and modular skill development capabilities.

RepoType: git
Repo: https://github.com/Tadashi-Hikari/Sapphire-Assistant-Framework.git

Builds:
  - versionName: '0.2.0'
    versionCode: 1
    commit: v0.2.0
    subdir: .
    gradle:
      - single
    prebuild: |
      $$SDK$$/tools/bin/sdkmanager 'platforms;android-30' 'build-tools;30.0.2'
```

## Monitoring and Analytics

### 1. Build Metrics

**GitHub Actions monitoring:**
- Build success/failure rates
- Build duration trends
- Test coverage metrics
- APK size tracking

**Gradle Build Scans:**
- Performance bottleneck identification
- Dependency analysis
- Cache effectiveness metrics

### 2. Release Analytics

**Distribution tracking:**
- Download statistics per release
- Platform/device analytics
- Crash reporting integration (if applicable)
- User feedback collection

### 3. Quality Gates

**Mandatory checks before release:**
- All tests pass (unit + instrumented)
- Code coverage > 70%
- No high-severity security vulnerabilities
- APK size within acceptable limits
- Performance benchmarks met

## Implementation Roadmap

### Phase 1: Foundation (Week 1-2)
1. Set up basic GitHub Actions workflows
2. Configure build caching and optimization
3. Implement unit testing pipeline
4. Set up artifact storage

### Phase 2: Quality Assurance (Week 3-4)
1. Add instrumented testing with Android emulators
2. Implement static analysis and linting
3. Set up security scanning
4. Configure code coverage reporting

### Phase 3: Distribution (Week 5-6)
1. Implement signing and release pipeline
2. Set up GitHub Releases automation
3. Prepare F-Droid metadata and submission
4. Configure beta distribution channel

### Phase 4: Advanced Features (Week 7-8)
1. Implement multi-variant builds
2. Set up performance monitoring
3. Add advanced caching strategies
4. Implement automated dependency updates

## Required Secrets and Configuration

### GitHub Secrets
```
SIGNING_KEY_BASE64          # Base64 encoded signing key
SIGNING_STORE_PASSWORD      # Keystore password
SIGNING_KEY_ALIAS          # Key alias
SIGNING_KEY_PASSWORD       # Key password
SNYK_TOKEN                 # Snyk security scanning token
CODECOV_TOKEN              # Code coverage reporting token
```

### Repository Settings
- Enable GitHub Actions
- Configure branch protection rules
- Set up required status checks
- Configure auto-merge for dependency updates

## Conclusion

This CI/CD strategy provides a comprehensive framework for automating the build, test, and distribution processes of the Sapphire Assistant Framework. The approach addresses the unique challenges of multi-module Android development while ensuring code quality, security, and efficient distribution across multiple channels.

Key benefits:
- **Automated Quality Assurance**: Comprehensive testing and static analysis
- **Multi-Channel Distribution**: GitHub Releases, F-Droid, and beta channels
- **Performance Optimization**: Advanced caching and build optimization
- **Security Focus**: Automated vulnerability scanning and secure signing
- **Developer Experience**: Fast feedback loops and detailed reporting

Implementation should be phased to ensure stability and allow for iterative improvements based on real-world usage and feedback.