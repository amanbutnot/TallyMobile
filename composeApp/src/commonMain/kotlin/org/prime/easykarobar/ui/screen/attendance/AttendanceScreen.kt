package org.prime.easykarobar.ui.screen.attendance

import CurrentDate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Report
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import com.mohamedrejeb.calf.permissions.ExperimentalPermissionsApi
import com.mohamedrejeb.calf.permissions.Permission
import com.mohamedrejeb.calf.permissions.isGranted
import com.mohamedrejeb.calf.permissions.rememberPermissionState
import dev.jordond.compass.Place
import dev.jordond.compass.geocoder.Geocoder
import dev.jordond.compass.geocoder.mobile
import dev.jordond.compass.geocoder.placeOrNull
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.ImageFormat
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.compressImage
import io.github.vinceglb.filekit.dialogs.FileKitCameraFacing
import io.github.vinceglb.filekit.dialogs.FileKitCameraType
import io.github.vinceglb.filekit.dialogs.openCameraPicker
import io.github.vinceglb.filekit.path
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.launch
import org.prime.easykarobar.business.viewmodel.attendance.AttendanceViewModel
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.model.attendance.AttendanceRequest
import org.prime.easykarobar.data.model.salesmanPermission
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.ui.screen.home.ROLE
import org.prime.easykarobar.ui.screen.home.userRole
import org.prime.easykarobar.ui.screen.reports.ledger.LedgerReportScreen
import org.prime.easykarobar.ui.screen.reports.outstanding.OutstandingReportScreen
import org.prime.easykarobar.ui.screen.reports.registers.RegisterReportScreen
import org.prime.easykarobar.ui.screen.transactions.SelectLedgerRow
import org.prime.easykarobar.ui.screen.transactions.TransactionBottomSheet
import org.prime.easykarobar.ui.screen.transactions.sale.SaleScreen
import org.prime.easykarobar.ui.shared.composables.PermissionDeniedDialog
import org.prime.easykarobar.ui.shared.composables.TallyButton
import org.prime.easykarobar.ui.shared.composables.TallyCircularLoader
import org.prime.easykarobar.ui.shared.composables.TallyLoadingDialog
import org.prime.easykarobar.ui.shared.composables.TallyResultDialog
import org.prime.easykarobar.ui.shared.composables.TallyScaffold
import org.prime.easykarobar.ui.shared.globalShared.StartDate
import org.prime.easykarobar.ui.shared.globalShared.getLedgerMasters
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

