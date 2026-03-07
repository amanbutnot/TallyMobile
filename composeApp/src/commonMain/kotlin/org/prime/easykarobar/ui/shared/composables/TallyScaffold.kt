package org.prime.easykarobar.ui.shared.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TallyScaffold(
    title: String,
    content: @Composable (PaddingValues) -> Unit,
    showBottomBar: Boolean = false,
    showAddBar: Boolean = false,
    onAddClick: () -> Unit = {},
    bottomBarContent: @Composable () -> Unit = {}
) {
    val nav = LocalNavigator.currentOrThrow
    val colors = MaterialTheme.colorScheme
    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                Box(modifier = Modifier.navigationBarsPadding()) {
                    bottomBarContent()
                }
            }
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Medium
                        )
                    )
                }, actions = {
                    if (showAddBar) {
                        IconButton(onClick = { onAddClick() }) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add",
                                tint = colors.onSurface
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { nav.pop() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.surface,
                    scrolledContainerColor = Color.Unspecified,
                    navigationIconContentColor = Color.Unspecified,
                    titleContentColor = colors.onSurface,
                    actionIconContentColor = Color.Unspecified
                )
            )
        }
    ) { innerPadding ->
        content(innerPadding)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TallyReportScaffold(
    title: String,
    showBottomBar: Boolean = false,
    bottomBarContent: @Composable () -> Unit = {},
    showSearchAction: Boolean = false,
    showBarcodeIcon: Boolean = false,
    showBurgerMenu: Boolean = false,
    onSearchClick: (() -> Unit)? = null,
    onBarcodeClick: (() -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
    menuItems: List<MenuItemData> = emptyList(),
    content: @Composable (PaddingValues) -> Unit
) {
    val nav = LocalNavigator.currentOrThrow
    val colors = MaterialTheme.colorScheme
    var expanded by remember { mutableStateOf(false) }


    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                Box(modifier = Modifier.navigationBarsPadding()) {
                    bottomBarContent()
                }
            }
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Medium
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { if(onBackClick==null)nav.pop() else onBackClick() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.onSurface
                        )
                    }
                },
                actions = {
                    Row {
                        if (showBarcodeIcon) {
                            IconButton(onClick = { onBarcodeClick?.invoke() }) {
                                Icon(
                                    Icons.Default.QrCodeScanner,
                                    contentDescription = "Search",
                                    tint = colors.onSurface
                                )
                            }
                        }
                        if (showSearchAction) {
                            IconButton(onClick = { onSearchClick?.invoke() }) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = colors.onSurface
                                )
                            }
                        }
                        if (showBurgerMenu) {
                            IconButton(onClick = {
                                expanded = true
                            }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Menu Icon")
                            }
                        }
                    }

                    if (expanded) {
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }) {
                            menuItems.forEach { item ->
                                DropdownMenuItem(
                                    modifier = Modifier.padding(4.dp),
                                    text = {
                                        Text(
                                            text = item.title,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    onClick = {
                                        item.onClick()
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.surface,
                    scrolledContainerColor = Color.Unspecified,
                    navigationIconContentColor = Color.Unspecified,
                    titleContentColor = colors.onSurface,
                    actionIconContentColor = Color.Unspecified
                )
            )
        }
    ) { innerPadding ->
        content(innerPadding)
    }
}

data class MenuItemData(
    val icon: ImageVector,
    val title: String,
    val onClick: () -> Unit
)
