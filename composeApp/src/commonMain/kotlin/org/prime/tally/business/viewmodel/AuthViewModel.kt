package org.prime.tally.business.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.prime.tally.data.model.LoginRequest
import org.prime.tally.data.model.LoginResponse
import org.prime.tally.business.repository.AuthRepository
import org.prime.tally.data.expect.deleteDbFile
import org.prime.tally.data.model.ForgotResponse
import org.prime.tally.data.utils.SharedPrefs


class AuthViewModel : ViewModel() {
    private val _authState = mutableStateOf(AuthState<LoginResponse>())
    val authState: State<AuthState<LoginResponse>> = _authState

    private val _validateState = mutableStateOf(DataState<ForgotResponse>())
    val validateState: State<DataState<ForgotResponse>> = _validateState
    private val _resetState = mutableStateOf(DataState<Unit>())
    val resetState: State<DataState<Unit>> = _resetState

    fun validateMobile(username: String, onSuccess: () -> Unit) {
        viewModelScope.launch {

            _validateState.value = DataState(isLoading = true)

            val res = AuthRepository.validateMobile(username)

            if (res?.statuscode == 200) {
                _validateState.value = DataState(
                    success = true, message = res.message, isLoading = false
                )
                onSuccess()
            } else {
                _validateState.value = DataState(
                    success = false,
                    isLoading = false,
                    error = res?.message ?: "Error Occurred. Please try again."
                )
            }
        }
    }

    fun resetPassword(username: String, id: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {

            _resetState.value = DataState(isLoading = true)

            val res = AuthRepository.resetPassword(id, username)

            if (res?.statuscode == 200) {
                _resetState.value = DataState(
                    success = true, message = res.message, isLoading = false, data = res.data
                )
            } else {
                _resetState.value = DataState(
                    success = false,
                    isLoading = false,
                    error = res?.message ?: "Error Occurred. Please try again."
                )
            }
        }
    }



    fun userLogin(loginRequest: LoginRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {

            _authState.value = AuthState(isLoading = true)

            val res = AuthRepository.userLogin(loginRequest)

            if (res?.statuscode == 200) {
                _authState.value = AuthState(
                    success = true, message = res.message
                )
                val token = res.data?.token
                val fileId = res.data?.C9
                if (!token.isNullOrBlank() && !fileId.isNullOrBlank()) {
                    SharedPrefs.Token.clear()
                    SharedPrefs.FileId.clear()
                    SharedPrefs.DistributorData.clear()
                    deleteDbFile()
                    SharedPrefs.Token.save(token)
                    SharedPrefs.FileId.save(fileId)
                } else {
                    println("⚠️ Login success, but token is null or blank")
                }
                val distributor = res.data?.distributor
                if (distributor != null) {
                    println("data saving: $distributor")
                    SharedPrefs.DistributorData.save(distributor)
                }
                onSuccess()
            } else {
                _authState.value = AuthState(
                    success = false,
                    isLoading = false,
                    error = res?.message ?: "Login failed. Please try again."
                )
            }
        }
    }

    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }


    data class AuthState<T>(
        val success: Boolean = false,
        val isLoading: Boolean = false,
        val data: T? = null,
        val error: String? = null,
        val message: String? = null
    )

    data class DataState<T>(
        val success: Boolean = false,
        val isLoading: Boolean = false,
        val data: T? = null,
        val error: String? = null,
        val message: String? = null
    )
}