data class AttendanceScreen(
    val lat: Double, val lon: Double, val isAttendance: Boolean
) : Screen {

    @OptIn(
        ExperimentalMaterial3Api::class, ExperimentalEncodingApi::class,
        ExperimentalPermissionsApi::class
    )
    @Composable
    override fun Content() {
        var address by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(false) }


        LaunchedEffect(Unit) {
            isLoading = true
            address =
                getPlaceFromCoordinates(lat, lon)?.let { formatAddress(it) } ?: "Address not found"
            isLoading = false
        }


        val viewModel: AttendanceViewModel = viewModel { AttendanceViewModel() }
        val state by viewModel.dataState
        var showAlert by remember { mutableStateOf(false) }

        val nav = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        var capturedFile by remember { mutableStateOf<PlatformFile?>(null) }
        var capturedFileInBytes by remember { mutableStateOf("") }
        val colors = MaterialTheme.colorScheme
        val typography = MaterialTheme.typography
        val scrollState = rememberScrollState()
        var selectedAccount by remember { mutableStateOf("") }
        var selectedGUID by remember { mutableStateOf("") }
        var showDeniedDialog by remember { mutableStateOf(false) }

        if (showDeniedDialog) {
            PermissionDeniedDialog { showDeniedDialog = false }
        }

        LaunchedEffect(Unit) {
            selectedAccount = SharedPrefs.CheckInOutLedger.get() ?: ""
        }
        var showBottomSheet by rememberSaveable { mutableStateOf(false) }

        val db = DatabaseHolder.instance
        val list = getLedgerMasters(db)

        val lastAttendanceDate = SharedPrefs.AttendanceDate.get()
        var lastCheckInOutDate by remember { mutableStateOf(SharedPrefs.CheckInOutDate.get()) }

        val buttonName = if (isAttendance) {
            if (isCheckIn(lastAttendanceDate)) "Attendance In" else "Attendance Out"
        } else {
            if (userRole() == ROLE.OFFICE_STAFF) {
                "Check"
            } else {
                if (isCheckIn(lastCheckInOutDate)) "Check In" else "Check Out"
            }

        }

        if (state.isLoading) {
            TallyLoadingDialog("Please Wait")
        }


        TallyScaffold(
            title = if (isAttendance) "Attendance" else "Check In/Check Out",
            onBack = { nav.pop() },
            content = { paddingValues ->

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        TallyCircularLoader()
                    }
                } else {

                    Column(
                        modifier = Modifier.fillMaxSize().background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    colors.surface, colors.surfaceVariant.copy(alpha = 0.3f)
                                )
                            )
                        ).verticalScroll(scrollState).padding(paddingValues)
                            .padding(horizontal = 8.dp, vertical = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {


                        if (!isAttendance) {
                            SelectLedgerRow(
                                selectedAccount = selectedAccount,
                                onShowBottomSheet = { showBottomSheet = true },
                                title = "Ledger",
                                enabled = if (userRole() == ROLE.OFFICE_STAFF) true else isCheckIn(
                                    lastCheckInOutDate
                                )
                            )
                        }
                        val bottomSheetList = if (userRole() == ROLE.OFFICE_STAFF) {
                            list.filter { it.L5 == 1.0 }
                                .map { Pair(it.Name ?: "", it.GUID ?: "") }
                        } else {
                            list.filter { it.L1 == 1.0 || it.L2 == 1.0 || it.L3 == 1.0 }
                                .map { Pair(it.Name ?: "", it.GUID ?: "") }
                        }
                        TransactionBottomSheet(
                            showBottomSheet = showBottomSheet,
                            list = bottomSheetList,
                            onSelected = {
                                it.let {
                                    selectedAccount = it.first
                                    selectedGUID = it.second
                                    println("Selected Account: ${it.first} and selected GUID is ${it.second}")
                                }
                            },
                            onDismiss = { showBottomSheet = false },
                            bottomSheetState = rememberModalBottomSheetState(
                                skipPartiallyExpanded = true
                            ),
                            title = "Ledger Name",
                        )

                        ElegantCard(
                            icon = Icons.Default.LocationOn,
                            title = "Location Details",
                            iconTint = colors.primary,
                            modifier = Modifier
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    address,
                                    style = typography.bodyMedium,
                                    color = colors.onSurface,
                                    lineHeight = typography.bodyMedium.lineHeight
                                )
                            }
                        }
                        if (userRole() != ROLE.OFFICE_STAFF) {
                            if (!isAttendance && !isCheckIn(lastCheckInOutDate)) {
                                ElegantCard(
                                    icon = Icons.Default.Report,
                                    title = "Reports",
                                    iconTint = colors.secondary,
                                    content = {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            ReportButton(
                                                label = "Account Ledger",
                                                icon = Icons.Default.AccountBalance,
                                                onClick = {
                                                    salesmanPermission(
                                                        "D7",
                                                        accessDeniedBlock = {
                                                            showDeniedDialog = true
                                                        },
                                                        successBlock = {
                                                            nav.push(
                                                                LedgerReportScreen(
                                                                    accountName = selectedAccount,
                                                                    startDate = StartDate(),
                                                                    endDate = CurrentDate()
                                                                )
                                                            )

                                                        }
                                                    )

                                                }
                                            )

                                            ReportButton(
                                                label = "Bill Receivable",
                                                icon = Icons.Default.Receipt,
                                                onClick = {
                                                    salesmanPermission(
                                                        "D8",
                                                        accessDeniedBlock = {
                                                            showDeniedDialog = true
                                                        },
                                                        successBlock = {
                                                            nav.push(
                                                                OutstandingReportScreen(
                                                                    name = "Bill Receivable",
                                                                    startDate = StartDate(),
                                                                    endDate = CurrentDate(),
                                                                    cm1 = selectedAccount,
                                                                    calculateDays = "Due Date",
                                                                    showOtherToggle = false,
                                                                )
                                                            )
                                                        }
                                                    )

                                                }
                                            )

                                            ReportButton(
                                                label = "Receipt",
                                                icon = Icons.Default.Payments,
                                                onClick = {
                                                    salesmanPermission(
                                                        "D15",
                                                        accessDeniedBlock = {
                                                            showDeniedDialog = true
                                                        },
                                                        successBlock = {
                                                            nav.push(
                                                                RegisterReportScreen(
                                                                    name = "Receipt",
                                                                    startDate = StartDate(),
                                                                    endDate = CurrentDate()
                                                                )
                                                            )
                                                        }
                                                    )
                                                }
                                            )

                                            ReportButton(
                                                label = "Pending Sale Order",
                                                icon = Icons.Default.PendingActions,
                                                onClick = {
                                                    nav.push(
                                                        OutstandingReportScreen(
                                                            name = "Pending Sale Order",
                                                            startDate = StartDate(),
                                                            endDate = CurrentDate(),
                                                            cm1 = selectedAccount,
                                                            calculateDays = "Due Date",
                                                            showOtherToggle = false,
                                                        )
                                                    )
                                                }
                                            )

                                            ReportButton(
                                                label = "Bill Payable",
                                                icon = Icons.Default.CreditCard,
                                                onClick = {
                                                    salesmanPermission(
                                                        "D9",
                                                        accessDeniedBlock = {
                                                            showDeniedDialog = true
                                                        },
                                                        successBlock = {
                                                            nav.push(
                                                                OutstandingReportScreen(
                                                                    name = "Bill Payable",
                                                                    startDate = StartDate(),
                                                                    endDate = CurrentDate(),
                                                                    cm1 = selectedAccount,
                                                                    calculateDays = "Due Date",
                                                                    showOtherToggle = false,
                                                                )
                                                            )
                                                        }
                                                    )
                                                }
                                            )
                                        }
                                    }
                                )

                                ElegantCard(
                                    icon = Icons.Default.Create,
                                    title = "Create",
                                    iconTint = colors.secondary,
                                    content = {
                                        ReportButton(
                                            label = "Create Sale Order",
                                            icon = Icons.Default.Create,
                                            onClick = {
                                                salesmanPermission(
                                                    "D20",
                                                    accessDeniedBlock = { showDeniedDialog = true },
                                                    successBlock = {
                                                        nav.push(
                                                            SaleScreen(
                                                                name = "Sale Order",
                                                                vchType = 12,
                                                                selectedLedger = selectedAccount,
                                                                //  selectedLedgerGUID = selectedGUID
                                                            )
                                                        )
                                                    })


                                            }
                                        )

                                    }
                                )
                            }

                        }



                        ElegantCard(
                            icon = Icons.Default.PhotoCamera,
                            title = "Camera Capture",
                            iconTint = colors.secondary,
                            modifier = Modifier
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val cameraPermission = rememberPermissionState(
                                    Permission.Camera
                                )
                                Button(
                                    modifier = Modifier.fillMaxWidth().height(52.dp),
                                    onClick = {
                                        if (cameraPermission.status.isGranted) {
                                            scope.launch {
                                                val file = FileKit.openCameraPicker(
                                                    type = FileKitCameraType.Photo,
                                                    cameraFacing = FileKitCameraFacing.Back
                                                )
                                                file?.let {
                                                    val bytes = it.readBytes()
                                                    val compressedBytes = FileKit.compressImage(
                                                        bytes = bytes,
                                                        quality = 30, // 0-100, where 100 is highest quality
                                                        maxWidth = 1024, // Optional maximum width
                                                        maxHeight = 1024, // Optional maximum height
                                                        imageFormat = ImageFormat.JPEG // JPEG or PNG
                                                    )

                                                    capturedFileInBytes =
                                                        Base64.encode(compressedBytes)
                                                    capturedFile = it
                                                }
                                            }

                                        } else {
                                            cameraPermission.launchPermissionRequest()
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colors.primaryContainer,
                                        contentColor = colors.onPrimaryContainer
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(
                                        defaultElevation = 2.dp, pressedElevation = 6.dp
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoCamera,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Capture Image",
                                        style = typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }


                                capturedFile?.let { file ->
                                    Surface(
                                        modifier = Modifier.fillMaxWidth().shadow(
                                            elevation = 8.dp, shape = RoundedCornerShape(16.dp)
                                        ),
                                        shape = RoundedCornerShape(16.dp),

                                        ) {
                                        Box(
                                            modifier = Modifier.padding(8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AsyncImage(
                                                model = file.path,
                                                contentDescription = "Captured image",
                                                modifier = Modifier.wrapContentWidth()
                                                    .wrapContentHeight().padding(8.dp)
                                                    .clip(RoundedCornerShape(12.dp)),
                                                contentScale = ContentScale.Fit
                                            )

                                        }
                                    }
                                }
                                var showErrorLocation by remember { mutableStateOf(false) }
                                val nav = LocalNavigator.currentOrThrow
                                if (showErrorLocation) {
                                    TallyResultDialog(
                                        message = "Address not found. Please try again \n Check Internet Connection",
                                        onDone = {
                                            showErrorLocation = false
                                            nav.pop()
                                        },
                                        isSuccess = false,
                                        confirmText = "Retry"
                                    )
                                }
                                TallyButton(
                                    label = buttonName,
                                    onClick = {

                                        if (address == "Address not found") {
                                            showErrorLocation = true
                                        } else {
                                            if (isAttendance) {
                                                println(capturedFileInBytes)
                                                //ATTENDANCE
                                                viewModel.sendAttendance(
                                                    attendanceRequest = AttendanceRequest(
                                                        LoginID = "Admin",
                                                        TranType = 1,
                                                        RecType = if (isCheckIn(lastAttendanceDate)) 1 else 2,
                                                        C2 = lat.toString(),
                                                        C3 = lon.toString(),
                                                        C4 = address,
                                                        C5 = capturedFileInBytes
                                                    ), onSuccess = {
                                                        if (isCheckIn(lastAttendanceDate)) {
                                                            SharedPrefs.AttendanceDate.save(
                                                                CurrentDate()
                                                            )
                                                        } else {
                                                            SharedPrefs.AttendanceDate.clear()
                                                        }
                                                        showAlert = true

                                                    })
                                            } else {
                                                // CHECK IN CHECK OUT
                                                viewModel.sendAttendance(
                                                    attendanceRequest = AttendanceRequest(
                                                        LoginID = "Admin",
                                                        TranType = 2,
                                                        RecType = if (isCheckIn(lastCheckInOutDate)) 1 else 2,
                                                        C1 = selectedAccount,
                                                        C2 = lat.toString(),
                                                        C3 = lon.toString(),
                                                        C4 = address,
                                                        C5 = capturedFileInBytes
                                                    ), onSuccess = {
                                                        if (userRole() != ROLE.OFFICE_STAFF) {
                                                            if (isCheckIn(lastCheckInOutDate)) {
                                                                SharedPrefs.CheckInOutDate.save(
                                                                    CurrentDate()
                                                                )
                                                                lastCheckInOutDate = CurrentDate()

                                                                SharedPrefs.CheckInOutLedger.save(
                                                                    selectedAccount
                                                                )
                                                            } else {
                                                                SharedPrefs.CheckInOutDate.clear()
                                                                lastCheckInOutDate = null
                                                                SharedPrefs.CheckInOutLedger.clear()
                                                            }
                                                        }
                                                        showAlert = true
                                                    })
                                            }

                                        }


                                    },
                                    backgroundColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                    enabled = if (isAttendance) {
                                        capturedFile != null
                                    } else {
                                        !selectedAccount.isEmpty() && capturedFile != null
                                    }
                                )
                            }
                        }

                    }
                }

            },

            )

        if (showAlert) {
            TallyResultDialog(
                state.message ?: "Error",
                onDone = {
                    if (!state.success) {
                        showAlert = false
                        return@TallyResultDialog
                    }

                    // Always clear image
                    capturedFile = null
                    capturedFileInBytes = ""
                    showAlert = false

                    if (userRole() == ROLE.OFFICE_STAFF) {
                        selectedAccount = ""
                        selectedGUID = ""
                        return@TallyResultDialog
                    }

                    // ATTENDANCE: always pop
                    if (isAttendance) {
                        nav.pop()
                        return@TallyResultDialog
                    }

                    // CHECK IN / CHECK OUT:
                    // Pop only if it was CHECK OUT
                    val wasCheckOut = isCheckIn(lastCheckInOutDate)

                    if (wasCheckOut) {
                        nav.pop()
                    }
                },
                isSuccess = state.success
            )
        }
    }
}

