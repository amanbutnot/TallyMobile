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
    val allNames = ids.toMutableList()
    val visitedGuids = mutableSetOf<String>()  // Set for O(1) lookup

    while (currentNames.isNotEmpty()) {
        val results = db.productGroupMasterQueries
            .getChildrenProductByGroupName(
                GroupName = currentNames,
                GUID = visitedGuids.ifEmpty { setOf("") }
            )
            .executeAsList()

        val childNames = results.mapNotNull { it.Name }
        val childGuids = results.mapNotNull { it.GUID }

        if (childNames.isEmpty()) break

        allNames.addAll(childNames)
        visitedGuids.addAll(childGuids)
        currentNames = childNames
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

        if (children.isEmpty()) {
            println("HGELLOSDFJLKSDJFLDSK " + allIds)
            return allIds
        }

        allIds.addAll(children)
        currentIds = children.mapNotNull { it.toDoubleOrNull() }
    }
    println("HGELLOSDFJLKSDJFLDSK " + allIds)

    return allIds
}

fun getSalemanPCFilter(groupCodes: List<String>): List<String> {
    val db = DatabaseHolder.instance
    val allIds = groupCodes.toMutableSet()
    val queue = ArrayDeque<String>()

    val parents = db.ledgerGroupMasterQueries
        .getParentsByGuid(GUID = groupCodes)
        .executeAsList()
        .mapNotNull { it.GUID }

    allIds.addAll(parents)
    queue.addAll(allIds)

    while (queue.isNotEmpty()) {
        val current = queue.removeFirst()

        val children = db.ledgerGroupMasterQueries
            .getChildrenByGroupCode(
                GroupCode = listOf(current),
                GUID = allIds.toList()
            )
            .executeAsList()
            .mapNotNull { it.GUID }

        for (child in children) {
            if (child !in allIds) {
                allIds.add(child)
                queue.add(child)
            }
        }
    }
    return allIds.toList()
}

fun filterGroupCodes(): List<Double> {
    val perms = SharedPrefs.Permissions.get()
    val groupCodes = perms?.ConfigAGRP.parseToStringList()
    return getSalemanPCFilter(groupCodes).mapNotNull { it.toDoubleOrNull() }
}

fun filterOneGroupCode(codes: List<String>): List<Double> {
    val perms = SharedPrefs.Permissions.get()
    //val groupCodes = perms?.ConfigAGRP.parseToStringList()
    return getSalemanPCFilter(codes).mapNotNull { it.toDoubleOrNull() }
}

fun filterItemGroupCodes(): List<Double> {
    val perms = SharedPrefs.Permissions.get()
    val groupCodes = perms?.ConfigIGRP.parseToStringList()
    println("asdlk;fjakl;sdfj " + getProductsGroupCodesByGuid(groupCodes).mapNotNull { it.toDoubleOrNull() })
    return getProductsGroupCodesByGuid(groupCodes).mapNotNull { it.toDoubleOrNull() }

}

fun filterItemGroupCodesByName(): List<Double> {
    val perms = SharedPrefs.Permissions.get()
    val groupCodes = perms?.ConfigIGRP.parseToStringList()
    println("asdlk;fjakl;sdfj " + getProductsGroupCodesByGuid(groupCodes).mapNotNull { it.toDoubleOrNull() })
    return getProductsGroupCodesByGuid(groupCodes).mapNotNull { it.toDoubleOrNull() }

}
