package org.prime.easykarobar.ui.screen.home.tabs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions

object CustomerSupportTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = "Support"
            val icon = rememberVectorPainter(Icons.Default.SupportAgent)

            return remember {
                TabOptions(
                    index = 4u,
                    title = title,
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        // This tab is intercepted in Dashboard.kt to open WhatsApp
    }
}
