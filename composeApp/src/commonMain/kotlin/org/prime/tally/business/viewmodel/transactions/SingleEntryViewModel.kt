package org.prime.tally.business.viewmodel.transactions

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.prime.tally.business.repository.transactions.SingleEntryRepository
import org.prime.tally.data.model.transactions.TranListRequest
import org.prime.tally.data.model.transactions.TranListResponse
import org.prime.tally.data.model.transactions.TranRequest
import org.prime.tally.data.model.transactions.TranResponse

class SingleEntryViewModel : ViewModel() {

    private val _dataState = mutableStateOf(DataState<TranResponse>())
    val dataState: State<DataState<TranResponse>> = _dataState
    private val _listState = mutableStateOf(DataState<List<TranListResponse>>())
    val listState: State<DataState<List<TranListResponse>>> = _listState


    fun addSingleTran(tranRequest: TranRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {

            _dataState.value = DataState(isLoading = true)

            val res = SingleEntryRepository.saveTransaction(tranRequest)

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

    fun updateSingleTran(tranRequest: TranRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {

            _dataState.value = DataState(isLoading = true)

            val res = SingleEntryRepository.modifyTransaction(tranRequest)

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

    fun listTrans(tranListRequest: TranListRequest) {
        viewModelScope.launch {

            _listState.value = DataState(isLoading = true)

            val res = SingleEntryRepository.listTransactions(tranListRequest)

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