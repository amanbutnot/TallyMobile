package org.prime.easykarobar.ui.screen.transactions.sale

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import org.prime.easykarobar.data.model.transactions.SundryItem
import org.prime.easykarobar.ui.screen.transactions.TransactionBottomSheet
import org.prime.easykarobar.ui.screen.transactions.TransactionOneBottomSheet
import org.prime.easykarobar.ui.screen.transactions.TransactionSundryBottomSheet
import org.prime.easykarobar.ui.shared.globalShared.ProductsWithConfig


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionSheet(
    show: Boolean,
    title: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    TransactionOneBottomSheet(
        showBottomSheet = show,
        list = options,
        onSelected = {
            onSelect(it)
            onDismiss()
        },
        onDismiss = { onDismiss() },
        bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        title = title
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionSheetTwo(
    show: Boolean,
    title: String,
    options: List<Pair<String, String>>,
    onSelect: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    TransactionBottomSheet(
        showBottomSheet = show,
        list = options,
        onSelected = { it ->
            it.let {
                onSelect(it.first, it.second)

            }
            onDismiss()
        },
        onDismiss = { onDismiss() },
        bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        title = title
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionSheetThree(
    show: Boolean,
    title: String,
    options: List<SundryItem>,
    onSelect: (SundryItem) -> Unit,
    onDismiss: () -> Unit
) {
    TransactionSundryBottomSheet(
        showBottomSheet = show,
        list = options,
        onSelected = { it ->
            it.let {
                onSelect(it)

            }
            onDismiss()
        },
        onDismiss = { onDismiss() },
        bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        title = title
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionSheetItem(
    show: Boolean,
    title: String,
    options: List<ProductsWithConfig>,
    onSelect: (ProductsWithConfig) -> Unit,
    onDismiss: () -> Unit
) {
    TransactionItemBottomList(
        showBottomSheet = show,
        list = options,
        onSelected = {
            onSelect(it)
            onDismiss()
        },
        onDismiss = { onDismiss() },
        bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        title = title
    )
}