package org.prime.easykarobar.ui.screen.attendance

import org.prime.easykarobar.ui.shared.reportsShared.CurrentDate
import org.prime.easykarobar.ui.shared.reportsShared.TallyDatePickerRow
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Filter1
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import dev.jordond.compass.Priority
import dev.jordond.compass.geolocation.Geolocator
import dev.jordond.compass.geolocation.GeolocatorResult
import dev.jordond.compass.geolocation.Locator
import dev.jordond.compass.geolocation.mobile.mobile
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.prime.easykarobar.business.viewmodel.attendance.AttendanceViewModel
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.expect.shareText
import org.prime.easykarobar.data.model.attendance.AttendanceListRequest
import org.prime.easykarobar.data.model.attendance.AttendanceListResponse
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.ui.printing.AttendanceRow
import org.prime.easykarobar.ui.printing.attendanceHtml
import org.prime.easykarobar.ui.screen.home.ROLE
import org.prime.easykarobar.ui.screen.home.userRole
import org.prime.easykarobar.ui.screen.transactions.TransactionBottomSheet
import org.prime.easykarobar.ui.shared.composables.MenuItemData
import org.prime.easykarobar.ui.shared.composables.TallyButton
import org.prime.easykarobar.ui.shared.composables.TallyCircularLoader
import org.prime.easykarobar.ui.shared.composables.TallyLoadingDialog
import org.prime.easykarobar.ui.shared.composables.TallyReportScaffold
import org.prime.easykarobar.ui.shared.composables.TallyScaffold
import org.prime.easykarobar.ui.shared.composables.TallySearchBar
import org.prime.easykarobar.ui.shared.composables.smartSearch
import org.prime.easykarobar.ui.shared.globalShared.StartDate
import org.prime.easykarobar.ui.shared.globalShared.Tdate
import org.prime.easykarobar.ui.shared.globalShared.extractNumericValue
import org.prime.easykarobar.ui.shared.globalShared.getLedgerMasters
import org.prime.easykarobar.ui.shared.globalShared.googleMapsLink
import org.prime.easykarobar.ui.shared.globalShared.parseDate
import org.prime.easykarobar.ui.shared.reportsShared.PdfAction
import org.prime.easykarobar.ui.shared.reportsShared.handlePdfAction

