package org.prime.easykarobar.business.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.prime.easykarobar.BuildKonfig
import org.prime.easykarobar.business.repository.WishlistRepository
import org.prime.easykarobar.data.model.WishlistItem
import org.prime.easykarobar.data.model.WishlistRequest

class WishlistViewModel : ViewModel() {
    private val _addState = mutableStateOf(DataState<Unit>())
    val addState: State<DataState<Unit>> = _addState

    private val _listState = mutableStateOf(DataState<List<WishlistItem>>())
    val listState: State<DataState<List<WishlistItem>>> = _listState

    private val _deleteState = mutableStateOf(DataState<Unit>())
    val deleteState: State<DataState<Unit>> = _deleteState

    fun addWishlist(itemGuid: String, groupGuid: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _addState.value = DataState(isLoading = true)
            val request = WishlistRequest(
                mobile_no = BuildKonfig.USERNAME,
                item_name = itemGuid,
                group_name = groupGuid
            )
            val res = WishlistRepository.addWishlist(request)
            if (res?.statuscode == 200) {
                _addState.value = DataState(success = true, message = res.message, isLoading = false)
                onSuccess()
                getWishlist() // Refresh list after adding
            } else {
                _addState.value = DataState(
                    success = false,
                    isLoading = false,
                    error = res?.message ?: "Error adding to wishlist"
                )
            }
        }
    }

    fun getWishlist() {
        viewModelScope.launch {
            _listState.value = DataState(isLoading = true)
            val res = WishlistRepository.getWishlist(BuildKonfig.USERNAME)
            if (res?.statuscode == 200) {
                _listState.value = DataState(
                    success = true,
                    isLoading = false,
                    data = res.data
                )
            } else {
                _listState.value = DataState(
                    success = false,
                    isLoading = false,
                    error = res?.message ?: "Error getting wishlist"
                )
            }
        }
    }

    fun deleteWishlist(itemGuid: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _deleteState.value = DataState(isLoading = true)
            val res = WishlistRepository.deleteWishlist(BuildKonfig.USERNAME, itemGuid)
            if (res?.statuscode == 200) {
                _deleteState.value = DataState(success = true, message = res.message, isLoading = false)
                onSuccess()
                getWishlist() // Refresh list after deleting
            } else {
                _deleteState.value = DataState(
                    success = false,
                    isLoading = false,
                    error = res?.message ?: "Error deleting from wishlist"
                )
            }
        }
    }

    fun clearAddError() {
        _addState.value = _addState.value.copy(error = null)
    }

    fun clearDeleteError() {
        _deleteState.value = _deleteState.value.copy(error = null)
    }

    data class DataState<T>(
        val success: Boolean = false,
        val isLoading: Boolean = false,
        val data: T? = null,
        val error: String? = null,
        val message: String? = null
    )
}
