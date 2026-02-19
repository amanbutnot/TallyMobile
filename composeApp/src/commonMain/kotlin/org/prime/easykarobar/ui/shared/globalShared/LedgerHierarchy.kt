package org.prime.easykarobar.ui.shared.globalShared

import org.prime.easykarobar.data.expect.DatabaseHolder

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
