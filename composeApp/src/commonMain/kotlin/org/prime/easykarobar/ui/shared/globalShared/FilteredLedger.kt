package org.prime.easykarobar.ui.shared.globalShared

import kotlinx.datetime.LocalDate
import org.prime.easykarobar.TallyDatabase
import org.prime.easykarobar.data.utils.SharedPrefs
import org.tally.BatchNoReport
import org.tally.GetProductParamStockList
import org.tally.GetProductStockItemList
import org.tally.LedgerMaster
import org.tally.Products
import org.tally.SerialNoReport

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

data class ProductsWithConfig(
    val ID: Long,
    val Name: String?,
    val Alias: String?,
    val PrintName: String?,
    val GroupName: String?,
    val GroupCode: Double?,
    val UnitName: String?,
    val UnitCode: Double?,
    val OpStk: Double?,
    val OpStkValue: Double?,
    val TaxCategory: String?,
    val TaxCategoryCode: Double?,
    val HSN: String?,
    val SalesPrice: Double?,
    val PurcPrice: Double?,
    val MRP: Double?,
    val MinSalesPrice: Double?,
    val SelfValPrice: Double?,
    val SaleDisc: Double?,
    val PurcDisc: Double?,
    val Vendor: String?,
    val VendorCode: Double?,
    val MaintainStock: Double?,
    val ALTERID: String?,
    val GUID: String?,
    val N1: Double?,
    val AltUnit: String?,
    val ConFactor: Double?,
    val ConType: Double?,
    val McOpening: Double
)

