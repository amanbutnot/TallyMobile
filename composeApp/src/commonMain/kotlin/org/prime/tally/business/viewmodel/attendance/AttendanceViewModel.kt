package org.prime.tally.business.viewmodel.attendance

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.prime.tally.business.repository.attendance.AttendanceRepository
import org.prime.tally.data.model.attendance.AttendanceRequest
import org.prime.tally.data.model.attendance.AttendanceResponse

class AttendanceViewModel : ViewModel() {
    private val _dataState = mutableStateOf(DataState())
    val dataState: State<DataState> = _dataState

    fun sendAttendance(attendanceRequest: AttendanceRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {

            _dataState.value = DataState(isLoading = true)

            val res = AttendanceRepository.sendAttendance(attendanceRequest)

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


    data class DataState(
        val success: Boolean = false,
        val isLoading: Boolean = false,
        val data: AttendanceResponse? = null,
        val error: String? = null,
        val message: String? = null
    )
}