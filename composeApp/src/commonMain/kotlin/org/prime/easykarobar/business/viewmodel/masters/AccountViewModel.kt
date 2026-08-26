package org.prime.easykarobar.business.viewmodel.masters

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.prime.easykarobar.business.repository.masters.AccountRepository
import org.prime.easykarobar.data.model.AccountItemResponse
import org.prime.easykarobar.ui.screen.masters.AccountModel
import org.prime.easykarobar.ui.screen.masters.CreateItemResponse
import org.prime.easykarobar.ui.screen.masters.ItemFormData


class AccountViewModel : ViewModel() {
    private val _dataState = mutableStateOf(DataState<AccountModel>())
    val dataState: State<DataState<AccountModel>> = _dataState
    private val _itemDataState = mutableStateOf(DataState<CreateItemResponse>())
    val itemDataState: State<DataState<CreateItemResponse>> = _itemDataState

    private val _listState = mutableStateOf(DataState<AccountItemResponse>())
    val listState: State<DataState<AccountItemResponse>> = _listState
    fun createAccount(accountModel: AccountModel, onResult: () -> Unit) {
        viewModelScope.launch {

            _dataState.value = DataState(isLoading = true)

            val res = AccountRepository.createAccount(accountModel)

            if (res?.statuscode == 200) {
                _dataState.value = DataState(
                    success = true,
                    isLoading = false,
                    data = res.data,
                    message = res.message
                )
            } else {
                _dataState.value = DataState(
                    success = false,
                    isLoading = false,
                    error = res?.message ?: "Error Occurred: Please try again."
                )
            }
            onResult()
        }
    }

    fun createItem(itemFormData: ItemFormData, onSuccess: () -> Unit) {
        viewModelScope.launch {

            _itemDataState.value = DataState(isLoading = true)

            val res = AccountRepository.createItem(itemFormData)

            if (res?.statuscode == 200) {
                _itemDataState.value = DataState(
                    success = true,
                    isLoading = false,
                    data = res.data,
                    message = res.message
                )
                onSuccess()
            } else {
                println("Error Occurredfjaklsfjlasdjfl;jl;")
                _itemDataState.value = DataState(
                    success = false,
                    isLoading = false,
                    error = res?.message ?: "Error Occurred: Please try again."
                )
            }
        }
    }


    fun listAccount(onSuccess: () -> Unit) {
        viewModelScope.launch {

            _listState.value = DataState(isLoading = true)

            val res = AccountRepository.listAccount()

            if (res?.statuscode == 200) {
                _listState.value = DataState(
                    success = true,
                    isLoading = false,
                    data = res,
                    message = res.message
                )
                onSuccess()
            } else {
                println("error from viewmodel")
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