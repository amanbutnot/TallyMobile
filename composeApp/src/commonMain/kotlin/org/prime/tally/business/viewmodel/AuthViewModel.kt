package org.prime.tally.business.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.prime.tally.data.model.LoginRequest
import org.prime.tally.data.model.LoginResponse
import org.prime.tally.business.repository.AuthRepository
import org.prime.tally.data.utils.SharedPrefs


class AuthViewModel : ViewModel() {
    private val _authState = mutableStateOf(AuthState<LoginResponse>())
    val authState: State<AuthState<LoginResponse>> = _authState

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
                    SharedPrefs.Token.save(token)
                    SharedPrefs.FileId.save(fileId)
                } else {
                    println("⚠️ Login success, but token is null or blank")
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
}