# Nutrica Development Environment Setup

This guide provides step-by-step instructions for setting up your development environment to work on Nutrica.

---

## Table of Contents

1. [System Requirements](#system-requirements)
2. [Installing Android Studio](#installing-android-studio)
3. [Configuring the Android SDK](#configuring-the-android-sdk)
4. [Repository Setup](#repository-setup)
5. [API Configuration](#api-configuration)
6. [Building and Running](#building-and-running)
7. [Troubleshooting](#troubleshooting)

---

## System Requirements

### Hardware
- **Processor:** Intel Core i5 or equivalent
- **RAM:** Minimum 8GB (16GB recommended)
- **Storage:** 15GB free disk space
- **Display:** 1920x1080 resolution minimum

### Software
- **Operating System:** Windows 10+, macOS 10.15+, or Ubuntu 18.04+
- **Java Development Kit (JDK):** Version 17 (mandatory)
- **Git:** Latest stable version
- **Android Studio:** Koala (2024.1.1) or newer

---

## Installing Android Studio

### macOS

1. Download Android Studio from [developer.android.com](https://developer.android.com/studio)
2. Open the `.dmg` file
3. Drag Android Studio into Applications folder
4. Open Applications and launch Android Studio
5. Complete the setup wizard (Install SDK, configure emulator)

### Windows

1. Download Android Studio installer `.exe`
2. Run the installer
3. Follow the setup wizard
4. Select components: Android SDK, Android Virtual Device
5. Specify SDK location (default: `C:\Users\%USERNAME%\AppData\Local\Android\Sdk`)

### Linux (Ubuntu)

```bash
# Download the tar.gz file
wget https://developer.android.com/studio/archive

# Extract to opt directory
sudo tar -xvf android-studio-*-linux.tar.gz -C /opt/

# Create symbolic link
sudo ln -s /opt/android-studio/bin/studio.sh /usr/local/bin/android-studio

# Launch
android-studio
```

---

## Configuring the Android SDK

### Via Android Studio

1. Launch Android Studio
2. Click **Tools > SDK Manager**
3. Under **SDK Platforms**, select:
   - Android 14 (API 34) - Target
   - Android 13 (API 33)
   - Android 7 (API 24) - Minimum

4. Under **SDK Tools**, verify installation of:
   - Android SDK Build-Tools 34.0.0+
   - Android Emulator
   - Android SDK Platform-Tools
   - Android SDK Tools

5. Click **Apply** and agree to licenses

### Via Command Line

```bash
# Set ANDROID_HOME environment variable
export ANDROID_HOME=$HOME/Android/Sdk  # macOS/Linux
set ANDROID_HOME=%USERPROFILE%\AppData\Local\Android\Sdk  # Windows

# Accept all licenses
$ANDROID_HOME/tools/bin/sdkmanager --licenses

# Install specific SDK platform
$ANDROID_HOME/tools/bin/sdkmanager "platforms;android-34"
```

### Setting Up JDK 17

**macOS:**
```bash
brew install openjdk@17
sudo ln -sfn /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk
```

**Windows:**
1. Download from [adoptium.net](https://adoptium.net/)
2. Run installer
3. During installation, choose "Set JAVA_HOME variable"

**Linux:**
```bash
sudo apt-get update
sudo apt-get install openjdk-17-jdk
```

---

## Repository Setup

### Clone the Repository

```bash
# HTTPS (recommended for beginners)
git clone https://github.com/ChaudhariParthh/Nutrica.git
cd Nutrica

# SSH (requires GitHub SSH key setup)
git clone git@github.com:ChaudhariParthh/Nutrica.git
cd Nutrica
```

### Create Feature Branch

```bash
# Update main branch
git checkout main
git pull origin main

# Create feature branch
git checkout -b feature/your-feature-name
```

### Configure Git User (First Time)

```bash
git config --global user.name "Your Name"
git config --global user.email "your.email@example.com"
```

---

## API Configuration

### Step 1: Obtain API Keys

**Gemini AI API:**
1. Visit [Google AI Studio](https://aistudio.google.com/app/apikeys)
2. Click "Create API Key"
3. Select or create project
4. Copy the generated API key

**Barcode Database API (Optional):**
1. Register at [Open Food Facts](https://world.openfoodfacts.org/data/en.openfoodfacts.org.products.csv)
2. Or use [Edamam API](https://developer.edamam.com/)
3. Obtain your API credentials

### Step 2: Create .env File

```bash
# In repository root
cp .env.example .env
```

Edit `.env` with your API keys:
```properties
GEMINI_API_KEY=your_actual_gemini_key_here
BARCODE_API_KEY=your_barcode_api_key
BARCODE_API_SECRET=your_barcode_api_secret
API_BASE_URL=https://api.example.com
```

### Step 3: Secrets Gradle Plugin Integration

The project uses `gradle-secrets-plugin` to automatically inject `.env` values:

```kotlin
// build.gradle.kts
plugins {
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.1"
}

secrets {
    propertiesFileName = ".env"
}
```

Access in code:
```kotlin
val geminiKey = BuildConfig.GEMINI_API_KEY
```

### Step 4: Secure Your .env

```bash
# Add to .gitignore (already included)
echo ".env" >> .gitignore

# Verify .env is ignored
git status  # Should NOT show .env
```

**IMPORTANT:** Never commit `.env` files containing real API keys.

---

## Building and Running

### Open Project in Android Studio

1. Launch Android Studio
2. Click **File > Open**
3. Navigate to Nutrica repository root
4. Select folder and click **Open**
5. Wait for Gradle sync to complete

### Initial Gradle Sync

Android Studio will:
- Download Gradle wrapper
- Resolve dependencies from `build.gradle.kts`
- Index the project
- This may take 5-10 minutes on first sync

### Build Debug APK

**Via Android Studio:**
1. Click **Build > Build Bundle(s) / APK(s) > Build APK(s)**
2. Wait for build to complete
3. APK location: `app/build/outputs/apk/debug/app-debug.apk`

**Via Command Line:**
```bash
./gradlew assembleDebug
```

### Run on Emulator

**Create Virtual Device:**
1. Click **Device Manager** in Android Studio
2. Click **Create device**
3. Select device profile: Pixel 6 (recommended)
4. Select system image: Android 14 (API 34)
5. Configure settings and click **Create**

**Launch App:**
1. Start emulator from Device Manager
2. Open project in Android Studio
3. Click **Run** (green play button)
4. Select emulator from device list
5. App launches automatically

### Run on Physical Device

**Enable Developer Mode:**
1. Open Settings > About Phone
2. Tap Build Number 7 times
3. Return to Settings > System > Developer Options
4. Enable "USB Debugging"

**Connect Device:**
```bash
# Verify device is detected
adb devices

# Should output:
# List of attached devices
# DEVICE_ID    device
```

**Install and Run:**
1. Connect device via USB
2. Trust the computer when prompted on device
3. Click **Run** in Android Studio
4. Select your physical device
5. App installs and launches

---

## Troubleshooting

### Gradle Sync Fails

**Error:** "Unable to resolve com.google..."

**Solution:**
```bash
# Clear cache
./gradlew clean

# Invalidate Android Studio cache
# File > Invalidate Caches > Invalidate and Restart

# Check internet connection and proxy settings
```

### Android SDK Not Found

**Error:** "ANDROID_SDK_ROOT not set"

**Solution:**
```bash
# Set environment variable
export ANDROID_HOME=$HOME/Android/Sdk  # macOS/Linux

# Or in Android Studio:
# File > Project Structure > SDK Location
```

### .env File Not Loaded

**Error:** "BuildConfig.GEMINI_API_KEY is null"

**Solution:**
1. Verify `.env` file exists in project root
2. Check `.env` format (no quotes around values)
3. Rebuild project: `./gradlew clean assembleDebug`
4. Invalidate Android Studio cache

### Device Not Detected

**Error:** "No devices found"

**Solution (Linux/macOS):**
```bash
# Kill and restart ADB daemon
adb kill-server
adb start-server

# Check device permissions (Linux)
sudo usermod -a -G plugdev $USER

# Reconnect device
```

### Build Takes Too Long

**Solution:**
1. Enable Gradle daemon: `org.gradle.daemon=true` in `gradle.properties`
2. Use parallel builds: `org.gradle.parallel=true`
3. Reduce APK size: disable unused locales in `build.gradle.kts`

### Emulator Crashes on Startup

**Solution:**
1. Wipe data: `emulator -avd DEVICE_NAME -wipe-data`
2. Use x86 system image instead of ARM
3. Increase allocated RAM in AVD settings
4. Update graphics drivers

---

## Next Steps

1. Review [ARCHITECTURE.md](ARCHITECTURE.md) to understand project structure
2. Read [CONTRIBUTING.md](../CONTRIBUTING.md) for development guidelines
3. Check [API.md](API.md) for API integration details
4. Start exploring the codebase!

---

## Getting Help

- **GitHub Issues:** [Report bugs](https://github.com/ChaudhariParthh/Nutrica/issues)
- **Documentation:** Check [README.md](../README.md)
- **Discussions:** Join [GitHub Discussions](https://github.com/ChaudhariParthh/Nutrica/discussions)