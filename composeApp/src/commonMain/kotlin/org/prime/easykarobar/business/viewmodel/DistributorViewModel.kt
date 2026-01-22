package org.prime.easykarobar.business.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.prime.easykarobar.business.repository.DistributorRepository
import org.prime.easykarobar.data.model.DistributorRequest
import org.prime.easykarobar.data.model.DistributorResponse

class DistributorViewModel : ViewModel() {
    private val _dataState = mutableStateOf(DataState<DistributorResponse>())
    val dataState: State<DataState<DistributorResponse>> = _dataState
    private val _updateState = mutableStateOf(DataState<DistributorResponse>())
    val updateState: State<DataState<DistributorResponse>> = _updateState
    private val _listState = mutableStateOf(DataState<List<DistributorRequest>>())
    val listState: State<DataState<List<DistributorRequest>>> = _listState


    fun createDistributor(distributorRequest: DistributorRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {

            _dataState.value = DataState(isLoading = true)

            val res = DistributorRepository.createDistributor(distributorRequest)

            if (res?.statuscode == 200) {
                _dataState.value = DataState(
                    success = true, message = res.message, isLoading = false
                )
                onSuccess()
            } else {
                _dataState.value = DataState(
                    success = false,
                    isLoading = false,
                    error = res?.message ?: "Error Occurred. Please try again."
                )
            }
        }
    }

    fun updateDistributor(distributorRequest: DistributorRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {

            _updateState.value = DataState(isLoading = true)

            val res = DistributorRepository.updateDistributor(distributorRequest)

            if (res?.statuscode == 200) {
                _updateState.value = DataState(
                    success = true, message = res.message, isLoading = false
                )
                onSuccess()
            } else {
                _updateState.value = DataState(
                    success = false,
                    isLoading = false,
                    error = res?.message ?: "Error Occurred. Please try again."
                )
            }
        }
    }

    fun listDistributor() {

        viewModelScope.launch {
            _listState.value = DataState(isLoading = true)

            val res = DistributorRepository.listDistributor()
            if (res?.statuscode == 200) {

                _listState.value = DataState(
                    success = true,
                    message = res.message,
                    isLoading = false,
                    data = res.data
                )

            } else {

                _listState.value = DataState(
                    success = false,
                    isLoading = false,
                    error = res?.message ?: "Error Occurred. Please try again."
                )
            }

        }
    }


    fun clearError() {
        _dataState.value = _dataState.value.copy(error = null)
    }


    data class DataState<T>(
        val success: Boolean = false,
        val isLoading: Boolean = false,
        val data: T? = null,
        val error: String? = null,
        val message: String? = null
    )
}
