package org.prime.tally.ui.screen.home.tabs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions

object TransactionTab : Tab {
    override val options: TabOptions
        @Composable get() {
            val icon = rememberVectorPainter(Icons.AutoMirrored.Default.ReceiptLong)
            return TabOptions(index = 1u, title = "Transaction", icon = icon)
        }

    @Composable
    override fun Content() {
        Text("Transaction tab", color = MaterialTheme.colorScheme.onBackground)

    }
}