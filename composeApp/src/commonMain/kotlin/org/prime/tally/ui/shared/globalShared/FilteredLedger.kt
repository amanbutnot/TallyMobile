package org.prime.tally.ui.shared.globalShared

import org.prime.tally.data.utils.SharedPrefs
import org.tally.GetByGodownCodes
import org.tally.GetByGroupAndExclude
import org.tally.GetByGroupAndGodown
import org.tally.GetByGroupCode
import org.tally.GetByGroupExcludeAndGodown
import org.tally.GetExcludeAndGodown
import org.tally.GetExcludingGuids
import org.tally.GetProductStockItemList
import org.tally.LedgerMaster
import org.tally.ProductStock
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
    return if (SharedPrefs.Permissions.get()?.D29 == 1) {
        SharedPrefs.User.get()?.FirstName
    } else {
        null
    }
}

private fun GetProductStockItemList.toDomain() = this
private fun GetByGroupCode.toDomain() = GetProductStockItemList(
    MasterCode1, MasterCode2, Value1, Value2, Value3,
    GUID, ProductName, UnitName, GodownName
)
private fun GetExcludingGuids.toDomain() = GetProductStockItemList(
    MasterCode1, MasterCode2, Value1, Value2, Value3,
    GUID, ProductName, UnitName, GodownName
)
private fun GetByGodownCodes.toDomain() = GetProductStockItemList(
    MasterCode1, MasterCode2, Value1, Value2, Value3,
    GUID, ProductName, UnitName, GodownName
)
private fun GetByGroupAndExclude.toDomain() = GetProductStockItemList(
    MasterCode1, MasterCode2, Value1, Value2, Value3,
    GUID, ProductName, UnitName, GodownName
)
private fun GetByGroupAndGodown.toDomain() = GetProductStockItemList(
    MasterCode1, MasterCode2, Value1, Value2, Value3,
    GUID, ProductName, UnitName, GodownName
)
private fun GetExcludeAndGodown.toDomain() = GetProductStockItemList(
    MasterCode1, MasterCode2, Value1, Value2, Value3,
    GUID, ProductName, UnitName, GodownName
)
private fun GetByGroupExcludeAndGodown.toDomain() = GetProductStockItemList(
    MasterCode1, MasterCode2, Value1, Value2, Value3,
    GUID, ProductName, UnitName, GodownName
)


fun getProductStockItems(db: TallyDatabase): List<GetProductStockItemList> {
    val perms = SharedPrefs.Permissions.get()

    if (perms == null) {
        return db.productStockQueries
            .getProductStockItemList()
            .executeAsList()
            .map { it.toDomain() }
    }

    val filterGroup = perms.FilterIGRP == "Y"
    val filterExclude = perms.FilterItems == "Y"
    val filterGodown = perms.FilterMC == "Y"

    val groupCodes = if (filterGroup) perms.ConfigIGRP.parseToDoubleList() else emptyList()
    val excludeGuids = if (filterExclude) perms.ConfigItems.parseToStringList() else emptyList()
    val godownCodes = if (filterGodown) perms.ConfigMC.parseToStringList() else emptyList()

    return when {
        filterGroup && filterExclude && filterGodown -> {
            db.productStockQueries
                .getByGroupExcludeAndGodown(groupCodes, excludeGuids, godownCodes)
                .executeAsList()
                .map { it.toDomain() }
        }
        filterGroup && filterExclude -> {
            db.productStockQueries
                .getByGroupAndExclude(groupCodes, excludeGuids)
                .executeAsList()
                .map { it.toDomain() }
        }
        filterGroup && filterGodown -> {
            db.productStockQueries
                .getByGroupAndGodown(groupCodes, godownCodes)
                .executeAsList()
                .map { it.toDomain() }
        }
        filterExclude && filterGodown -> {
            db.productStockQueries
                .getExcludeAndGodown(excludeGuids, godownCodes)
                .executeAsList()
                .map { it.toDomain() }
        }
        filterGroup -> {
            db.productStockQueries
                .getByGroupCode(groupCodes)
                .executeAsList()
                .map { it.toDomain() }
        }
        filterExclude -> {
            db.productStockQueries
                .getExcludingGuids(excludeGuids)
                .executeAsList()
                .map { it.toDomain() }
        }
        filterGodown -> {
            db.productStockQueries
                .getByGodownCodes(godownCodes)
                .executeAsList()
                .map { it.toDomain() }
        }
        else -> {
            db.productStockQueries
                .getProductStockItemList()
                .executeAsList()
                .map { it.toDomain() }
        }
    }
}

