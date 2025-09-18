package org.prime.tally.ui.screen.home.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions

object MastersTab : Tab {
    override val options: TabOptions
        @Composable get() {
            val icon = rememberVectorPainter(Icons.Default.ManageAccounts)
            return TabOptions(index = 0u, title = "Masters", icon = icon)
        }

    @Composable
    override fun Content() {
        val colors = MaterialTheme.colorScheme
        Column(modifier = Modifier.fillMaxSize().background(colors.background)) {
        }

    }
}

