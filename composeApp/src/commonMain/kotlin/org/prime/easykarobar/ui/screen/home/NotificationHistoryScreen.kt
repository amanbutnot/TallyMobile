package org.prime.easykarobar.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.ui.shared.composables.TallyScaffold
import org.tally.NotificationHistory

object NotificationHistoryScreen : Screen {
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        val colors = MaterialTheme.colorScheme
        val db = DatabaseHolder.instance
        var notifications by remember { mutableStateOf(emptyList<NotificationHistory>()) }

        LaunchedEffect(Unit) {
            notifications = db.notificationHistoryQueries.getAllNotifications().executeAsList()
        }

        TallyScaffold(
            title = "Notification History",
            onBack = { nav.pop() },
            showEditIcon = false
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.surfaceContainerLowest)
                    .padding(paddingValues)
            ) {
                if (notifications.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "No notifications found", color = colors.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(notifications) { notification ->
                            NotificationItem(notification) {
                                db.notificationHistoryQueries.markAsClicked(notification.id)
                                notifications = db.notificationHistoryQueries.getAllNotifications().executeAsList()
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun NotificationItem(notification: NotificationHistory, onClick: () -> Unit) {
        val colors = MaterialTheme.colorScheme
        val isClicked = notification.isClicked > 0
        
        val instant = Instant.fromEpochMilliseconds(notification.receivedAt)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val dateString = "${localDateTime.date} ${localDateTime.hour}:${localDateTime.minute.toString().padStart(2, '0')}"
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isClicked) colors.surfaceVariant.copy(alpha = 0.5f) else colors.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = colors.primary.copy(alpha = 0.1f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notification",
                            tint = colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = if (isClicked) FontWeight.Normal else FontWeight.Bold
                        ),
                        color = colors.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = notification.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = dateString,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