data class AttendanceListScreen(val isCheckIn: Boolean, val name: String) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        var showLoading by remember { mutableStateOf(false) }
        var showLocationPopup by remember { mutableStateOf(false) }
        var showError by remember { mutableStateOf(false) }
        TallyScaffold(name, onBack = { nav.pop() }) { paddingValues ->
            var startDate by rememberSaveable { mutableStateOf(StartDate()) }
            var endDate by rememberSaveable { mutableStateOf(CurrentDate()) }
            var selectedAccount by rememberSaveable { mutableStateOf("") }
            var selectedMobile by rememberSaveable { mutableStateOf("") }
            var showBottomSheet by remember { mutableStateOf(false) }

            var reportType by rememberSaveable { mutableStateOf("ALL") }

            val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            val viewModel: AttendanceViewModel = viewModel { AttendanceViewModel() }
            val isAdmin = SharedPrefs.User.get()?.role == "admin"
            val scope = rememberCoroutineScope()

            val vState by viewModel.salesmanList
            val nameList = vState.data ?: emptyList()
            LaunchedEffect(Unit) {
                viewModel.getSalesmanList()
                println(vState.data)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .navigationBarsPadding()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Compact Header
                Text(
                    text = if (isCheckIn) "Check In Configuration" else "Attendance Configuration",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Main Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        if (userRole() != ROLE.STAFF_MANAGER) {
                            OutlinedButton(
                                onClick = {
                                    if (!isCheckIn) {
                                        scope.launch {
                                            showLoading = true
                                            try {
                                                val geolocator =
                                                    Geolocator(Locator.mobile())

                                                runCatching { geolocator.lastLocation() }

                                                val result = withTimeoutOrNull(20000) {
                                                    geolocator.current(Priority.HighAccuracy)
                                                }

                                                when (result) {
                                                    is GeolocatorResult.Success -> {
                                                        val c = result.data.coordinates

                                                        nav?.push(
                                                            AttendanceScreen(
                                                                c.latitude,
                                                                c.longitude,

                                                                isAttendance = true
                                                            )
                                                        )
                                                    }

                                                    else -> showLocationPopup = true
                                                }

                                            } catch (e: Exception) {
                                                showLocationPopup = true
                                            } finally {
                                                showLoading = false
                                            }
                                        }
                                    } else {
                                        scope.launch {
                                            showLoading = true
                                            try {
                                                val geolocator =
                                                    Geolocator(Locator.mobile())

                                                runCatching { geolocator.lastLocation() }

                                                val result = withTimeoutOrNull(20000) {
                                                    geolocator.current(Priority.HighAccuracy)
                                                }

                                                when (result) {
                                                    is GeolocatorResult.Success -> {
                                                        val c = result.data.coordinates

                                                        nav?.push(
                                                            AttendanceScreen(
                                                                c.latitude,
                                                                c.longitude,

                                                                isAttendance = false
                                                            )
                                                        )
                                                    }

                                                    else -> showLocationPopup = true
                                                }

                                            } catch (e: Exception) {
                                                showLocationPopup = true
                                            } finally {
                                                showLoading = false
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth().padding(12.dp)
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(
                                    1.5.dp,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Add New",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }

                        }

                        // Report Type Section (Only for Admin and Check-in)
                        if (isAdmin) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Attendance Type",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // All Salesmen Option
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                reportType = "ALL"
                                                selectedAccount = ""
                                                selectedMobile = ""
                                            },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (reportType == "ALL")
                                            MaterialTheme.colorScheme.primaryContainer
                                        else
                                            MaterialTheme.colorScheme.surfaceContainer,
                                        border = BorderStroke(
                                            1.5.dp,
                                            if (reportType == "ALL")
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.outlineVariant
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            RadioButton(
                                                selected = reportType == "ALL",
                                                onClick = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                "All Salesmen",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = if (reportType == "ALL")
                                                    MaterialTheme.colorScheme.onPrimaryContainer
                                                else
                                                    MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }

                                    // Single Salesman Option
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { reportType = "SINGLE" },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (reportType == "SINGLE")
                                            MaterialTheme.colorScheme.primaryContainer
                                        else
                                            MaterialTheme.colorScheme.surfaceContainer,
                                        border = BorderStroke(
                                            1.5.dp,
                                            if (reportType == "SINGLE")
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.outlineVariant
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            RadioButton(
                                                selected = reportType == "SINGLE",
                                                onClick = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                "Single Salesman",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = if (reportType == "SINGLE")
                                                    MaterialTheme.colorScheme.onPrimaryContainer
                                                else
                                                    MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }

                            // Salesman Selection (only for SINGLE)
                            if (reportType == "SINGLE") {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "Select Salesman",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { showBottomSheet = true },
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceContainer,
                                        border = BorderStroke(
                                            1.dp,
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 14.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = selectedAccount.ifEmpty { "Choose a salesman" },
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = if (selectedAccount.isEmpty())
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                                else
                                                    MaterialTheme.colorScheme.onSurface
                                            )
                                            Icon(
                                                imageVector = Icons.Outlined.KeyboardArrowDown,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )
                        }

                        // Date Range Section
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            TallyDatePickerRow(
                                label = "Start Date",
                                selectedDate = startDate,
                                onDateSelected = { startDate = it },
                                defaultDate = CurrentDate()
                            )

                            TallyDatePickerRow(
                                label = "End Date",
                                selectedDate = endDate,
                                defaultDate = CurrentDate(),
                                onDateSelected = { endDate = it }
                            )
                            // Error Message with Animation
                            AnimatedVisibility(
                                visible = showError,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            )
                            {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(18.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ErrorOutline,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(26.dp)
                                        )
                                        Text(
                                            text = "End date cannot be earlier than start date",
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Generate Button
                TallyButton(
                    label = "Generate",
                    onClick = {
                        if (parseDate(startDate) > parseDate(endDate)) {
                            showError = true
                        } else {
                            showError = false
                            nav.push(
                                AttendanceScreenUi(
                                    startDate = startDate,
                                    endDate = endDate,
                                    accountName = if (isAdmin && reportType == "SINGLE") selectedMobile else "",
                                    vchType = if (isCheckIn) 2 else 1, isCheckIn = isCheckIn
                                )
                            )

                        }
                    },
                    enabled = (startDate.isNotEmpty()) &&
                            (endDate.isNotEmpty()) && (!showError) &&
                            (if (isAdmin && isCheckIn) {
                                if (reportType == "SINGLE") selectedMobile.isNotEmpty() else true
                            } else true),
                    backgroundColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }

            // Bottom Sheet
            TransactionBottomSheet(
                showBottomSheet = showBottomSheet,
                list = nameList.map {
                    Pair("${it.salesman_name} (${it.salesman_mobile})", it.salesman_mobile)
                },
                onSelected = {
                    it.let {
                        selectedAccount = it.first
                        selectedMobile = it.second
                    }
                },
                onDismiss = { showBottomSheet = false },
                bottomSheetState = state,
                title = "Select Salesman"
            )
        }
        if (showLocationPopup) {
            AlertDialog(
                onDismissRequest = { showLocationPopup = false },
                title = { Text("Location is Off") },
                text = { Text("Please enable location to continue.") },
                confirmButton = {
                    Button(onClick = { showLocationPopup = false }) {
                        Text("OK")
                    }
                }
            )
        }

        if (showLoading) {
            TallyLoadingDialog("Getting Location")
        }
    }
}

data class AttendanceScreenUi(
    val startDate: String,
    val endDate: String,
    val vchType: Int,
    val accountName: String,
    val isCheckIn: Boolean
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {

        val viewModel: AttendanceViewModel = viewModel { AttendanceViewModel() }
        val state by viewModel.listState
        val bottomState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        var shareLoading by remember { mutableStateOf(false) }
        var showFilterBottomSheet by remember { mutableStateOf(false) }
        val db = DatabaseHolder.instance
        val list = getLedgerMasters(db)
        var selectedAccount by remember { mutableStateOf("") }
        var selectedGUID by remember { mutableStateOf("") }
        val nameListFilter = list.map { (it.Name ?: "") to (it.GUID ?: "") }
        val scope = rememberCoroutineScope()
        var showSearchBar by remember { mutableStateOf(false) }

        var searchQuery by remember { mutableStateOf("") }
        val focusRequester = remember { FocusRequester() }

        val rows = state.data?.map {
            AttendanceRow(
                date = it.LocationDateTime.take(10),
                time = it.LocationDateTime.substring(11).take(8),
                salesmen = extractNumericValue(it.C1),
                party = it.PartyName.toString(),
                status = if (it.RecType == 1) "In" else "Out",
                address = it.C4,
                photoUrl = it.C5
            )
        }

        val htmlContent = attendanceHtml(
            title = if (isCheckIn) "Check In" else "Attendance",
            rows = rows as List<AttendanceRow>,
            startDate = startDate, endDate = endDate, isCheckIn = isCheckIn
        )

        val menuItems = listOf(
            MenuItemData(
                title = "Filter",
                icon = Icons.Default.Filter1,
                onClick = {

                    showFilterBottomSheet = true
                }
            )
        )
        if (shareLoading) {
            TallyLoadingDialog("Generating Report")
        }
        TallyReportScaffold(
            title = if (isCheckIn) "Check In/Out Records" else "Attendance Records",
            showBurgerMenu = true,
            onDownloadClick = {
                scope.launch {
                    handlePdfAction(
                        fileName = if (isCheckIn) "Check In" else "Attendance",
                        htmlContent = htmlContent,
                        action = PdfAction.Download,
                        onLoadingChange = { shareLoading = it }
                    )
                }
            },
            onShareClick = {
                scope.launch {
                    handlePdfAction(
                        fileName = if (isCheckIn) "Check In" else "Attendance",
                        htmlContent = htmlContent,
                        action = PdfAction.Share,
                        onLoadingChange = { shareLoading = it }
                    )
                }
            },
            onExcelClick = {
                scope.launch {
                    val excelRows = (rows as List<AttendanceRow>).map { item ->
                        listOf(
                            item.date,
                            item.time,
                            item.party,
                            item.status,
                            item.address
                        )
                    }
                    handlePdfAction(
                        fileName = if (isCheckIn) "Check_In_Out_Report" else "Attendance_Report",
                        htmlContent = htmlContent,
                        headers = listOf("Date", "Time", "Party", "Status", "Address"),
                        rows = excelRows,
                        action = PdfAction.DownloadExcel,
                        onLoadingChange = { shareLoading = it }
                    )
                }
            },
            menuItems = menuItems,
            showSearchAction = true,
            onSearchClick = { showSearchBar = !showSearchBar },
            showBottomBar = false,
            bottomBarContent = { },
            content = { paddingValues ->


                LaunchedEffect(Unit) {
                    viewModel.getAttendance(
                        attendanceRequest = AttendanceListRequest(
                            VchType = vchType,
                            StartDate = startDate,
                            EndDate = endDate,
                            salesman_mobile = accountName
                        )
                    )
                }
                val filteredList = smartSearch(
                    list = state.data.orEmpty(),
                    query = searchQuery,
                    selectors = listOf { it.C1 }
                )
                val finalList = if (selectedAccount.isNotEmpty()) {
                    println(selectedAccount)
                    filteredList.filter { "\\[(.*?)\\]".toRegex()
                        .find(it.C1?:"")
                        ?.groupValues
                        ?.get(1) == selectedAccount }
                } else {
                    filteredList
                }
                when {
                    state.isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                            contentAlignment = Alignment.Center
                        ) {
                            TallyCircularLoader()
                        }
                    }

                    else -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                        ) {
                            if (showSearchBar) {
                                TallySearchBar(
                                    searchQuery = searchQuery,
                                    onQueryChange = { searchQuery = it },
                                    modifier = Modifier.focusRequester(focusRequester)
                                )
                            }
                            AttendanceListContent(
                                modifier = Modifier,
                                list = finalList, isCheckIn = isCheckIn
                            )

                        }
                    }
                }
            }
        )
        TransactionBottomSheet(
            showBottomSheet = showFilterBottomSheet,
            list = nameListFilter,
            onSelected = {
                selectedAccount = it.first
                selectedGUID = it.second
            },
            onDismiss = { showFilterBottomSheet = false },
            bottomSheetState = bottomState
        )
    }
}

/* ---------- UI BELOW (NO STRUCTURE CHANGES ABOVE) ---------- */

@Composable
private fun AttendanceListContent(
    modifier: Modifier,
    list: List<AttendanceListResponse>, isCheckIn: Boolean
) {
    if (list.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
                Text(
                    text = "No attendance records found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Try adjusting your filters",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
        return
    }

    Column(modifier = modifier) {
        // Summary Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Records",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "${list.size}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(list) { item ->
                AttendanceItem(item, isCheckIn)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AttendanceItem(item: AttendanceListResponse, isCheckIn: Boolean) {

    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    // -------- Bottom Sheet --------
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState
        )
        {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {

                // Close button (explicit, not relying on swipe)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = { showSheet = false }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }

                AsyncImage(
                    model = item.C5,
                    contentDescription = "Attendance photo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = item.C1 ?: "Unknown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = buildString {
                        append(Tdate(item.LocationDateTime.take(10)))
                        if (item.LocationDateTime.length > 10) {
                            append(" • ")
                            append(item.LocationDateTime.substring(11).take(8))
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (item.C4.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = item.C4,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
    var shareLocation by remember { mutableStateOf(false) }

    if (shareLocation) {
        shareText(
            googleMapsLink(
                item.C2,
                item.C3
            )
        )
        shareLocation = false
    }
    // -------- List Item (UNCHANGED UI) --------
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showSheet = true } // whole row clickable
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            if (item.C5.isNotBlank()) {
                Surface(
                    modifier = Modifier.size(120.dp),
                    shape = RoundedCornerShape(16.dp),
                    tonalElevation = 2.dp,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    AsyncImage(
                        model = item.C5,
                        contentDescription = "Attendance photo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.Center
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {

                Text(
                    text = item.C1 ?: "Unknown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = Tdate(item.LocationDateTime.take(10)),
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (item.LocationDateTime.length > 10) {
                            Text(
                                text = item.LocationDateTime.substring(11).take(8),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = if (item.RecType == 1) "In" else "Out",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = if (item.RecType == 1) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )

                    }


                }

                if (item.C4.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.C4,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Share Location
                OutlinedButton(
                    onClick = { shareLocation = true },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Share Location",
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

            }
        }
    }
}

