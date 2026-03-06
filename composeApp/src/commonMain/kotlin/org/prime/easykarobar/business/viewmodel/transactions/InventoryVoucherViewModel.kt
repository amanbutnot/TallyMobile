package org.prime.easykarobar.business.viewmodel.transactions

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.prime.easykarobar.business.repository.transactions.DeleteResponse
import org.prime.easykarobar.business.repository.transactions.InventoryVoucherRepo
import org.prime.easykarobar.data.model.transactions.InventoryItemResponse
import org.prime.easykarobar.data.model.transactions.InventoryListRequest
import org.prime.easykarobar.data.model.transactions.InventoryListResponse
import org.prime.easykarobar.data.model.transactions.InventoryVoucherRequest
import org.prime.easykarobar.data.model.transactions.InventoryVoucherResponse

class InventoryVoucherViewModel : ViewModel() {

    private val _dataState = mutableStateOf(DataState<InventoryVoucherResponse>())
    val dataState: State<DataState<InventoryVoucherResponse>> = _dataState

    private val _oneState = mutableStateOf(DataState<InventoryItemResponse>())
    val oneState: State<DataState<InventoryItemResponse>> = _oneState
    private val _deleteState = mutableStateOf(DataState<DeleteResponse>())
    val deleteState: State<DataState<DeleteResponse>> = _deleteState

    private val _listState = mutableStateOf(DataState<List<InventoryListResponse>>())
    val listState: State<DataState<List<InventoryListResponse>>> = _listState


    fun createEditInventoryResponse(
        inventoryVoucherRequest: InventoryVoucherRequest,
        onSuccess: () -> Unit, url: String
    ) {
        viewModelScope.launch {

            _dataState.value = DataState(isLoading = true)

            println("CreateEditRequest: $inventoryVoucherRequest")
            val res = InventoryVoucherRepo.createInventoryVch(
                inventoryVoucherRequest = inventoryVoucherRequest,
                endpoint = url
            )

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

    fun clearError() {
        _dataState.value = _dataState.value.copy(error = null)
    }

    fun deleteInventoryVch(
        tranId: Int, vchType: Int,

        ) {
        viewModelScope.launch {

            _deleteState.value = DataState(isLoading = true)

            val res = InventoryVoucherRepo.deleteInventoryVoucher(
                tranId = tranId,
                vchType = vchType
            )

            if (res?.statuscode == 200) {
                _deleteState.value = DataState(
                    success = true,
                    isLoading = false,
                    data = res.data,
                    message = res.message
                )

            } else {
                _deleteState.value = DataState(
                    success = false,
                    isLoading = false,
                    error = res?.message ?: "Error Occurred: Please try again."
                )
            }
        }
    }


    fun getOneInventoryVoucher(tranId: Int) {
        viewModelScope.launch {

            _oneState.value = DataState(isLoading = true)

            val res = InventoryVoucherRepo.getInventoryVoucher(tranId)

            if (res?.statuscode == 200) {
                _oneState.value = DataState(
                    success = true,
                    isLoading = false,
                    data = res.data,
                    message = res.message
                )
            } else {
                _oneState.value = DataState(
                    success = false,
                    isLoading = false,
                    error = res?.message ?: "Error Occurred: Please try again."
                )
            }
        }
    }

    fun listInventoryVch(inventoryListRequest: InventoryListRequest) {
        viewModelScope.launch {

            _listState.value = DataState(isLoading = true)

            val res = InventoryVoucherRepo.listInventoryVch(inventoryListRequest)

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