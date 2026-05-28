package org.prime.easykarobar.business.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.prime.easykarobar.business.repository.FollowupRepo
import org.prime.easykarobar.data.model.FollowupData
import org.prime.easykarobar.data.model.PostFollowup
import org.prime.easykarobar.data.model.PostFollowupResponse

class FollowupViewmodel : ViewModel() {
    private val _dataState = mutableStateOf(DataState<PostFollowupResponse>())
    val dataState: State<DataState<PostFollowupResponse>> = _dataState

    private val _listState = mutableStateOf(DataState<List<FollowupData>>())
    val listState: State<DataState<List<FollowupData>>> = _listState

    fun postFollowup(followup: PostFollowup, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _dataState.value = DataState(isLoading = true)
            val res = FollowupRepo.createAccount(followup)
            if (res?.statuscode == 200) {
                _dataState.value = DataState(
                    success = true,
                    isLoading = false,
                    data = res.data,
                    message = res.message
                )
                onSuccess()
            } else {
                _dataState.value = DataState(
                    success = false,
                    isLoading = false,
                    error = res?.message ?: "Error Occurred: Please try again."
                )
            }
        }
    }

    fun getFollowupList() {
        viewModelScope.launch {
            _listState.value = DataState(isLoading = true)
            val res = FollowupRepo.getFollowupList()
            if (res?.statuscode == 200) {
                _listState.value = DataState(
                    success = true,
                    isLoading = false,
                    data = res.data,
                    message = res.message
                )
            } else {
                _listState.value = DataState(
                    success = false,
                    isLoading = false,
                    error = res?.message ?: "Error Occurred: Please try again."
                )
            }
        }
    }


    data class DataState<T>(
        val success: Boolean = false,
        val isLoading: Boolean = false,
        val data: T? = null,
        val error: String? = null,
        val message: String? = null
    )
}
