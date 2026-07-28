# Walkthrough - Store Database Direct Download

I have implemented the ability to download the store database directly from `easykarobar.in` when a `STORE_ID` is present, bypassing the Google Drive download flow.

## Changes Made

### Business Logic
- **[GoogleDriveRepository.kt](file:///Users/aman/Projects/Freelance/Easy%20Karobar/composeApp/src/commonMain/kotlin/org/prime/easykarobar/business/repository/GoogleDriveRepository.kt)**: Added `downloadAndExtractZip` expect function.
- **[GoogleDriveRepository.android.kt](file:///Users/aman/Projects/Freelance/Easy%20Karobar/composeApp/src/androidMain/kotlin/org/prime/easykarobar/business/repository/GoogleDriveRepository.android.kt)**: Implemented the logic to download and unzip from any URL.
- **[GoogleDriveRepository.ios.kt](file:///Users/aman/Projects/Freelance/Easy%20Karobar/composeApp/src/iosMain/kotlin/org/prime/easykarobar/business/repository/GoogleDriveRepository.ios.kt)**: Implemented the corresponding logic for iOS using `okio` and `zlib`.
- **[GoogleDriveViewModel.kt](file:///Users/aman/Projects/Freelance/Easy%20Karobar/composeApp/src/commonMain/kotlin/org/prime/easykarobar/business/viewmodel/GoogleDriveViewModel.kt)**: Added `downloadDatabaseFromUrl` to handle direct downloads.

### UI Implementation
- **[GoogleDriveDownloadScreen.kt](file:///Users/aman/Projects/Freelance/Easy%20Karobar/composeApp/src/commonMain/kotlin/org/prime/easykarobar/ui/screen/startup/GoogleDriveDownloadScreen.kt)**:
    - Updated `LaunchedEffect(Unit)` to check for `BuildKonfig.STORE_ID`.
    - If `STORE_ID` is present, it now downloads from `http://easykarobar.in/database/${storeId}.zip`.
    - It skips the Google Drive token acquisition step for these stores.

## Verification Results

- **Code Quality**: Verified with `analyze_file`, no compilation errors found in common, Android, or iOS source sets.
- **Logic**: The URL construction correctly follows the pattern `http://easykarobar.in/database/10335.zip` as requested.
- **Backward Compatibility**: The existing Google Drive flow remains intact for apps without a `STORE_ID`.

> [!TIP]
> This change ensures that branded "Easy Mart" apps will have a faster setup process as they no longer need to communicate with Google Drive APIs.
