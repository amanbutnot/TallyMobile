package org.prime.easykarobar.ui.screen.reports.pendingOrder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AssignmentLate
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.prime.easykarobar.data.model.salesmanPermission
import org.prime.easykarobar.ui.screen.home.tabs.ReportButton
import org.prime.easykarobar.ui.screen.reports.outstanding.OutstandingFilterScreen
import org.prime.easykarobar.ui.shared.composables.PermissionDeniedDialog
import org.prime.easykarobar.ui.shared.composables.TallyScaffold

object OrderReportSelectScreen : Screen {
    @Composable
    override fun Content() {
        val colors = MaterialTheme.colorScheme
        val nav = LocalNavigator.currentOrThrow
        var showDeniedDialog by remember { mutableStateOf(false) }

        if (showDeniedDialog) {
            PermissionDeniedDialog { showDeniedDialog = false }
        }
        TallyScaffold(
            "Pending Report Select",
            onBack = { nav.pop() },
            showEditIcon = false,
            onEditClick = {}
        ) { paddingValues ->
            Column(
                modifier = Modifier.fillMaxSize().background(colors.background)
                    .padding(paddingValues)
            ) {
                Text(
                    text = "Account wise",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Medium,
                    color = colors.onBackground,
                    modifier = Modifier.padding(20.dp).padding(bottom = 8.dp),
                )


                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                )
                {
                    ReportButton(
                        icon = Icons.Default.AccountBalance,
                        modifier = Modifier.weight(1f),
                        title = "Pending Sale Order",
                        onClick = {
                            salesmanPermission(
                                "D38",
                                accessDeniedBlock = { showDeniedDialog = true },
                                successBlock = {  nav.push(
                                    OutstandingFilterScreen("Pending Sale Order")
                                ) }
                            )

//                            salesmanPermission(
//                                "D8",
//                                accessDeniedBlock = { showDeniedDialog = true },
//                                successBlock = {
//
//                                }
//                            )

                        }
                    )
                    ReportButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.AssignmentLate,
                        title = "Pending Purchase Order",
                        onClick = {
                            salesmanPermission(
                                "D39",
                                accessDeniedBlock = { showDeniedDialog = true },
                                successBlock = {  nav.push(
                                    OutstandingFilterScreen("Pending Purchase Order")
                                ) }
                            )
//                            salesmanPermission(
//                                "D9",
//                                accessDeniedBlock = { showDeniedDialog = true },
//                                successBlock = {
//
//                                }
//                            )

                        }
                    )


                }
            }
        }
    }
}