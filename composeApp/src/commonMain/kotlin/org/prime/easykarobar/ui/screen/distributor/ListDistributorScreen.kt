package org.prime.easykarobar.ui.screen.distributor

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.prime.easykarobar.business.viewmodel.DistributorViewModel
import org.prime.easykarobar.data.model.DistributorRequest
import org.prime.easykarobar.ui.shared.composables.TallyAlertBox
import org.prime.easykarobar.ui.shared.composables.TallyCircularLoader
import org.prime.easykarobar.ui.shared.composables.TallyResultDialog
import org.prime.easykarobar.ui.shared.composables.TallyScaffold
import org.prime.easykarobar.ui.shared.composables.TallyTextField

object ListDistributorScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel: DistributorViewModel = viewModel { DistributorViewModel() }
        val state by viewModel.listState

        LaunchedEffect(Unit) {
            viewModel.listDistributor()
        }

        TallyScaffold(
            title = "Distributors",
            showBottomBar = false,
            bottomBarContent = {},
            content = { paddingValues ->
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

                    state.data.isNullOrEmpty() -> {
                        EmptyState(modifier = Modifier.padding(paddingValues))
                    }

                    else -> {
                        DistributorList(
                            distributors = state.data!!,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                            viewModel = viewModel
                        )
                    }
                }
            })
    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No distributors found",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Add distributors to see them here",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DistributorList(
    distributors: List<DistributorRequest>,
    modifier: Modifier = Modifier,
    viewModel: DistributorViewModel
) {
    var showInactiveDialog by remember { mutableStateOf(false) }
    var pass by remember { mutableStateOf("") }
    var selectedDistributor by remember { mutableStateOf<DistributorRequest?>(null) }

    val buttons = listOf("All", "Active", "Inactive")
    var selected by remember { mutableStateOf(0) }

    val nav = LocalNavigator.currentOrThrow
    val state by viewModel.updateState

    val interactionSources = remember {
        List(buttons.size) { MutableInteractionSource() }
    }

    // FIX: compute filtered list OUTSIDE LazyColumn
    val filteredList = remember(selected, distributors) {
        when (selected) {
            0 -> distributors
            1 -> distributors.filter { it.status.equals("active", ignoreCase = true) }
            2 -> distributors.filter { it.status.equals("inactive", ignoreCase = true) }
            else -> distributors
        }
    }

    // --- UI ---
    Column(modifier = modifier.fillMaxSize()) {

        // EXPRESSIVE BUTTON GROUP WITH CORRECT SHAPES
        ButtonGroup(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            expandedRatio = ButtonGroupDefaults.ExpandedRatio
        ) {
            buttons.forEachIndexed { index, label ->


                ToggleButton(
                    checked = selected == index,
                    onCheckedChange = { selected = index },
                    modifier = Modifier
                        .weight(1f)
                        .animateWidth(interactionSources[index]),
                    interactionSource = interactionSources[index],
                ) {
                    Text(label)
                }
            }
        }

        // LIST
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredList) { distributor ->

                DistributorCard(
                    distributor = distributor,
                    onEdit = {
                        nav.push(CreateDistributorScreen(isEdit = true, distributor))
                    },
                    onDelete = {
                        selectedDistributor = distributor
                        showInactiveDialog = true
                    }
                )
            }
        }
    }

    // --- LOADER ---
    if (state.isLoading) {
        TallyCircularLoader()
    }

    // --- SUCCESS / ERROR RESULT ---
    if (state.success || state.error != null) {
        TallyResultDialog(
            message = state.message ?: "Error",
            onDone = {
                // CLOSE ONLY THIS DIALOG

            },
            isSuccess = state.success,
            confirmText = "Okay"
        )
    }

    // --- INACTIVE CONFIRM DIALOG ---
    if (showInactiveDialog) {
        TallyAlertBox(
            title = "Deactivate Distributor",
            message = "Are you sure you want to deactivate this distributor?",
            confirmButtonText = "Yes",
            cancelButtonText = "No",
            onConfirm = {
                val dist = selectedDistributor ?: return@TallyAlertBox

                // BASIC CHECK
                if (pass.isBlank()) return@TallyAlertBox

                viewModel.updateDistributor(dist.copy(status = "inactive", password = pass)) {
                    showInactiveDialog = false
                    pass = "" // clear password
                }
            },
            onCancel = {
                showInactiveDialog = false
                pass = ""
            },
            onDismiss = {
                showInactiveDialog = false
                pass = ""
            },
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Verify Password", style = MaterialTheme.typography.labelMedium)

                    TallyTextField(
                        value = pass,
                        onValueChange = { pass = it },
                        isPassword = true,
                        placeholder = "Enter your password",
                        label = "Password", isNumber = false
                    )
                }
            }
        )
    }
}

@Composable
fun DistributorCard(
    distributor: DistributorRequest,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isActive = distributor.status?.equals("active", ignoreCase = true) == true

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Avatar with initial
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (isActive)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = distributor.distributor_name.firstOrNull()?.uppercase() ?: "D",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isActive)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                // Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Name and status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = distributor.distributor_name,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        StatusChip(isActive)
                    }

                    // Info rows
                    InfoRowCompact(
                        icon = Icons.Outlined.Phone,
                        value = distributor.mobile_no
                    )
                    InfoRowCompact(
                        icon = Icons.Outlined.AccountBalance,
                        value = distributor.ledger_name
                    )
                }
            }

            // Action buttons
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Edit button
                TextButton(
                    onClick = onEdit,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Edit",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Delete button
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Delete",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun StatusChip(isActive: Boolean) {
    val backgroundColor = if (isActive) Color(0xFF10B981) else Color(0xFFEF4444)
    val contentColor = Color.White

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = backgroundColor,
        modifier = Modifier.height(22.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isActive) "Active" else "Inactive",
                color = contentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun InfoRowCompact(icon: ImageVector, value: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Normal
        )
    }
}