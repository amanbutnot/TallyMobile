package org.prime.tally.ui.shared.globalShared

import kotlinx.datetime.LocalDate
import org.prime.tally.data.utils.SharedPrefs
import org.tally.GetProductParamStockList
import org.tally.GetProductStockItemList
import org.tally.LedgerMaster
import org.tally.Products
import org.tally.TallyDatabase

// Extension functions
fun String?.parseToDoubleList(): List<Double> {
    if (this.isNullOrBlank()) return emptyList()
    return this.split(",")
        .mapNotNull { it.trim().removeSurrounding("'").toDoubleOrNull() }
}

fun String?.parseToStringList(): List<String> {
    if (this.isNullOrBlank()) return emptyList()
    return this.split(",")
        .map { it.trim().removeSurrounding("'") }
        .filter { it.isNotEmpty() }
}

fun getLedgerMasters(db: TallyDatabase): List<LedgerMaster> {
    val perms = SharedPrefs.Permissions.get()
    val filterAGRP = perms?.FilterAGRP == "Y"
    val filterAccounts = perms?.FilterAccounts == "Y"

    return when {
        perms == null -> {
            db.ledgerMasterQueries.selectAll()
                .executeAsList()
        }
        // Both filters active
        filterAGRP && filterAccounts -> {
            val groupCodes = perms.ConfigAGRP.parseToDoubleList()
            val excludeGuids = perms.ConfigAccounts.parseToStringList()
            db.ledgerMasterQueries.selectAllFilterAGRP(groupCodes, excludeGuids)
                .executeAsList()
        }

        // Only GroupCode filter
        filterAGRP -> {
            val groupCodes = perms.ConfigAGRP.parseToDoubleList()
            db.ledgerMasterQueries.selectByGroupCode(groupCodes)
                .executeAsList()
        }

        // Only GUID exclusion
        filterAccounts -> {
            val excludeGuids = perms.ConfigAccounts.parseToStringList()
            db.ledgerMasterQueries.selectExcludingGuid(excludeGuids)
                .executeAsList()
        }

        // No filters
        else -> {
            db.ledgerMasterQueries.selectAll()
                .executeAsList()
        }
    }
}

fun getItemMasters(db: TallyDatabase): List<Products> {
    val perms = SharedPrefs.Permissions.get()
    val filterIGRP = perms?.FilterIGRP == "Y"
    val filterItems = perms?.FilterItems == "Y"

    println(filterItems)
    println(filterIGRP)

    return when {
        perms == null -> {
            db.productsQueries.selectAll()
                .executeAsList()
        }
        // Both filters active
        filterIGRP && filterItems -> {
            val groupCodes = perms.ConfigIGRP.parseToDoubleList()
            val excludeGuids = perms.ConfigItems.parseToStringList()
            db.productsQueries.selectAllFilterAGRP(groupCodes, excludeGuids)
                .executeAsList()
        }

        // Only GroupCode filter
        filterIGRP -> {
            val groupCodes = perms.ConfigIGRP.parseToDoubleList()
            println(groupCodes)
            db.productsQueries.selectByGroupCode(groupCodes)
                .executeAsList()
        }

        // Only GUID exclusion
        filterItems -> {
            val excludeGuids = perms.ConfigItems.parseToStringList()
            db.productsQueries.selectExcludingGuid(excludeGuids)
                .executeAsList()
        }

        // No filters
        else -> {
            db.productsQueries.selectAll()
                .executeAsList()
        }
    }
}


fun outstandingFilter(): String? {
    return if (SharedPrefs.Permissions.get()?.FilterBroker == "Y") {
        SharedPrefs.Permissions.get()?.ConfigBroker
    } else {
        null
    }
}


fun getProductStockItems(db: TallyDatabase): List<GetProductStockItemList> {
    val perms = SharedPrefs.Permissions.get()

    if (perms == null) {
        return db.productStockQueries
            .getProductStockItemList(
                filterGroup = 0,
                groupCodes = emptyList(),
                filterExclude = 0,
                excludeGuids = emptyList(),
                filterGodown = 0,
                godownCodes = emptyList()
            )
            .executeAsList()
    }

    val filterGroup = if (perms.FilterIGRP == "Y") 1L else 0L
    val filterExclude = if (perms.FilterItems == "Y") 1L else 0L
    val filterGodown = if (perms.FilterGodown == "Y") 1L else 0L

    val groupCodes = if (filterGroup == 1L) perms.ConfigIGRP.parseToDoubleList() else emptyList()
    val excludeGuids =
        if (filterExclude == 1L) perms.ConfigItems.parseToStringList() else emptyList()
    val godownCodes =
        if (filterGodown == 1L) perms.ConfigGodown.parseToStringList() else emptyList()

    return db.productStockQueries
        .getProductStockItemList(
            filterGroup = filterGroup,
            groupCodes = groupCodes,
            filterExclude = filterExclude,
            excludeGuids = excludeGuids,
            filterGodown = filterGodown,
            godownCodes = godownCodes
        )
        .executeAsList()
}


fun getProductParamStockItems(db: TallyDatabase): List<GetProductParamStockList> {
    val perms = SharedPrefs.Permissions.get()

    val filterGroup = if (perms?.FilterIGRP == "Y") 1L else 0L
    val filterExclude = if (perms?.FilterItems == "Y") 1L else 0L
    val filterGodown = if (perms?.FilterGodown == "Y") 1L else 0L

    val groupCodes = if (filterGroup == 1L) perms?.ConfigIGRP.parseToDoubleList() else emptyList()
    val excludeGuids =
        if (filterExclude == 1L) perms?.ConfigItems.parseToStringList() else emptyList()
    val godownCodes =
        if (filterGodown == 1L) perms?.ConfigGodown.parseToDoubleList() else emptyList()

    if (perms == null) {
        return db.productParamStockQueries
            .getProductParamStockList(
                productGuid = null,
                filterParam1 = 0,
                configParam1 = emptyList(),     filterGroup = filterGroup,
                groupCodes = groupCodes,
                filterExclude = filterExclude,
                excludeGuids = excludeGuids,
                filterGodown = filterGodown,
                godownCodes = godownCodes
            )
            .executeAsList()
    }

    val filterParams = if (perms.FilterParam1 == "Y") 1L else 0L
    val paramCodes = if (filterParams == 1L) perms.ConfigParam1.parseToStringList() else emptyList()

    return db.productParamStockQueries
        .getProductParamStockList(
            productGuid = null,
            filterParam1 = filterParams,
            configParam1 = paramCodes,     filterGroup = filterGroup,
            groupCodes = groupCodes,
            filterExclude = filterExclude,
            excludeGuids = excludeGuids,
            filterGodown = filterGodown,
            godownCodes = godownCodes
        )
        .executeAsList()
}


fun parseDate(date: String): LocalDate {
    val parts = date.split("-")
    return LocalDate(
        year = parts[0].toInt(),
        monthNumber = parts[1].toInt(),
        dayOfMonth = parts[2].toInt()
    )
}
