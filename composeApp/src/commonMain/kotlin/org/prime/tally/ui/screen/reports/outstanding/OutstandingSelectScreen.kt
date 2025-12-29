package org.prime.tally.ui.screen.reports.outstanding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AssignmentLate
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.prime.tally.data.model.SalesmanPermission
import org.prime.tally.ui.screen.home.tabs.ReportButton
import org.prime.tally.ui.shared.composables.TallyScaffold

object OutstandingSelectScreen : Screen {
    @Composable
    override fun Content() {
        val colors = MaterialTheme.colorScheme
        val nav = LocalNavigator.currentOrThrow
        TallyScaffold(
            "Outstanding Select",
            onBack = { nav.pop() },
            showEditIcon = false,
            onEditClick = {}
        ) { paddingValues ->
            Column(
                modifier = Modifier.fillMaxSize().background(colors.background)
                    .padding(paddingValues)
            ) {
                Text(
                    text = "Outstanding",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Medium,
                    color = colors.onBackground,
                    modifier = Modifier.padding(20.dp).padding(bottom = 8.dp),
                )


                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item{
                        SalesmanPermission("D8"){
                            ReportButton(
                                icon = Icons.Default.AccountBalance,
                                title = "Bill Receivable",
                                onClick = {
                                    nav.push(
                                        OutstandingFilterScreen("Bill Receivable")
                                    )
                                }
                            )
                        }
                    }
                    item{
                        SalesmanPermission("D9"){
                            ReportButton(
                                icon = Icons.Default.AssignmentLate,
                                title = "Bill Payable",
                                onClick = {
                                    nav.push(
                                        OutstandingFilterScreen("Bill Payable")
                                    )
                                }
                            )
                        }
                    }

                }

            }
        }


    }}