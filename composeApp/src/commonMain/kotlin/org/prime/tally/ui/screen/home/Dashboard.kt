package org.prime.tally.ui.screen.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import org.prime.tally.ui.screen.home.tabs.HomeTab
import org.prime.tally.ui.screen.home.tabs.MastersTab
import org.prime.tally.ui.screen.home.tabs.ReportingTab
import org.prime.tally.ui.screen.home.tabs.TransactionTab
import org.prime.tally.ui.shared.TallyDivider
import org.tally.TallyDatabase

object Dashboard : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val colors = MaterialTheme.colorScheme
        val nav = LocalNavigator.currentOrThrow
        TabNavigator(HomeTab) { tabNavigator ->
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                "Company Name",
                                color = colors.onBackground,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium)
                            )
                        },
                        actions = {
                            IconButton(onClick = { nav.push(SettingScreen) }) {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = "Settings icon",
                                    tint = colors.onBackground
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = colors.primary.copy(alpha = 0.2f)
                        )
                    )
                },
                bottomBar = {
                    NavigationBar {
                        TabNavigationItem(HomeTab, modifier = Modifier.weight(1f))
                        TabNavigationItem(MastersTab, modifier = Modifier.weight(1f))
                        TabNavigationItem(TransactionTab, modifier = Modifier.weight(1f))
                        TabNavigationItem(ReportingTab, modifier = Modifier.weight(1f))
                    }
                }) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    CurrentTab()
                }

            }
        }
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TabNavigationItem(tab: Tab, modifier: Modifier = Modifier) {
    val tabNavigator = LocalTabNavigator.current
    val selected = tabNavigator.current == tab

    val colorTint by animateColorAsState(
        targetValue = if (selected)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.onSurfaceVariant,
        label = "IconTint"
    )


    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.15f else 1f,
        label = "IconScale"
    )

    ShortNavigationBarItem(
        modifier = modifier,
        selected = selected,
        onClick = { tabNavigator.current = tab },
        icon = {
            tab.options.icon?.let {
                Icon(
                    it,
                    contentDescription = tab.options.title,
                    tint = colorTint,
                    modifier = Modifier.scale(iconScale)
                )
            }
        },
        label = {
            Text(
                text = tab.options.title,
                style = MaterialTheme.typography.labelMedium,
                color = colorTint
            )
        }
    )
}
