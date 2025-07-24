# GitFolio

GitFolio is an Android application for searching GitHub users and viewing their profiles and repositories. It's built with modern Android development practices, following a clean architecture.

## Installation

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/sam43/GitFolio.git
    ```
2.  **Open in Android Studio:**
    *   Open Android Studio.
    *   Select `File > Open...` or `Open an Existing Project`.
    *   Navigate to the cloned directory and open it.
3.  **Build & Run:**
    *   Wait for Android Studio to sync the Gradle project.
    *   Select a target device (emulator or physical).
    *   Click the **Run** button.
4. **To run tests:**
    *   In Android Studio, select `View > Tool Windows > Terminal`.
    *   Run unit tests using:
        ```bash
        ./gradlew testDebugUnitTest
        ```
**N:B: Add ```GITHUB_API_TOKEN``` to local.properties file to request more than 60 requests per hour from GitHub API.**
## Project Structure

```
app/
├── build.gradle.kts
└── src/
    ├── main/
    │   ├── AndroidManifest.xml
    │   ├── java/
    │   │   └── io/sam43/gitfolio/
    │   │       ├── data/                # Data Layer (Repositories, Remote/Local Data Sources)
    │   │       │   ├── helper/          # Network monitoring and Result wrappers
    │   │       │   ├── remote/          # Retrofit services, DTOs, Mappers
    │   │       │   └── repository/      # Implementation of domain repositories
    │   │       │
    │   │       ├── di/                  # Hilt dependency injection modules
    │   │       │
    │   │       ├── domain/              # Domain Layer (Business logic, Use Cases, Models)
    │   │       │   ├── model/           # Core domain models
    │   │       │   ├── usecases/        # Application-specific business rules
    │   │       │   └── repository/      # Repository interfaces
    │   │       │
    │   │       ├── presentation/        # Presentation Layer (UI)
    │   │       │   ├── theme/           # Jetpack Compose theme
    │   │       │   ├── ui/              # Composable screens and components
    │   │       │   └── viewmodels/      # ViewModels for UI state management
    │   │       │
    │   │       └── util/                # Utility classes
    │   │
    │   └── res/                     # Android resources
    │
    ├── test/                        # Unit tests
    │   └── java/io/sam43/gitfolio/
    │
    └── androidTest/                 # Instrumented tests
        └── java/io/sam43/gitfolio/
```
