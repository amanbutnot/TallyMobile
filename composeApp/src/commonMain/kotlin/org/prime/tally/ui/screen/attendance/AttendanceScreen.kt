package org.prime.tally.ui.screen.attendance

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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
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
import dev.jordond.compass.Place
import dev.jordond.compass.geocoder.Geocoder
import dev.jordond.compass.geocoder.mobile
import dev.jordond.compass.geocoder.placeOrNull
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitCameraFacing
import io.github.vinceglb.filekit.dialogs.FileKitCameraType
import io.github.vinceglb.filekit.dialogs.openCameraPicker
import io.github.vinceglb.filekit.path
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.launch
import org.prime.tally.business.viewmodel.attendance.AttendanceViewModel
import org.prime.tally.data.expect.DatabaseHolder
import org.prime.tally.data.model.attendance.AttendanceRequest
import org.prime.tally.data.utils.SharedPrefs
import org.prime.tally.ui.screen.transactions.SelectLedgerRow
import org.prime.tally.ui.screen.transactions.TransactionBottomSheet
import org.prime.tally.ui.shared.composables.TallyAlertBox
import org.prime.tally.ui.shared.composables.TallyButton
import org.prime.tally.ui.shared.composables.TallyCircularLoader
import org.prime.tally.ui.shared.composables.TallyLoadingDialog
import org.prime.tally.ui.shared.composables.TallyResultDialog
import org.prime.tally.ui.shared.composables.TallyScaffold
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

data class AttendanceScreen(
    val lat: Double,
    val lon: Double,
    val address: String,
    val isAttendance: Boolean
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalEncodingApi::class)
    @Composable
    override fun Content() {


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

        LaunchedEffect(Unit) {
            selectedAccount = SharedPrefs.AttendanceLedger.get() ?: ""
        }
        var showBottomSheet by rememberSaveable { mutableStateOf(false) }

        val db = DatabaseHolder.instance
        val list = db.ledgerMasterQueries.selectAll().executeAsList()

        val spDate = SharedPrefs.AttendanceDate.get()
        if (state.isLoading) {
            TallyLoadingDialog("Please Wait")
        }


        TallyScaffold(
            title = if (isAttendance) "Attendance" else "Check In/Check Out",
            onBack = { nav.pop() },
            content = { paddingValues ->

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    colors.surface,
                                    colors.surfaceVariant.copy(alpha = 0.3f)
                                )
                            )
                        )
                        .verticalScroll(scrollState)
                        .padding(paddingValues)
                        .padding(horizontal = 8.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {


                    if (!isAttendance) {
                        SelectLedgerRow(
                            selectedAccount = selectedAccount,
                            onShowBottomSheet = { showBottomSheet = true },
                            title = "Ledger",
                        )
                    }

                    TransactionBottomSheet(
                        showBottomSheet = showBottomSheet,
                        list = list.map { Pair(it.Name ?: "", it.GUID ?: "") },
                        onSelected = {
                            it.let {
                                selectedAccount = it.first
                                println("Selected Account: ${it.first} and selected GUID is ${it.second}")
                            }
                        },
                        onDismiss = { showBottomSheet = false },
                        bottomSheetState = rememberModalBottomSheetState(),
                        title = "Ledger Name",
                    )

                    ElegantCard(
                        icon = Icons.Default.LocationOn,
                        title = "Location Details",
                        iconTint = colors.primary, modifier = Modifier
                    )
                    {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                address,
                                style = typography.bodyMedium,
                                color = colors.onSurface,
                                lineHeight = typography.bodyMedium.lineHeight
                            )
                        }
                    }



                    ElegantCard(
                        icon = Icons.Default.PhotoCamera,
                        title = "Camera Capture",
                        iconTint = colors.secondary, modifier = Modifier
                    )
                    {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Button(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                onClick = {
                                    scope.launch {
                                        val file = FileKit.openCameraPicker(
                                            type = FileKitCameraType.Photo,
                                            cameraFacing = FileKitCameraFacing.Back
                                        )
                                        file?.let {
                                            val bytes = it.readBytes()
                                            capturedFileInBytes = Base64.encode(bytes)
                                            capturedFile = it
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.primaryContainer,
                                    contentColor = colors.onPrimaryContainer
                                ),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 2.dp,
                                    pressedElevation = 6.dp
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
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .shadow(
                                            elevation = 8.dp,
                                            shape = RoundedCornerShape(16.dp)
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
                                            modifier = Modifier
                                                .wrapContentWidth()
                                                .wrapContentHeight()
                                                .padding(8.dp)
                                                .clip(RoundedCornerShape(12.dp)),
                                            contentScale = ContentScale.Fit
                                        )

                                    }
                                }
                            }

                            TallyButton(
                                label = if (isCheckIn(spDate)) "Check In" else "Check Out",
                                onClick = {

                                    if (isAttendance) {
                                        viewModel.sendAttendance(
                                            attendanceRequest = AttendanceRequest(
                                                LoginID = "Admin",
                                                TranType = 1,
                                                RecType = if (isCheckIn(spDate)) 1 else 2,
                                                C2 = lat.toString(),
                                                C3 = lon.toString(),
                                                C4 = address,
                                                C5 = capturedFileInBytes
                                            ),
                                            onSuccess = {
                                                if (isCheckIn(spDate)) {
                                                    SharedPrefs.AttendanceDate.save(CurrentDate())
                                                } else {
                                                    SharedPrefs.AttendanceDate.clear()
                                                }
                                                showAlert = true

                                            }
                                        )
                                    } else {
                                        viewModel.sendAttendance(
                                            attendanceRequest = AttendanceRequest(
                                                LoginID = "Admin",
                                                TranType = 2,
                                                RecType = if (isCheckIn(spDate)) 1 else 2,
                                                C1 = selectedAccount,
                                                C2 = lat.toString(),
                                                C3 = lon.toString(),
                                                C4 = address,
                                                C5 = capturedFileInBytes
                                            ),
                                            onSuccess = {
                                                if (isCheckIn(spDate)) {
                                                    SharedPrefs.AttendanceDate.save(CurrentDate())
                                                    SharedPrefs.AttendanceLedger.save(
                                                        selectedAccount
                                                    )
                                                } else {
                                                    SharedPrefs.AttendanceDate.clear()
                                                    SharedPrefs.AttendanceLedger.clear()
                                                }
                                                showAlert = true
                                            }
                                        )
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
            },

            )

        if (showAlert) {
            TallyResultDialog(
                state.message ?: "Error",
                onDone = { if (state.success) nav.pop() else showAlert = false },
                isSuccess = state.success
            )
        }
    }
}

@Composable
private fun ElegantCard(
    icon: ImageVector,
    title: String,
    iconTint: Color, modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = iconTint.copy(alpha = 0.1f),
                spotColor = iconTint.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
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
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
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
                color = colors.outlineVariant.copy(alpha = 0.3f),
                thickness = 1.dp
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