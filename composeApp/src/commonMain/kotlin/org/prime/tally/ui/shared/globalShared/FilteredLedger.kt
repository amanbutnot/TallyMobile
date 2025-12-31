package org.prime.tally.ui.shared.globalShared

import org.prime.tally.data.expect.DatabaseHolder
import org.prime.tally.data.utils.SharedPrefs
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
        perms==null->{
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
    val filterAGRP = perms?.FilterIGRP == "Y"
    val filterAccounts = perms?.FilterItems == "Y"

    println(filterAccounts)
    println(filterAGRP)

    return when {
        perms==null->{
            db.productsQueries.selectAll()
                .executeAsList()
        }
        // Both filters active
        filterAGRP && filterAccounts -> {
            val groupCodes = perms.FilterIGRP.parseToDoubleList()
            val excludeGuids = perms.FilterItems.parseToStringList()
            db.productsQueries.selectAllFilterAGRP(groupCodes, excludeGuids)
                .executeAsList()
        }

        // Only GroupCode filter
        filterAGRP -> {
            val groupCodes = perms.FilterIGRP.parseToDoubleList()
            db.productsQueries.selectByGroupCode(groupCodes)
                .executeAsList()
        }

        // Only GUID exclusion
        filterAccounts -> {
            val excludeGuids = perms.FilterItems.parseToStringList()
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




