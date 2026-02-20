package org.prime.easykarobar.ui.shared.globalShared

import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.utils.SharedPrefs

fun getPCGroupCodes(id: String): List<String> {
    val db = DatabaseHolder.instance
    var currentIds = listOf(id)
    val allIds = mutableListOf(id)

    repeat(9999) {
        val children = db.ledgerGroupMasterQueries
            .getChildrenByGroupCode(
                GroupCode = currentIds,
                GUID = allIds
            )
            .executeAsList()
            .mapNotNull { it.GUID }

        if (children.isEmpty()) return allIds

        allIds.addAll(children)
        currentIds = children
    }

    return allIds
}

fun getPCGroupCodesByName(ids: List<String>): List<String> {
    val db = DatabaseHolder.instance
    var currentNames = ids
    val allNames = mutableListOf<String>().apply { addAll(ids) }
    val visitedGuids = mutableListOf<String>()

    repeat(9999) {
        val results = db.ledgerGroupMasterQueries
            .getChildrenByGroupName(
                GroupName = currentNames,
                GUID = visitedGuids.ifEmpty { listOf("") }
            )
            .executeAsList()

        val childNames = results.mapNotNull { it.Name }
        val childGuids = results.mapNotNull { it.GUID }

        if (childNames.isEmpty()) return allNames

        allNames.addAll(childNames)
        visitedGuids.addAll(childGuids)
        currentNames = childNames
    }

    return allNames
}

fun getProductsGroupCodesByName(ids: List<String>): List<String> {
    val db = DatabaseHolder.instance
    var currentNames = ids
    val allNames = mutableListOf<String>().apply { addAll(ids) }
    val visitedGuids = mutableListOf<String>()

    repeat(9999) {
        val results = db.productGroupMasterQueries
            .getChildrenProductByGroupName(
                GroupName = currentNames,
                GUID = visitedGuids.ifEmpty { listOf("") }
            )
            .executeAsList()

        val childNames = results.mapNotNull { it.Name }
        val childGuids = results.mapNotNull { it.GUID }

        if (childNames.isEmpty()) return allNames

        allNames.addAll(childNames)
        visitedGuids.addAll(childGuids)
        currentNames = childNames  // next iteration searches children of current children
    }

    return allNames
}

fun getProductsGroupCodesByGuid(ids: List<String>): List<String> {
    val db = DatabaseHolder.instance

    var currentIds = ids.mapNotNull { it.toDoubleOrNull() }
    val allIds = ids.toMutableList()

    repeat(9999) {
        val children = db.productGroupMasterQueries
            .getChildrenProductByGroupCode(
                GroupCode = currentIds,
                GUID = allIds
            )
            .executeAsList()
            .mapNotNull { it.GUID }

        if (children.isEmpty()) return allIds

        allIds.addAll(children)
        currentIds = children.mapNotNull { it.toDoubleOrNull() }
    }

    return allIds
}

fun getSalemanPCFilter(ids: List<String>): List<String> {
    val db = DatabaseHolder.instance

    var currentIds = ids
    val allIds = ids.toMutableList()

    repeat(9999) {
        val children = db.ledgerGroupMasterQueries
            .getChildrenByGroupCode(
                GroupCode = currentIds,
                GUID = allIds
            )
            .executeAsList()
            .mapNotNull { it.GUID }

        if (children.isEmpty()) return allIds

        allIds.addAll(children)
        currentIds = children
    }

    return allIds
}


fun filterGroupCodes(): List<Double> {
    val perms = SharedPrefs.Permissions.get()
    val groupCodes = perms?.ConfigAGRP.parseToStringList()

    println("FilterGroupCodes: ${getSalemanPCFilter(groupCodes).mapNotNull { it.toDoubleOrNull() }}")

    return getSalemanPCFilter(groupCodes).mapNotNull { it.toDoubleOrNull() }

}

fun filterItemGroupCodes(): List<Double> {
    val perms = SharedPrefs.Permissions.get()
    val groupCodes = perms?.ConfigIGRP.parseToStringList()
    println(getProductsGroupCodesByGuid(groupCodes).mapNotNull { it.toDoubleOrNull() })
    return getProductsGroupCodesByGuid(groupCodes).mapNotNull { it.toDoubleOrNull() }

}
