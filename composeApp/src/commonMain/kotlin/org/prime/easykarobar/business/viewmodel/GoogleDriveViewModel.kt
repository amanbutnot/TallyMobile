package org.prime.easykarobar.business.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.prime.easykarobar.data.model.DriveTokenResponse
import org.prime.easykarobar.business.repository.GoogleDriveRepository
import org.prime.easykarobar.business.repository.downloadAndExtractGoogleDriveFile
import org.prime.easykarobar.business.repository.downloadAndExtractZip

class GoogleDriveViewModel : ViewModel() {

    private val _driveState = mutableStateOf(ProfileState<DriveTokenResponse>())
    val driveState: State<ProfileState<DriveTokenResponse>> = _driveState
    private val _downloadState = mutableStateOf(ProfileState<ByteArray>())
    val downloadState: State<ProfileState<ByteArray>> = _downloadState

    fun getDriveToken(onSuccess: (token: String) -> Unit) {
        viewModelScope.launch {

            _driveState.value = ProfileState(isLoading = true)

            val res = GoogleDriveRepository.getDriveToken()

            val token = res?.data?.access_token
            if (res?.statuscode == 200 && token != null) {
                _driveState.value = ProfileState(
                    success = true,
                    isLoading = false,
                    data = res.data,
                    message = res.message
                )
                onSuccess(token)
            } else {
                _driveState.value = ProfileState(
                    success = false,
                    isLoading = false,
                    error = res?.message ?: "Failed to fetch drive token. Please try again."
                )
            }
        }
    }

    data class ProfileState<T>(
        val success: Boolean = false,
        val isLoading: Boolean = false,
        val data: T? = null,
        val error: String? = null,
        val message: String? = null
    )
}

data class DownloadProfileState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val data: Any? = null,
    val message: String? = null,
    val error: String? = null,
)

class GDownloadViewModel : ViewModel() {

    private val _driveState = MutableStateFlow(DownloadProfileState())
    val driveState: StateFlow<DownloadProfileState> = _driveState.asStateFlow()

    private val _downloadState = MutableStateFlow(DownloadProfileState())
    val downloadState: StateFlow<DownloadProfileState> = _downloadState.asStateFlow()

    fun downloadAndExtractDatabase(
        fileId: String,
        accessToken: String,
        destinationPath: String,
        onSuccess: (String) -> Unit  // Returns extracted database path
    ) {
        viewModelScope.launch {
            try {
                _driveState.value = DownloadProfileState(isLoading = true)

                val result = downloadAndExtractGoogleDriveFile(
                    fileId = fileId,
                    accessToken = accessToken,
                    destinationPath = destinationPath,
                )

                result.onSuccess { dbPath ->
                    _downloadState.value = DownloadProfileState(
                        success = true,
                        data = dbPath,
                        message = "Database downloaded and extracted successfully"
                    )
                    // Keep the last progress visible
                    _driveState.value = _driveState.value.copy(isLoading = false)

                    println("DATABASE READY at: $dbPath")
                    onSuccess(dbPath)
                }

                result.onFailure { error ->
                    _downloadState.value = DownloadProfileState(
                        success = false,
                        error = error.message ?: "Failed to download/extract database"
                    )
                    _driveState.value = DownloadProfileState(isLoading = false)
                }

            } catch (e: Exception) {
                _downloadState.value = DownloadProfileState(
                    success = false,
                    error = e.message ?: "Failed to process database"
                )
                _driveState.value = DownloadProfileState(isLoading = false)
            }
        }
    }

    fun downloadDatabaseFromUrl(
        url: String,
        destinationPath: String,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _driveState.value = DownloadProfileState(isLoading = true)

                val result = downloadAndExtractZip(
                    url = url,
                    destinationPath = destinationPath,
                )

                result.onSuccess { dbPath ->
                    _downloadState.value = DownloadProfileState(
                        success = true,
                        data = dbPath,
                        message = "Database downloaded and extracted successfully"
                    )
                    _driveState.value = _driveState.value.copy(isLoading = false)
                    onSuccess(dbPath)
                }

                result.onFailure { error ->
                    _downloadState.value = DownloadProfileState(
                        success = false,
                        error = error.message ?: "Failed to download/extract database"
                    )
                    _driveState.value = DownloadProfileState(isLoading = false)
                }
            } catch (e: Exception) {
                _downloadState.value = DownloadProfileState(
                    success = false,
                    error = e.message ?: "Failed to process database"
                )
                _driveState.value = DownloadProfileState(isLoading = false)
            }
        }
    }
}
