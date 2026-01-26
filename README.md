# Easy Karobar

[![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin-Multiplatform-blue.svg?style=for-the-badge)](https://kotlinlang.org/docs/multiplatform.html)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](https://opensource.org/licenses/MIT)

<div>
  <img src="https://img.shields.io/badge/Kotlin-a503fc?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Compose%20Multiplatform-4285F4?style=for-the-badge&logo=jetpack-compose&logoColor=white" alt="Compose Multiplatform" />
  <img src="https://img.shields.io/badge/Kotlin%20Coroutines-3DDC84?style=for-the-badge&logo=kotlin&logoColor=white" alt="Coroutines" />
  <img src="https://img.shields.io/badge/Ktor-0095D5?style=for-the-badge&logo=ktor&logoColor=white" alt="Ktor" />
  <img src="https://img.shields.io/badge/SQLDelight-f74230?style=for-the-badge&logo=sqlite&logoColor=white" alt="SQLDelight" />
</div>

**Easy Karobar** is a powerful and intuitive mobile application that brings the functionality of ERP softwares to your fingertips. Designed for business owners, accountants, and sales professionals, Easy Karobar allows you to manage your business's financial data on the go. This application is a Kotlin Multiplatform project, targeting both Android and iOS devices with a shared codebase for maximum efficiency and a consistent user experience.

---

## Key Features

### Financial Data Management
*   **Voucher Management:** Create, edit, and delete various voucher types including sales, purchases, receipts, and payments directly from your mobile device.
*   **Ledger Inquiry:** Access detailed ledger accounts and view transaction history with ease.
*   **Comprehensive Reports:** Generate essential financial reports like Day Book, Trial Balance, and more on the fly.

### Inventory Control
*   **Stock Management:** Keep a real-time track of your inventory levels, stock items, and batch details.
*   **Godown Management:** Manage stock across multiple locations or godowns seamlessly.

### Usability and Performance
*   **Real-time Synchronization:** Ensures your mobile data is always in sync.
*   **Offline First:** Continue working without an active internet connection. Your data will be automatically synced once you're back online.
*   **Multi-Company Support:** Effortlessly switch between and manage multiple companies from a single, unified interface.
*   **Intuitive User Interface:** A clean, modern interface designed for efficiency and ease of use across both Android and iOS.

---

## Tech Stack & Architecture

*   **Kotlin Multiplatform Mobile (KMM):** For sharing business logic between Android and iOS.
*   **Compose Multiplatform:** For building the user interface for both Android and iOS from a single codebase.
*   **Coroutines:** For asynchronous programming and responsive user interfaces.
*   **Ktor:** For robust and efficient networking.
*   **SQLDelight:** For type-safe local database storage.
*   **MVVM Architecture:** The project follows the Model-View-ViewModel architectural pattern for a clean and scalable codebase.

---

## Getting Started

### Prerequisites

*   Android Studio
*   Xcode
*   Kotlin Multiplatform Mobile plugin

### Clone the repository

```bash
git clone https://github.com/amanbutnot/EasyKarobar.git
```

### Build and Run

#### Android

To build and run the development version of the Android app, use the run configuration from the run widget in your IDE’s toolbar or build it directly from the terminal:

- **macOS/Linux:**
  ```shell
  ./gradlew :composeApp:assembleDebug
  ```
- **Windows:**
  ```shell
  .\gradlew.bat :composeApp:assembleDebug
  ```

#### iOS

To build and run the development version of the iOS app, use the run configuration from the run widget in your IDE’s toolbar or open the `/iosApp` directory in Xcode and run it from there.

---

## Project Structure

This is a Kotlin Multiplatform project targeting Android and iOS.

*   `./composeApp`: Shared code for Compose Multiplatform applications.
    *   `commonMain`: Code common to all targets.
    *   Platform-specific folders (e.g., `iosMain`, `androidMain`) for platform-specific implementations.
*   `./iosApp`: The native iOS application entry point.

---

## License

This project is licensed under the MIT License. See the `LICENSE` file for more details.
