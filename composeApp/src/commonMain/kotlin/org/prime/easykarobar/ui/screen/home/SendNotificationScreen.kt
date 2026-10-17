package org.prime.easykarobar.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.path
import io.github.vinceglb.filekit.readBytes
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.prime.easykarobar.TallyDatabase
import org.prime.easykarobar.business.repository.AuthRepository
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.model.NotificationPayload
import org.prime.easykarobar.data.model.OnClickAction
import org.prime.easykarobar.data.utils.KtorClient
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.ui.shared.composables.TallyScaffold
import org.prime.easykarobar.ui.shared.globalShared.getNotificationImage
import org.tally.GetProductsForDis
import kotlin.time.Clock

data class EntityItem(val name: String?, val guid: String)

class SendNotificationScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        val colors = MaterialTheme.colorScheme
        val db = DatabaseHolder.instance
        val scope = rememberCoroutineScope()

        var title by remember { mutableStateOf("") }
        var contentType by remember { mutableStateOf("desc") } // "desc" or "image"
        var description by remember { mutableStateOf("") }
        var imageBytes by remember { mutableStateOf<ByteArray?>(null) }
        var imageUriString by remember { mutableStateOf<String?>(null) }

        var onClickAction by remember { mutableStateOf(OnClickAction.NOTHING) }
        var selectedEntityName by remember { mutableStateOf("") }
        var selectedEntityGuid by remember { mutableStateOf("") }

        var showSelectionDialog by remember { mutableStateOf(false) }
        var showConfirmDialog by remember { mutableStateOf(false) }
        var showSuccessDialog by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        val actionOptions = OnClickAction.entries

        val galleryLauncher = rememberFilePickerLauncher(
            type = FileKitType.Image,
            mode = FileKitMode.Single,
            title = "Select Image"
        ) { file: PlatformFile? ->
            if (file != null) {
                scope.launch {
                    try {
                        imageBytes = file.readBytes()
                        imageUriString = file.path
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        TallyScaffold(
            title = "Send Notification",
            onBack = { nav.pop() },
            showEditIcon = false
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.surfaceContainerLowest)
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Notification Title") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Content Type Selection: Desc vs Image
                    Text("Content Type", style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilterChip(
                            selected = contentType == "desc",
                            onClick = { contentType = "desc" },
                            label = { Text("Description") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = contentType == "image",
                            onClick = { contentType = "image" },
                            label = { Text("Image") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (contentType == "desc") {
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Notification Description") },
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 5
                        )
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (imageUriString != null) {
                                    AsyncImage(
                                        model = imageUriString,
                                        contentDescription = "Selected notification image",
                                        modifier = Modifier
                                            .size(160.dp)
                                            .clip(RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text(
                                        text = "No image selected",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colors.onSurfaceVariant
                                    )
                                }
                                Button(
                                    onClick = { galleryLauncher.launch() },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Image, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Choose Image from Gallery")
                                }
                            }
                        }
                    }

                    // OnClick Action Selection
                    Text("OnClick Action", style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
                    
                    var expandedActionDropdown by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expandedActionDropdown,
                        onExpandedChange = { expandedActionDropdown = !expandedActionDropdown },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = onClickAction.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Action Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedActionDropdown) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedActionDropdown,
                            onDismissRequest = { expandedActionDropdown = false }
                        ) {
                            actionOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.displayName) },
                                    onClick = {
                                        onClickAction = option
                                        expandedActionDropdown = false
                                        selectedEntityName = ""
                                        selectedEntityGuid = ""
                                    }
                                )
                            }
                        }
                    }

                    // If action is not nothing, show selection picker for item/category/brand/subcategory
                    if (onClickAction != OnClickAction.NOTHING) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showSelectionDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Selected ${onClickAction.displayName.replace("Open ", "")}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = colors.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = selectedEntityName.ifBlank { "Tap to select ${onClickAction.displayName.replace("Open ", "").lowercase()}" },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (selectedEntityName.isBlank()) colors.onSurfaceVariant else colors.onSurface,
                                        fontWeight = if (selectedEntityName.isBlank()) FontWeight.Normal else FontWeight.Bold
                                    )
                                }
                                TextButton(onClick = { showSelectionDialog = true }) {
                                    Text("Change")
                                }
                            }
                        }
                    }

                    if (errorMessage != null) {
                        Text(text = errorMessage!!, color = colors.error, style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                errorMessage = "Please enter notification title"
                                return@Button
                            }
                            if (contentType == "desc" && description.isBlank()) {
                                errorMessage = "Please enter notification description"
                                return@Button
                            }
                            if (contentType == "image" && imageBytes == null) {
                                errorMessage = "Please select an image"
                                return@Button
                            }
                            if (onClickAction != OnClickAction.NOTHING && selectedEntityGuid.isBlank()) {
                                errorMessage = "Please select an item/category/brand/subcategory for the action"
                                return@Button
                            }

                            errorMessage = null
                            showConfirmDialog = true
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Send Notification", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            if (showConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showConfirmDialog = false },
                    title = { Text("Confirm Notification") },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Do you want to confirm? This won't be deleted.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.onSurface
                            )
                            Text(
                                text = "Notification Preview:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary
                            )
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.onSurface
                                    )
                                    if (contentType == "desc") {
                                        Text(
                                            text = description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colors.onSurfaceVariant
                                        )
                                    } else {
                                        if (imageUriString != null) {
                                            AsyncImage(
                                                model = imageUriString,
                                                contentDescription = "Preview image",
                                                modifier = Modifier.size(100.dp).clip(RoundedCornerShape(6.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Text(
                                                text = "[Image Attached]",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = colors.primary,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                    if (onClickAction != OnClickAction.NOTHING) {
                                        Text(
                                            text = "Action: ${onClickAction.displayName}" + if (selectedEntityName.isNotBlank()) " ($selectedEntityName)" else "",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = colors.secondary
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                scope.launch {
                                    var imageUrl: String? = null
                                    val storeId = SharedPrefs.User.get()?.ID.toString()
                                    if (contentType == "image" && imageBytes != null) {
                                        val sanitizedTitle = title.trim().replace(" ", "_")
                                        val success = uploadNotificationImageToServer(imageBytes!!, sanitizedTitle, "Notification", storeId)
                                        if (success) {
                                            imageUrl = getNotificationImage(storeId, title)
                                        }
                                    }

                                    val payload = NotificationPayload(
                                        title = title,
                                        contentType = contentType,
                                        description = if (contentType == "desc") description else null,
                                        hasImage = contentType == "image" && imageUrl != null,
                                        imageUrl = imageUrl,
                                        onClickAction = onClickAction.value,
                                        entityGuid = if (onClickAction != OnClickAction.NOTHING) selectedEntityGuid else null,
                                        entityName = if (onClickAction != OnClickAction.NOTHING) selectedEntityName else null,
                                        timestamp = Clock.System.now().toEpochMilliseconds()
                                    )
                                    val jsonString = Json { prettyPrint = true }.encodeToString(payload)
                                    println("NOTIFICATION_JSON: $jsonString")

                                    // Call Push API
                                    AuthRepository.saveNotification(payload)

                                    db.notificationHistoryQueries.insertNotification(
                                        title = title,
                                        description = jsonString,
                                        receivedAt = payload.timestamp
                                    )
                                    showConfirmDialog = false
                                    showSuccessDialog = true
                                }
                            }
                        ) {
                            Text("Confirm")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showConfirmDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            if (showSelectionDialog) {
                ModalBottomSheet(
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                    onDismissRequest = { showSelectionDialog = false }
                ) {
                    SelectionBottomSheetContent(
                        action = onClickAction,
                        db = db,
                        onDismiss = { showSelectionDialog = false },
                        onSelect = { name, guid ->
                            selectedEntityName = name
                            selectedEntityGuid = guid
                        }
                    )
                }
            }

            if (showSuccessDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showSuccessDialog = false
                        nav.pop()
                    },
                    title = { Text("Success") },
                    text = { Text("Notification sent and saved successfully!") },
                    confirmButton = {
                        Button(onClick = {
                            showSuccessDialog = false
                            nav.pop()
                        }) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }

    @Composable
    private fun SelectionBottomSheetContent(
        action: OnClickAction,
        db: TallyDatabase,
        onDismiss: () -> Unit,
        onSelect: (name: String, guid: String) -> Unit
    ) {
        var query by remember { mutableStateOf("") }
        val colors = MaterialTheme.colorScheme

        val itemsList: List<EntityItem> = remember(action) {
            when (action) {
                OnClickAction.OPEN_ITEM -> {
                    db.productsQueries.getProductsForDis(
                        filterGroup = 0L,
                        groupCodes = emptyList(),
                        productCode = null,
                        changePrice = SharedPrefs.ChangePrice.get(),
                        mapper = ::GetProductsForDis
                    ).executeAsList().map { EntityItem(it.product_name, it.product_id ?: "") }
                }
                OnClickAction.OPEN_ITEM_CATEGORY -> {
                    db.productsQueries.productCategoriesForDis(
                        filterGroup = 0L,
                        groupCodes = emptyList()
                    ).executeAsList().map { EntityItem(it.Name, it.GUID ?: "") }
                }
                OnClickAction.OPEN_BRAND -> {
                    db.productsQueries.getAllBrands().executeAsList().map { EntityItem(it.Name, it.Code?.toString() ?: "") }
                }
                OnClickAction.OPEN_ITEM_SUBCATEGORY -> {
                    db.product_CategoryQueries.getSubCategory(null).executeAsList().map { EntityItem(it.CatName, it.CatCode?.toString() ?: "") }
                }
                OnClickAction.NOTHING -> emptyList()
            }
        }

        val filteredList = remember(query, itemsList) {
            if (query.isBlank()) itemsList
            else itemsList.filter { it.name?.contains(query, ignoreCase = true) == true }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .padding(20.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = "Select ${action.displayName.replace("Open ", "")}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Search") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filteredList) { item ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelect(item.name ?: "", item.guid)
                                onDismiss()
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp)
                    ) {
                        Text(
                            text = item.name ?: "Unknown",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}

suspend fun uploadNotificationImageToServer(fileBytes: ByteArray, id: String, folderName: String, storeId: String): Boolean {
    return try {
        val fileName = "$id.jpg"
        val response = KtorClient.client.submitFormWithBinaryData(
            url = "https://easykarobar.in/Tokens/upload-base64-r2.php",
            formData = formData {
                append("folderName", folderName)
                append("fileName", fileName)
                append("storeId", storeId)
                append("image", fileBytes, Headers.build {
                    append(HttpHeaders.ContentType, "image/jpeg")
                    append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                })
            }
        )
        println("notification image upload response: ${response.bodyAsText()}")
        response.status.value in 200..299
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}
