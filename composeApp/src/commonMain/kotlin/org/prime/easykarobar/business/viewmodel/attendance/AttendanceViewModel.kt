package org.prime.easykarobar.business.viewmodel.attendance

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.prime.easykarobar.business.repository.attendance.AttendanceRepository
import org.prime.easykarobar.data.model.attendance.AttendanceListRequest
import org.prime.easykarobar.data.model.attendance.AttendanceListResponse
import org.prime.easykarobar.data.model.attendance.AttendanceRequest
import org.prime.easykarobar.data.model.attendance.AttendanceResponse
import org.prime.easykarobar.data.model.attendance.SalesmanList

class AttendanceViewModel : ViewModel() {
    private val _dataState = mutableStateOf(DataState<AttendanceResponse>())
    val dataState: State<DataState<AttendanceResponse>> = _dataState
    private val _listState = mutableStateOf(DataState<List<AttendanceListResponse>>())
    val listState: State<DataState<List<AttendanceListResponse>>> = _listState
    private val _salesmanList = mutableStateOf(DataState<List<SalesmanList>>())
    val salesmanList: State<DataState<List<SalesmanList>>> = _salesmanList

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

    fun getAttendance(attendanceRequest: AttendanceListRequest) {
        viewModelScope.launch {

            _listState.value = DataState(isLoading = true)

            val res = AttendanceRepository.getAttendanceList(attendanceRequest)

            if (res?.statuscode == 200) {
                _listState.value = DataState(
                    success = true, message = res.message, isLoading = false, data = res.data
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

    fun getSalesmanList() {
        viewModelScope.launch {

            _salesmanList.value = DataState(isLoading = true)

            val res = AttendanceRepository.getSalesmanList()

            if (res?.statuscode == 200) {
                _salesmanList.value = DataState(
                    success = true, message = res.message, isLoading = false, data = res.data
                )
            } else {
                _salesmanList.value = DataState(
                    success = false,
                    isLoading = false,
                    error = res?.message ?: "Error Occurred. Please try again."
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