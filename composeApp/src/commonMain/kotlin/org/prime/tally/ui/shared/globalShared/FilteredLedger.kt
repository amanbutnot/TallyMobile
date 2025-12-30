package org.prime.tally.ui.shared.globalShared

import org.prime.tally.data.expect.DatabaseHolder
import org.prime.tally.data.utils.SharedPrefs
import org.tally.LedgerMaster
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


    println("AGRP: $filterAGRP and filter accounts $filterAccounts")

    return when {
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




