# Project Structure

```

app/
├── src/
│   ├── main/
│   │   ├── kotlin+java/
│   │   │   └── io/sam43/gitfolio/  # Your app's root package
│   │   │       ├── App.kt      # Application class (for Koin init)
│   │   │       ├── MainActivity.kt                 # Main Activity (hosts navigation)
│   │   │       │
│   │   │       ├── data/                           # Data Layer (Implements domain interfaces)
│   │   │       │   ├── remote/
│   │   │       │   │   ├── ApiService.kt     # Retrofit2 service interface/implementation
│   │   │       │   │   ├── dto/                    # Data Transfer Objects
│   │   │       │   │   │   ├── UserDto.kt
│   │   │       │   │   │   └── UserDetailDto.kt
│   │   │       │   │   └── mapper/                 # DTO to Domain model mappers
│   │   │       │   │       ├── UserDtoMapper.kt
│   │   │       │   │       └── UserDetailDtoMapper.kt
│   │   │       │   │
│   │   │       │   ├── local/
│   │   │       │   │   ├── database/
│   │   │       │   │   │   ├── AppDatabase.kt      # Room Database class
│   │   │       │   │   │   └── dao/
│   │   │       │   │   │       └── UserDao.kt      # Room DAO
│   │   │       │   │   └── entity/                 # Room Entities
│   │   │       │   │       └── UserEntity.kt
│   │   │       │   │   └── mapper/                 # Entity to Domain model mappers
│   │   │       │   │       └── UserEntityMapper.kt
│   │   │       │   │
│   │   │       │   └── repository/
│   │   │       │       └── UserRepositoryImpl.kt   # Implementation of the domain's UserRepository
│   │   │       │
│   │   │       ├── di/                             # Dependency Injection (Koin Modules)
│   │   │       │   ├── AppModule.kt               # Main Koin module
│   │   │       │   ├── DataModule.kt              # Koin module for Data layer
│   │   │       │   ├── DomainModule.kt            # Koin module for Domain layer (use cases)
│   │   │       │   └── PresentationModule.kt      # Koin module for Presentation layer (view models)
│   │   │       │
│   │   │       ├── domain/                         # Domain Layer (Core business logic, pure Kotlin)
│   │   │       │   ├── model/
│   │   │       │   │   ├── User.kt                 # Domain User model
│   │   │       │   │   └── UserDetail.kt           # Domain UserDetail model
│   │   │       │   │
│   │   │       │   ├── repository/
│   │   │       │   │   └── UserRepository.kt       # Interface (contract) for User data
│   │   │       │   │
│   │   │       │   └── usecase/
│   │   │       │       ├── GetUserDetailUseCase.kt # Use Case to get user details
│   │   │       │       └── GetUsersUseCase.kt      # Use Case to get a list of users
│   │   │       │
│   │   │       ├── presentation/                   # Presentation Layer (UI logic, ViewModels, Composables)
│   │   │       │   ├── common/                     # Reusable UI components
│   │   │       │   │   ├── AppDestinations.kt      # Navigation routes
│   │   │       │   │   ├── AppNavigation.kt        # NavHost setup
│   │   │       │   │   │── theme                   # Material Theme, Colors, Type
│   │   │       │   │   │   └── Color.kt
│   │   │       │   │   │   └── Theme.kt
│   │   │       │   │   │   └── Type.kt
│   │   │       │   │   └── ComposableUtils.kt      # Generic UI utility functions
│   │   │       │   │
│   │   │       │   └── users/                      # Feature-specific UI (GitHub Users)
│   │   │       │       ├── contract/
│   │   │       │       │   └── UserContract.kt     # Defines MVI Intent, State, SideEffect
│   │   │       │       ├── UserDetailScreen.kt     # Composable for user detail
│   │   │       │       ├── UserListScreen.kt       # Composable for user list
│   │   │       │       └── UserViewModel.kt        # ViewModel for user features
│   │   │       │
│   │   │       └── util/                           # General utility functions/extensions
│   │   │           └── Result.kt                   # Sealed class for generic result handling
│   │   │
│   │   ├── res/                                    # Android resources
│   │   │   ├── drawable/
│   │   │   ├── values/
│   │   │   │   ├── colors.xml
│   │   │   │   ├── strings.xml
│   │   │   │   └── themes.xml
│   │   │   └── ...
│   │   │
│   │   └── AndroidManifest.xml
│   │
│   ├── test/                                       # Unit Tests
│   │   └── com/yourcompany/githubviewer/
│   │       ├── domain/
│   │       │   └── usecase/
│   │       │       └── GetUsersUseCaseTest.kt
│   │       ├── data/
│   │       │   └── repository/
│   │       │       └── UserRepositoryImplTest.kt
│   │       └── presentation/
│   │           └── users/
│   │               └── UserViewModelTest.kt
│   │
│   └── androidTest/                                # Instrumented Tests
│       └── com/yourcompany/githubviewer/
│           ├── presentation/
│           │   └── users/
│           │       └── UserListScreenTest.kt      # Compose UI Tests
│           └── ExampleInstrumentedTest.kt
│
└── build.gradle.kts                                # app module's build file


```
