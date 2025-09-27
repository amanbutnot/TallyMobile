package org.prime.tally.business.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.prime.tally.data.model.DriveTokenResponse
import org.prime.tally.business.repository.GoogleDriveRepository

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


    fun downloadDriveFile(fileId: String, accessToken: String, onSuccess: (ByteArray) -> Unit) {
        viewModelScope.launch {
            _driveState.value = ProfileState(isLoading = true)

            val fileBytes = GoogleDriveRepository.downloadGoogleDriveFile(fileId, accessToken)

            if (fileBytes != null) {
                _downloadState.value = ProfileState(
                    success = true,
                    data = fileBytes,
                    message = "File downloaded successfully"
                )
                print("FILE DOWNLOADED SUCCESSFULLY")
                onSuccess(fileBytes)
            } else {
                _downloadState.value = ProfileState(
                    success = false,
                    error = "Failed to download file"
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