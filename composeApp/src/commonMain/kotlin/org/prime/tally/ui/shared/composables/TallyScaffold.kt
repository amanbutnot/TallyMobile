package org.prime.tally.ui.shared.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TallyScaffold(
    title: String,
    content: @Composable (PaddingValues) -> Unit,
    showBottomBar: Boolean = false,
    bottomBarContent: @Composable () -> Unit = {}
) {
    val nav = LocalNavigator.currentOrThrow
    val colors = MaterialTheme.colorScheme
    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                Box(modifier = Modifier.navigationBarsPadding()){
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
    content: @Composable (PaddingValues) -> Unit,
    showBottomBar: Boolean = false,
    bottomBarContent: @Composable () -> Unit = {},
    showSearchAction: Boolean = false,
    onSearchClick: (() -> Unit)? = null,
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
                actions = {
                    if (showSearchAction) {
                        IconButton(onClick = { onSearchClick?.invoke() }) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search",
                                tint = colors.onSurface
                            )
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
