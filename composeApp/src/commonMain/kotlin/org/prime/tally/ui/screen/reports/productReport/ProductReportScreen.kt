package org.prime.tally.ui.screen.reports.productReport

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.prime.tally.data.expect.DatabaseHolder
import org.prime.tally.ui.printing.productReportHtml
import org.prime.tally.ui.shared.composables.MenuItemData
import org.prime.tally.ui.shared.composables.TallyCircularLoader
import org.prime.tally.ui.shared.composables.TallyLoadingDialog
import org.prime.tally.ui.shared.composables.TallyReportScaffold
import org.prime.tally.ui.shared.composables.TallySearchBar
import org.prime.tally.ui.shared.reportsShared.PdfAction
import org.prime.tally.ui.shared.reportsShared.ReportColumn
import org.prime.tally.ui.shared.reportsShared.TallyReportBottomBar
import org.prime.tally.ui.shared.reportsShared.handlePdfAction
import org.tally.GetProductStockList

data class ProductReportScreen(val productGuid: String? = null, val isMain: Boolean) : Screen {
    @Composable
    override fun Content() {
        val db = DatabaseHolder.instance

        var list by remember { mutableStateOf<List<GetProductStockList>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var showSearchBar by remember { mutableStateOf(false) }
        var searchQuery by remember { mutableStateOf("") }
        val focusRequester = remember { FocusRequester() }
        var shareLoading by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            isLoading = true
            withContext(Dispatchers.IO) {
                println(productGuid)
                list = db.productParamStockQueries.getProductStockList(productGuid).executeAsList()
            }
            isLoading = false
        }

        LaunchedEffect(showSearchBar) {
            if (showSearchBar) {
                focusRequester.requestFocus()
            }
        }

        val filteredList = if (searchQuery.isEmpty()) {
            list
        } else {
            list.filter {
                it.ProductName?.contains(searchQuery, ignoreCase = true) == true ||
                        it.GodownName?.contains(searchQuery, ignoreCase = true) == true
            }
        }

        if (shareLoading) {
            TallyLoadingDialog("Generating Report")
        }


        val menuItems = listOf(
            MenuItemData(
                title = "Download",
                icon = Icons.Default.Download,
                onClick = {
                    scope.launch {
                        handlePdfAction(
                            fileName = "Product Report",
                            htmlContent = productReportHtml(
                                rows = list
                            ),
                            action = PdfAction.Download,
                            onLoadingChange = { shareLoading = it }
                        )
                    }
                }
            ),
            MenuItemData(
                title = "Share",
                icon = Icons.Default.Share,
                onClick = {
                    scope.launch {
                        handlePdfAction(
                            fileName = "Product Report",
                            htmlContent = productReportHtml(
                                rows = list
                            ),
                            action = PdfAction.Download,
                            onLoadingChange = { shareLoading = it }
                        )
                    }
                }
            )
        )

        if (shareLoading) {
            TallyLoadingDialog("Generating Report")
        }

        TallyReportScaffold(
            title = "Product Report",
            showBottomBar = true,
            showSearchAction = true,
            showBurgerMenu = true,
            menuItems = menuItems,
            onSearchClick = { showSearchBar = !showSearchBar },
            bottomBarContent = {
                TallyReportBottomBar(
                    columns = listOf(
                        ReportColumn(
                            "Rows: ${filteredList.count()}",
                            1f,
                            TextAlign.Start
                        )
                    )
                )
            },
            content = { paddingValues ->
                if (isLoading) {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        TallyCircularLoader()
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        if (showSearchBar) {
                            TallySearchBar(
                                searchQuery = searchQuery,
                                onQueryChange = { searchQuery = it },
                                modifier = Modifier.focusRequester(focusRequester)
                            )
                        }
                        if(isMain){
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(filteredList) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp)
                                            .padding(top = 16.dp, bottom = 20.dp)
                                    ) {
                                        Text(
                                            text = it.ProductName.toString(),
                                            style = MaterialTheme.typography.headlineLarge,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = it.C1.toString(),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = it.C2.toString(),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = it.C3.toString(),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = it.Value1.toString(),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        thickness = 1.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                            }
                        }else{
                            val groupId = filteredList.groupBy { it.ProductName }
                            LazyColumn(modifier = Modifier.fillMaxSize())
                            {
                                groupId.forEach { (name,items) ->
                                    item {
                                        Text(
                                            text = name.toString(),
                                            style = MaterialTheme.typography.headlineLarge,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                    }
                                    items(items){
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp)
                                                .padding(top = 16.dp, bottom = 20.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = it.C1.toString(),
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = it.C2.toString(),
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = it.C3.toString(),
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = it.Value1.toString(),
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                        HorizontalDivider(
                                            modifier = Modifier.padding(horizontal = 16.dp),
                                            thickness = 1.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                }
                            }
                        }


//                        // Header Card (sticky)
//                        ScrollableScreen(horizontalScrollState, columnWidths, filteredList)
                    }
                }
            }
        )
    }
}

@Composable
private fun ColumnScope.ScrollableScreen(
    horizontalScrollState: ScrollState,
    columnWidths: List<Dp>,
    filteredList: List<GetProductStockList>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    )
    {
        Row(
            modifier = Modifier
                .horizontalScroll(horizontalScrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            TableCellFixed(
                text = "Product Name",
                width = columnWidths[0],
                textAlign = TextAlign.Start,
                isHeader = true
            )
            TableCellFixed(
                text = "Location",
                width = columnWidths[1],
                textAlign = TextAlign.Start,
                isHeader = true
            )
            TableCellFixed(
                text = "Main Qty",
                width = columnWidths[2],
                textAlign = TextAlign.End,
                isHeader = true
            )
            TableCellFixed(
                text = "Alt Qty",
                width = columnWidths[3],
                textAlign = TextAlign.End,
                isHeader = true
            )
            TableCellFixed(
                text = "C1",
                width = columnWidths[4],
                textAlign = TextAlign.End,
                isHeader = true
            )
            TableCellFixed(
                text = "C2",
                width = columnWidths[5],
                textAlign = TextAlign.End,
                isHeader = true
            )
            TableCellFixed(
                text = "C3",
                width = columnWidths[6],
                textAlign = TextAlign.End,
                isHeader = true
            )
        }
    }
    val state = rememberLazyListState()
    // Data rows with vertical scroll
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f), state = state
    )
    {
        itemsIndexed(items = filteredList) { index, item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (index % 2 == 0) {
                        MaterialTheme.colorScheme.surface
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    }
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .horizontalScroll(horizontalScrollState)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    TableCellFixed(
                        text = item.ProductName ?: "-",
                        width = columnWidths[0],
                        textAlign = TextAlign.Start,
                        isHeader = false
                    )
                    TableCellFixed(
                        text = item.GodownName ?: "-",
                        width = columnWidths[1],
                        textAlign = TextAlign.Start,
                        isHeader = false
                    )
                    TableCellFixed(
                        text = item.Value1?.toString() ?: "-",
                        width = columnWidths[2],
                        textAlign = TextAlign.End,
                        isHeader = false
                    )
                    TableCellFixed(
                        text = item.Value2?.toString() ?: "-",
                        width = columnWidths[3],
                        textAlign = TextAlign.End,
                        isHeader = false
                    )
                    TableCellFixed(
                        text = item.C1 ?: "-",
                        width = columnWidths[4],
                        textAlign = TextAlign.End,
                        isHeader = false
                    )
                    TableCellFixed(
                        text = item.C2 ?: "-",
                        width = columnWidths[5],
                        textAlign = TextAlign.End,
                        isHeader = false
                    )
                    TableCellFixed(
                        text = item.C3 ?: "-",
                        width = columnWidths[6],
                        textAlign = TextAlign.End,
                        isHeader = false
                    )
                }
            }
            if (index < filteredList.lastIndex) {
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun TableCellFixed(
    text: String,
    width: Dp,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
    isHeader: Boolean = false
) {
    Text(
        text = text,
        modifier = modifier
            .width(width)
            .padding(horizontal = 8.dp),
        textAlign = textAlign,
        style = if (isHeader) {
            MaterialTheme.typography.titleSmall
        } else {
            MaterialTheme.typography.bodyMedium
        },
        fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
        color = if (isHeader) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        maxLines = if (isHeader) 1 else 2,
        overflow = TextOverflow.Ellipsis
    )
}
