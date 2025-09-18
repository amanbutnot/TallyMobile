package org.prime.tally.ui.screen.home.tabs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Report
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions

object ReportingTab: Tab {
    override val options: TabOptions
        @Composable get() {
            val icon = rememberVectorPainter(Icons.Default.Assessment)
            return TabOptions(index = 2u, title = "Reporting", icon = icon)
        }

    @Composable
    override fun Content() {
        Text("Reporting tab", color = MaterialTheme.colorScheme.onBackground)

    }
}