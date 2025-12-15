package org.prime.tally.business.viewmodel.transactions

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.prime.tally.business.repository.transactions.InventoryVoucherRepo
import org.prime.tally.data.model.transactions.InventoryVoucherRequest
import org.prime.tally.data.model.transactions.InventoryVoucherResponse

class InventoryVoucherViewModel : ViewModel() {

    private val _dataState = mutableStateOf(DataState<InventoryVoucherResponse>())
    val dataState: State<DataState<InventoryVoucherResponse>> = _dataState


    fun createInventoryVoucher(inventoryVoucherRequest: InventoryVoucherRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {

            _dataState.value = DataState(isLoading = true)

            val res = InventoryVoucherRepo.createInventoryVch(inventoryVoucherRequest)

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

    data class DataState<T>(
        val success: Boolean = false,
        val isLoading: Boolean = false,
        val data: T? = null,
        val error: String? = null,
        val message: String? = null
    )
}