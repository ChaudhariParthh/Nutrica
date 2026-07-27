# Contributing to Nutrica

We are excited that you want to contribute to **Nutrica**! Below is a guide on how to get your environment configured, follow our design standards, and submit a high-quality contribution.

---

## 🚀 Development Setup

Nutrica is a modern Android application built using **Kotlin**, **Jetpack Compose**, and **Material Design 3**.

### Prerequisites

- **Android Studio** (Koala or newer recommended)
- **JDK 17** (mandatory for Gradle and modern AGP support)
- **Android SDK** (API Level 24 up to API Level 36)

### Local Build and Sync

To set up the project locally:

1. **Clone the Repository:**
   ```bash
   git clone https://github.com/your-username/Nutrica.git
   cd Nutrica
   ```

2. **Configure API Secrets:**
   Nutrica uses the **Secrets Gradle Plugin** to load API keys securely without committing them to source control.
   - Copy the environment template file:
     ```bash
     cp .env.example .env
     ```
   - Open `.env` in your editor and add your secure API keys (such as `GEMINI_API_KEY`).

3. **Open in Android Studio:**
   - Select **File > Open** and choose the root directory of Nutrica.
   - Wait for Gradle sync to complete successfully.

---

## 🛠️ Build and Verify

Before committing or pushing changes, verify that the project compiles cleanly and passes all local unit tests.

### Compile the Application

```bash
gradle assembleDebug
```

### Run Unit & Robolectric Tests

Nutrica has local JVM tests using **Robolectric** to simulate Android runtime environments and **Roborazzi** for screenshot verification.

To run the full suite:
```bash
gradle :app:testDebugUnitTest
```

### Roborazzi Screenshot Verification

If you modified UI elements, verify there are no visual regressions:
```bash
gradle :app:verifyRoborazziDebug
```

If you intentionally updated the layout and need to update the baseline screenshots:
```bash
gradle :app:recordRoborazziDebug
```

---

## 🎨 Architectural & Design Rules

To maintain high code quality, we follow several strict guidelines:

1. **Architecture:** Use **MVVM (Model-View-ViewModel)** with Jetpack architecture components. Place business logic in `NutricaViewModel` and UI components in composable screens.
2. **State Management:** Use `MutableStateFlow` and gather flows safely using `collectAsStateWithLifecycle()` to save system resources.
3. **Jetpack Compose & Material 3:**
   - Do not hardcode hex values. Always use values from the theme color scheme (`MaterialTheme.colorScheme`).
   - Maintain excellent touch padding. All touch targets must have a minimum footprint of `48.dp` x `48.dp`.
   - Provide meaningful `contentDescription` for all non-decorative icons.
4. **Local Data Persistence:** Any local entity storage must utilize the integrated **Room Database** (`AppDatabase.kt`).

---

## 📬 Pull Request Process

1. **Create a Feature Branch:** Use descriptive names like `feature/add-recipe-sharing` or `bugfix/fix-login-spacing`.
2. **Commit Message Standards:** Write clear, concise commit messages (e.g., `feat: Add barcode preset picker for fast nutrition lookup`).
3. **Self-Review:** Open and fill out the `PULL_REQUEST_TEMPLATE.md` to self-verify code styling, test results, and secret protection.
4. **CI Build Success:** Ensure that the automatic GitHub Action "Android CI" passes successfully on your branch.
