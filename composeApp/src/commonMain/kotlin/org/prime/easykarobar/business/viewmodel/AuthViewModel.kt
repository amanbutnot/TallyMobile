package org.prime.easykarobar.business.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import org.prime.easykarobar.business.repository.AuthRepository
import org.prime.easykarobar.data.model.CompanyList
import org.prime.easykarobar.data.model.ForgotResponse
import org.prime.easykarobar.data.model.LoginRequest
import org.prime.easykarobar.data.model.LoginResponse
import org.prime.easykarobar.data.utils.MOBILE_VERSION
import org.prime.easykarobar.data.utils.SharedPrefs

class AuthViewModel : ViewModel() {

    private val _authState = mutableStateOf(AuthState<LoginResponse>())
    val authState: State<AuthState<LoginResponse>> = _authState

    private val _validateState = mutableStateOf(DataState<ForgotResponse>())
    val validateState: State<DataState<ForgotResponse>> = _validateState

    private val _resetState = mutableStateOf(DataState<Unit>())
    val resetState: State<DataState<Unit>> = _resetState

    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun clearValidateMessage() {
        _validateState.value = _validateState.value.copy(error = null, message = null)
    }

    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }

    fun validateMobile(username: String, onSuccess: () -> Unit) {

        viewModelScope.launch {

            _validateState.value = DataState(isLoading = true)

            val res = AuthRepository.validateMobile(username)

            if (res?.statuscode == 200) {

                _validateState.value = DataState(
                    success = true,
                    isLoading = false,
                    message = res.message
                )

                onSuccess()

            } else {

                _validateState.value = DataState(
                    success = false,
                    isLoading = false,
                    error = res?.message ?: "Error occurred. Please try again."
                )
            }
        }
    }

    fun sendOtp(number: String, message: String, onSuccess: () -> Unit) {

        viewModelScope.launch {

            _validateState.value = DataState(isLoading = true)

            val res = AuthRepository.sendOtp(number, message)

            if (res?.success == true) {

                _validateState.value = DataState(
                    success = true,
                    isLoading = false,
                    message = res.message
                )

                onSuccess()

            } else {

                _validateState.value = DataState(
                    success = false,
                    isLoading = false,
                    error = res?.message ?: "Error occurred. Please try again."
                )
            }
        }
    }

    fun resetPassword(password: String, id: String, onSuccess: () -> Unit) {

        viewModelScope.launch {

            _resetState.value = DataState(isLoading = true)

            val res = AuthRepository.resetPassword(id, password)

            if (res?.statuscode == 200) {

                _resetState.value = DataState(
                    success = true,
                    isLoading = false,
                    message = res.message
                )

                onSuccess()

            } else {

                _resetState.value = DataState(
                    success = false,
                    isLoading = false,
                    error = res?.message ?: "Error occurred. Please try again."
                )
            }
        }
    }

    fun userLogin(
        loginRequest: LoginRequest,
        onSuccess: () -> Unit,
        onListSuccess: (CompanyList) -> Unit
    ) {

        viewModelScope.launch {

            _authState.value = AuthState(isLoading = true)

            try {

                val res = AuthRepository.userLogin(loginRequest)

                when (res?.statuscode) {

                    200 -> {

                        val loginData =
                            json.decodeFromJsonElement<LoginResponse>(res.data)

                        if (loginData.token.isNotBlank() && loginData.C9.isNotBlank()) {

                            SharedPrefs.LoginVersion.save(MOBILE_VERSION)

                            SharedPrefs.Token.clear()
                            SharedPrefs.FileId.clear()
                            SharedPrefs.DistributorData.clear()

                            SharedPrefs.Token.save(loginData.token)
                            SharedPrefs.FileId.save(loginData.C9)

                            // Save username ONCE
                            SharedPrefs.LoginInfo.save(loginRequest.Username.trim())

                        } else {

                            _authState.value = AuthState(
                                success = false,
                                isLoading = false,
                                error = "Data not found",
                                message = res.message
                            )

                            return@launch
                        }

                        loginData.distributor?.let {
                            SharedPrefs.DistributorData.save(it)
                        }

                        loginData.permissions?.let {
                            SharedPrefs.Permissions.save(it)
                        }

                        SharedPrefs.User.save(loginData)

                        _authState.value = AuthState(
                            success = true,
                            isLoading = false,
                            message = res.message,
                            data = loginData
                        )

                        onSuccess()
                    }

                    900 -> {

                        val companies =
                            json.decodeFromJsonElement<CompanyList>(res.data)

                        _authState.value = AuthState(
                            success = true,
                            isLoading = false,
                            message = res.message
                        )

                        onListSuccess(companies)
                    }

                    else -> {

                        _authState.value = AuthState(
                            success = false,
                            isLoading = false,
                            error = res?.message ?: "Unexpected error"
                        )
                    }
                }

            } catch (e: Exception) {

                _authState.value = AuthState(
                    success = false,
                    isLoading = false,
                    error = e.message ?: "Unexpected error"
                )
            }
        }
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