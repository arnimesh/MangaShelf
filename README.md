# MangaShelf

MangaShelf is a modern Android application that helps users discover, track, and organize their manga reading experience.
## Features

- Browse manga with a clean, modern UI
- Filter manga by year using tab navigation
- Sort manga by:
  - Year (Ascending/Descending)
  - Score (Ascending/Descending)
  - Popularity (Ascending/Descending)
- Mark manga as favorite
- Track reading status
- Offline support with local caching

## Tech Stack

- **Architecture Pattern**: MVVM 
- **Programming Language**: Kotlin
- **Dependency Injection**: Hilt
- **Database**: Room
- **Networking**: Retrofit
- **Navigation**: Navigation Component
- **Asynchronous Programming**: Coroutines + Flow

## Setup Instructions

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer(preferred)
- JDK 17
- Android SDK 24 or higher

### Steps
1. Clone the repository:
   ```bash
   git clone https://github.com/arnimesh/MangaShelf.git
   ```

2. Open Android Studio and select "Open an existing project"

3. Navigate to the cloned directory and click "Open"

4. Wait for Gradle sync to complete

5. Run the app:
   - Select a device/emulator (minimum SDK 24)
   - Click the "Run" button or press Shift + F10

## Architecture Overview

The application follows the MVVM (Model-View-ViewModel) architecture pattern and is organized into several layers:

### Presentation Layer
- **Activities/Fragments**: Handle UI rendering and user interactions
- **ViewModels**: Manage UI-related data, handle business logic, and maintain UI state
- **Adapters**: Handle RecyclerView data binding

### Data Layer
- **Repository**: Single source of truth for data, manages data flow between local and remote sources
- **Local Database**: Room database for offline caching
- **Remote Source**: Retrofit service for API calls

### Key Libraries
- **Hilt**: For dependency injection, making the code more modular and testable
- **Room**: Provides abstraction layer over SQLite for robust database access
- **Retrofit**: Type-safe HTTP client for API calls
- **Coroutines & Flow**: For asynchronous programming and reactive streams
- **Navigation Component**: Handles in-app navigation

