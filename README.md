# RUTEdu

RUTEdu is an educational quiz application for mobile platforms. It helps users learn and practice topics in Mathematics, Chemistry, and Geography. The application is built using Kotlin Multiplatform and Compose Multiplatform, allowing it to run on both Android and iOS devices from a shared codebase.

## Key Features

* **Multiple Subjects**
  * **Mathematics**: Practice arithmetic operations (addition, subtraction, multiplication, and division), test divisibility rules, complete unit conversions, and solve algebra problems.
  * **Chemistry**: Practice with randomly generated questions. Activities include balancing chemical equations, exploring element cards, and testing your knowledge of acids, bases, hydrocarbons, and the periodic table.
  * **Geography**: Participate in interactive map-based quizzes using country coordinates and regional maps.
* **Gameplay Modes**
  * **Solo Mode**: Practice lessons and review hints at your own pace.
  * **PvP (Player versus Player) Mode**: Challenge another player to educational battles on the same device.
* **Progress Tracking**: Create player profiles to save your learning history, track progress, and customize the number of questions per lesson.

## Project Structure

The project is divided into the following main directories:
* `composeApp/`: Contains the shared user interface and core logic code written in Kotlin.
  * `commonMain/`: The core codebase shared across all platforms.
  * `androidMain/`: Android-specific setup and platform configurations.
  * `iosMain/`: iOS-specific platform configurations.
* `androidApp/`: The Android launcher application wrapper.
* `iosApp/`: The iOS launcher application wrapper and SwiftUI entry point.

## Getting Started

### Prerequisites

To build this project, you will need:
* Java Development Kit (JDK) 17 or higher
* Android SDK (to build the Android app)
* macOS with Xcode (to build the iOS app)

### Building the Android App

Open the project in Android Studio, or compile it directly from your terminal:
* On Linux or macOS:
  ```bash
  ./gradlew :androidApp:assembleDebug
  ```
* On Windows:
  ```cmd
  gradlew.bat :androidApp:assembleDebug
  ```

### Building the iOS App

To build and run the iOS application, open the `iosApp` directory in Xcode and start the build, or run it directly using the configuration in your IDE.

## Technology Stack

* **Kotlin Multiplatform**: Shared code between Android and iOS.
* **Compose Multiplatform**: Shared user interface toolkit for building the application screens.
* **SQLDelight**: Local database management to store user profiles and lesson progress.
* **Dokka**: Documentation generation tool.
