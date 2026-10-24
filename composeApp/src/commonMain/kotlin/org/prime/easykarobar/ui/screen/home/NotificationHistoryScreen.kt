package org.prime.easykarobar.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import org.prime.easykarobar.business.repository.AuthRepository
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.model.NotificationPayload
import org.prime.easykarobar.data.model.OnClickAction
import org.prime.easykarobar.ui.shared.composables.TallyScaffold

object NotificationHistoryScreen : Screen {
    private val json = Json { ignoreUnknownKeys = true }

    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        val colors = MaterialTheme.colorScheme
        val db = DatabaseHolder.instance
        var payloads by remember { mutableStateOf(emptyList<NotificationPayload>()) }
        var isLoading by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            isLoading = true
            val res = AuthRepository.listNotifications()
            if (res?.statuscode == 200 && res.data != null) {
                payloads = res.data.sortedByDescending { it.timestamp }
            } else {
                // Fallback to local database if network fails
                val local = db.notificationHistoryQueries.getAllNotifications().executeAsList()
                payloads = local.map { loc ->
                    runCatching {
                        json.decodeFromString<NotificationPayload>(loc.description)
                    }.getOrElse {
                        NotificationPayload(
                            title = loc.title,
                            contentType = "desc",
                            description = loc.description,
                            hasImage = false,
                            imageUrl = null,
                            onClickAction = "nothing",
                            entityGuid = null,
                            entityName = null,
                            timestamp = loc.receivedAt
                        )
                    }
                }.sortedByDescending { it.timestamp }
            }
            isLoading = false
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
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colors.primary)
                    }
                } else if (payloads.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(24.dp),
                                color = colors.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(72.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = null,
                                        tint = colors.onSurfaceVariant,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                            Text(
                                text = "No notifications found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.onSurface
                            )
                            Text(
                                text = "Previous notifications will appear here",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(payloads) { payload ->
                            NotificationPayloadItem(payload)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun NotificationPayloadItem(payload: NotificationPayload) {
        val colors = MaterialTheme.colorScheme
        
        val instant = Instant.fromEpochMilliseconds(payload.timestamp)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val dateString = "${localDateTime.date} ${localDateTime.hour}:${localDateTime.minute.toString().padStart(2, '0')}"
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = colors.primary.copy(alpha = 0.12f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notification",
                                    tint = colors.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Text(
                            text = payload.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.onSurface
                        )
                    }
                    Text(
                        text = dateString,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                if (payload.contentType == "desc") {
                    if (!payload.description.isNullOrBlank()) {
                        Text(
                            text = payload.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant
                        )
                    }
                } else {
                    if (!payload.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = payload.imageUrl,
                            contentDescription = "Notification image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                val actionEnum = OnClickAction.fromValue(payload.onClickAction)
                if (actionEnum != OnClickAction.NOTHING) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = colors.secondaryContainer.copy(alpha = 0.6f),
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Action: ${actionEnum.displayName}" + if (!payload.entityName.isNullOrBlank()) " • ${payload.entityName}" else "",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}
