package org.prime.easykarobar.business.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import org.prime.easykarobar.business.repository.AuthRepository
import org.prime.easykarobar.data.expect.getDeviceIdString
import org.prime.easykarobar.data.expect.getDeviceName
import org.prime.easykarobar.data.expect.getFirebaseToken
import org.prime.easykarobar.data.expect.setupFcmSubscription
import org.prime.easykarobar.data.model.CompanyList
import org.prime.easykarobar.data.model.FirebaseTokenRequest
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
        println("[WWWW] AuthViewModel.userLogin called with username: ${loginRequest.Username}")
        viewModelScope.launch {

            _authState.value = AuthState(isLoading = true)

            try {

                val res = AuthRepository.userLogin(loginRequest)
                println("[WWWW] AuthViewModel.userLogin response statuscode: ${res?.statuscode}, message: ${res?.message}")

                when (res?.statuscode) {

                    200 -> {
                        println("[WWWW] AuthViewModel.userLogin status 200 received. Decoding LoginResponse...")
                        val loginData =
                            json.decodeFromJsonElement<LoginResponse>(res.data)

                        println("[WWWW] AuthViewModel.userLogin decoded LoginResponse: ID=${loginData.ID}, tokenIsBlank=${loginData.token.isBlank()}, C9=${loginData.C9}")

                        if (loginData.token.isNotBlank() && loginData.C9.isNotBlank()) {

                            SharedPrefs.LoginVersion.save(MOBILE_VERSION)

                            SharedPrefs.Token.clear()
                            SharedPrefs.FileId.clear()
                            SharedPrefs.DistributorData.clear()

                            SharedPrefs.Token.save(loginData.token)
                            SharedPrefs.FileId.save(loginData.C9)
                            SharedPrefs.ShowZeroStock.save(true)

                            // Save username ONCE
                            SharedPrefs.LoginInfo.save(loginRequest.Username.trim())
                            sendFirebaseToken(loginRequest.Username.trim())
                            println("[WWWW] AuthViewModel.userLogin token and FileId saved successfully.")

                        } else {
                            println("[WWWW] AuthViewModel.userLogin validation failed: token or C9 is blank!")
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
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                setupFcmSubscription(loginData.ID.toString())
                                println("[WWWW] FCM setup completed successfully for topic: ${loginData.ID}")
                            } catch (e: Exception) {
                                println("[WWWW] FCM setup error: ${e.message}")
                            }
                        }

                        _authState.value = AuthState(
                            success = true,
                            isLoading = false,
                            message = res.message,
                            data = loginData
                        )

                        println("[WWWW] AuthViewModel.userLogin triggering onSuccess()")
                        onSuccess()
                    }

                    900 -> {
                        println("[WWWW] AuthViewModel.userLogin status 900 (multiple companies) received.")
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
                        println("[WWWW] AuthViewModel.userLogin unexpected statuscode: ${res?.statuscode}")
                        _authState.value = AuthState(
                            success = false,
                            isLoading = false,
                            error = res?.message ?: "Login failed",
                            message = res?.message
                        )
                    }
                }

            } catch (e: Exception) {
                println("[WWWW] AuthViewModel.userLogin exception: ${e.message}")
                e.printStackTrace()
                _authState.value = AuthState(
                    success = false,
                    isLoading = false,
                    error = e.message ?: "Unexpected error"
                )
            }
        }
    }

    fun sendFirebaseToken(mobileNo: String? = null) {
        viewModelScope.launch {
            try {
                val fcmToken = getFirebaseToken() ?: ""
                val username = mobileNo?.trim()?.takeIf { it.isNotBlank() }
                    ?: SharedPrefs.LoginInfo.get()
                    ?: ""
                if (username.isNotBlank()) {
                    val req = FirebaseTokenRequest(
                        mobileNo = username,
                        deviceId = getDeviceIdString(),
                        deviceName = getDeviceName(),
                        jwtToken = fcmToken,
                        firebase_token = fcmToken
                    )
                    AuthRepository.sendFirebaseToken(req)
                }
            } catch (e: Exception) {
                println("Failed to send firebase token: ${e.message}")
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