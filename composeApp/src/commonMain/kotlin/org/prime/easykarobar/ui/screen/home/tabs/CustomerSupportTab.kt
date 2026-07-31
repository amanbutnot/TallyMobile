package org.prime.easykarobar.ui.screen.home.tabs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions

object WhatsAppSupportTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = "WhatsApp"
            val icon = rememberVectorPainter(Icons.Default.SupportAgent)

            return remember {
                TabOptions(
                    index = 5u,
                    title = title,
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        // Intercepted in Dashboard.kt
    }
}

object CallSupportTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = "Call"
            val icon = rememberVectorPainter(Icons.Default.Call)

            return remember {
                TabOptions(
                    index = 6u,
                    title = title,
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        // Intercepted in Dashboard.kt
    }
}