private fun mapToProductsWithConfig(
    ID: Long,
    Name: String?,
    Alias: String?,
    PrintName: String?,
    GroupName: String?,
    GroupCode: Double?,
    UnitName: String?,
    UnitCode: Double?,
    OpStk: Double?,
    OpStkValue: Double?,
    TaxCategory: String?,
    TaxCategoryCode: Double?,
    HSN: String?,
    SalesPrice: Double?,
    PurcPrice: Double?,
    MRP: Double?,
    MinSalesPrice: Double?,
    SelfValPrice: Double?,
    SaleDisc: Double?,
    PurcDisc: Double?,
    Vendor: String?,
    VendorCode: Double?,
    MaintainStock: Double?,
    ALTERID: String?,
    GUID: String?,
    N1: Double?,
    AltUnit: String?,
    ConFactor: Double?,
    ConType: Double?,
    McOpening: Double
): ProductsWithConfig = ProductsWithConfig(
    ID, Name, Alias, PrintName, GroupName, GroupCode, UnitName, UnitCode, OpStk, OpStkValue,
    TaxCategory, TaxCategoryCode, HSN, SalesPrice, PurcPrice, MRP, MinSalesPrice, SelfValPrice,
    SaleDisc, PurcDisc, Vendor, VendorCode, MaintainStock, ALTERID, GUID, N1, AltUnit, ConFactor, ConType, McOpening
)

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
            val excludeGuids = perms.ConfigAccounts.parseToStringList()
            db.ledgerMasterQueries.selectAllFilterAGRP(filterGroupCodes(), excludeGuids)
                .executeAsList()
        }

        // Only GroupCode filter
        filterAGRP -> {
            db.ledgerMasterQueries.selectByGroupCode(filterGroupCodes())
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


fun getConfigItemMasters(db: TallyDatabase, vchType: Int): List<ProductsWithConfig> {
    val perms = SharedPrefs.Permissions.get()
    val filterIGRP = perms?.FilterIGRP == "Y"
    val filterItems = perms?.FilterItems == "Y"
    val showZeroGroup = if (SharedPrefs.ShowZeroStock.get() == false) 1L else 0L

    println(filterItems)
    val itemConfig = when (vchType) {
        9, 3, 12 -> {
            SaleItemConfig()
        }

        13, 10, 2 -> {
            PurItemConfig()
        }

        else -> {
            ""
        }
    }
    println("VCH TYPE: $vchType")
    println("ITEM CONFIG: $itemConfig")
    return when {
        perms == null -> {
            db.productsQueries.selectAllConfig(
                applyN1Filter = showZeroGroup,
                compConfigFilter = itemConfig,
                mapper = ::mapToProductsWithConfig
            ).executeAsList()
        }
        // Both filters active
        filterIGRP && filterItems -> {
            val excludeGuids = perms.ConfigItems.parseToStringList()
            db.productsQueries.selectAllFilterAGRPConfig(
                compConfigFilter = itemConfig,
                GroupCode = filterItemGroupCodes(),
                GUID = excludeGuids,
                applyN1Filter = showZeroGroup,
                mapper = ::mapToProductsWithConfig
            ).executeAsList()
        }

        // Only GroupCode filter
        filterIGRP -> {
            db.productsQueries.selectByGroupCodeConfig(
                compConfigFilter = itemConfig,
                GroupCode = filterItemGroupCodes(),
                applyN1Filter = showZeroGroup,
                mapper = ::mapToProductsWithConfig
            ).executeAsList()
        }

        // Only GUID exclusion
        filterItems -> {
            val excludeGuids = perms.ConfigItems.parseToStringList()
            db.productsQueries.selectExcludingGuidConfig(
                compConfigFilter = itemConfig,
                GUID = excludeGuids,
                applyN1Filter = showZeroGroup,
                mapper = ::mapToProductsWithConfig
            ).executeAsList()
        }

        // No filters
        else -> {
            db.productsQueries.selectAllConfig(
                applyN1Filter = showZeroGroup,
                compConfigFilter = itemConfig,
                mapper = ::mapToProductsWithConfig
            ).executeAsList()
        }
    }
}


fun getItemMasters(db: TallyDatabase): List<Products> {
    val perms = SharedPrefs.Permissions.get()
    val filterIGRP = perms?.FilterIGRP == "Y"
    val filterItems = perms?.FilterItems == "Y"
    val showZeroGroup = if(SharedPrefs.ShowZeroStock.get() == false)1L else 0L

    println(filterItems)
    println(filterIGRP)

    return when {
        perms == null -> {
            db.productsQueries.selectAll(
                applyN1Filter = showZeroGroup
            )
                .executeAsList()
        }
        // Both filters active
        filterIGRP && filterItems -> {
            val excludeGuids = perms.ConfigItems.parseToStringList()
            db.productsQueries.selectAllFilterAGRP(filterItemGroupCodes(), excludeGuids, applyN1Filter = showZeroGroup)
                .executeAsList()
        }

        // Only GroupCode filter
        filterIGRP -> {
            db.productsQueries.selectByGroupCode(filterItemGroupCodes(), applyN1Filter = showZeroGroup)
                .executeAsList()
        }

        // Only GUID exclusion
        filterItems -> {
            val excludeGuids = perms.ConfigItems.parseToStringList()
            db.productsQueries.selectExcludingGuid(excludeGuids, applyN1Filter = showZeroGroup)
                .executeAsList()
        }

        // No filters
        else -> {
            db.productsQueries.selectAll( applyN1Filter = showZeroGroup)
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

    val filterExclude = if (perms.FilterItems == "Y") 1L else 0L
    val filterGodown = if (perms.FilterGodown == "Y") 1L else 0L

    val excludeGuids =
        if (filterExclude == 1L) perms.ConfigItems.parseToStringList() else emptyList()
    val godownCodes =
        if (filterGodown == 1L) perms.ConfigGodown.parseToStringList() else emptyList()


    println("-STOCK")
    println("groupcodes : ${itemGroupCodes()}")
    println("-godownCodes : $godownCodes")
    println("-exclude GUID: $excludeGuids")
    return db.productStockQueries
        .getProductStockItemList(
            groupCodes = itemGroupCodes(),
            godownCodes = godownCodes,
            excludeGuids = excludeGuids,
            filterGroup = filterItemGroups(),
            filterExclude = filterExclude,
            filterGodown = filterGodown
        )
        .executeAsList()
}

fun getProductSerialNo(
    db: TallyDatabase,
    isMain: Boolean,
    godownName: String? = null
): List<SerialNoReport> {
    val perms = SharedPrefs.Permissions.get()
    println("IS MAIN" + isMain)
    println(godownName)
    if (perms == null) {
        return db.productSerialNoQueries
            .serialNoReport(
                filterGroup = 0,
                groupCodes = emptyList(),
                filterExclude = 0,
                excludeGuids = emptyList(),
                filterGodown = 0,
                godownCodes = emptyList(),
                filterSingle = if (isMain) 0L else 1L,
                includeSingle = godownName
            )
            .executeAsList()
    }

    val filterExclude = if (perms.FilterItems == "Y") 1L else 0L
    val filterGodown = if (perms.FilterGodown == "Y") 1L else 0L

    val excludeGuids =
        if (filterExclude == 1L) perms.ConfigItems.parseToStringList() else emptyList()
    val godownCodes =
        if (filterGodown == 1L) perms.ConfigGodown.parseToStringList() else emptyList()

    return db.productSerialNoQueries
        .serialNoReport(
            groupCodes = itemGroupCodes(),
            godownCodes = godownCodes,
            excludeGuids = excludeGuids,
            filterGroup = filterItemGroups(),
            filterExclude = filterExclude,
            filterGodown = filterGodown, filterSingle = if (isMain) 0L else 1L,
            includeSingle = godownName
        )
        .executeAsList()
}

fun getProductBatchNo(
    db: TallyDatabase,
    isMain: Boolean,
    godownName: String? = null
): List<BatchNoReport> {
    val perms = SharedPrefs.Permissions.get()
    println("IS MAIN" + isMain)
    println(godownName)
    if (perms == null) {
        return db.productBatchNoQueries
            .batchNoReport(
                filterGroup = 0,
                groupCodes = emptyList(),
                filterExclude = 0,
                excludeGuids = emptyList(),
                filterGodown = 0,
                godownCodes = emptyList(),
                filterSingle = if (isMain) 0L else 1L,
                includeSingle = godownName
            )
            .executeAsList()
    }

    val filterExclude = if (perms.FilterItems == "Y") 1L else 0L
    val filterGodown = if (perms.FilterGodown == "Y") 1L else 0L

    val excludeGuids =
        if (filterExclude == 1L) perms.ConfigItems.parseToStringList() else emptyList()
    val godownCodes =
        if (filterGodown == 1L) perms.ConfigGodown.parseToStringList() else emptyList()

    return db.productBatchNoQueries
        .batchNoReport(
            groupCodes = itemGroupCodes(),
            godownCodes = godownCodes,
            excludeGuids = excludeGuids,
            filterGroup = filterItemGroups(),
            filterExclude = filterExclude,
            filterGodown = filterGodown, filterSingle = if (isMain) 0L else 1L,
            includeSingle = godownName
        )
        .executeAsList()
}


fun getProductParamStockItems(db: TallyDatabase): List<GetProductParamStockList> {
    val perms = SharedPrefs.Permissions.get()

    val filterGroup = if (perms?.FilterIGRP == "Y") 1L else 0L
    val filterExclude = if (perms?.FilterItems == "Y") 1L else 0L
    val filterGodown = if (perms?.FilterGodown == "Y") 1L else 0L

    val groupCodes = if (filterGroup == 1L) filterItemGroupCodes() else emptyList()
    val excludeGuids =
        if (filterExclude == 1L) perms?.ConfigItems.parseToStringList() else emptyList()
    val godownCodes =
        if (filterGodown == 1L) perms?.ConfigGodown.parseToDoubleList() else emptyList()

    if (perms == null) {
        return db.productParamStockQueries
            .getProductParamStockList(
                productGuid = null,
                filterParam1 = 0,
                configParam1 = emptyList(), filterGroup = filterGroup,
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
            configParam1 = paramCodes, filterGroup = filterGroup,
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