@Composable
private fun ElegantCard(
    icon: ImageVector,
    title: String,
    iconTint: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Card(
        modifier = modifier.fillMaxWidth().shadow(
            elevation = 4.dp,
            shape = RoundedCornerShape(20.dp),
            ambientColor = iconTint.copy(alpha = 0.1f),
            spotColor = iconTint.copy(alpha = 0.1f)
        ), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(
            containerColor = colors.surface
        ), elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header with icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = iconTint.copy(alpha = 0.12f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Text(
                    title,
                    style = typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
            }

            HorizontalDivider(
                color = colors.outlineVariant.copy(alpha = 0.3f), thickness = 1.dp
            )

            content()
        }
    }
}


suspend fun getPlaceFromCoordinates(lat: Double, lon: Double): Place? {
    return Geocoder.mobile().placeOrNull(lat, lon)
}

fun formatAddress(a: Place): String {
    fun clean(v: String?) = v?.takeIf { it.isNotBlank() }

    val parts = listOfNotNull(
        clean(a.name),
        clean("${a.subThoroughfare.orEmpty()} ${a.thoroughfare.orEmpty()}".trim()),
        clean("${a.subLocality.orEmpty()} ${a.locality.orEmpty()}".trim()),
        clean("${a.subAdministrativeArea.orEmpty()} ${a.administrativeArea.orEmpty()}".trim()),
        clean(a.postalCode),
        clean(a.country),
        clean(a.isoCountryCode?.let { "($it)" })
    )

    return parts.joinToString("\n")
}


fun isCheckIn(spDate: String?): Boolean {
    return spDate.isNullOrBlank() || spDate != CurrentDate()
}

@Composable
private fun ReportButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surfaceVariant.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )

                Text(
                    text = label,
                    style = typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = colors.onSurface
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}