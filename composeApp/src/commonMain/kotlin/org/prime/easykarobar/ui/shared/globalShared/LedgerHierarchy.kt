package org.prime.easykarobar.ui.shared.globalShared

import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.utils.SharedPrefs

fun getPCGroupCodes(id: String): List<String> {
    if (id.isBlank()) return emptyList()
    val db = DatabaseHolder.instance
    val allGroups = db.ledgerGroupMasterQueries.simpleSelectAll().executeAsList()
    if (allGroups.isEmpty()) return listOf(id)

    val childrenMap = HashMap<String, MutableList<String>>()
    for (group in allGroups) {
        val parent = group.GroupCode
        val guid = group.GUID ?: continue
        childrenMap.getOrPut(parent) { mutableListOf() }.add(guid)
    }

    val visited = mutableSetOf(id)
    val queue = ArrayDeque<String>().apply { add(id) }

    while (queue.isNotEmpty()) {
        val current = queue.removeFirst()
        childrenMap[current]?.forEach { childGuid ->
            if (visited.add(childGuid)) {
                queue.add(childGuid)
            }
        }
    }

    return visited.toList()
}

fun getPCGroupCodesByName(ids: List<String>): List<String> {
    if (ids.isEmpty()) return emptyList()
    val db = DatabaseHolder.instance
    val allGroups = db.ledgerGroupMasterQueries.simpleSelectAll().executeAsList()
    if (allGroups.isEmpty()) return ids

    val childrenMap = HashMap<String, MutableList<Pair<String, String>>>()
    for (group in allGroups) {
        val parentName = group.GroupName ?: continue
        val childName = group.Name ?: continue
        val childGuid = group.GUID ?: ""
        childrenMap.getOrPut(parentName) { mutableListOf() }.add(Pair(childName, childGuid))
    }

    val resultNames = ids.toMutableList()
    val visitedGuids = mutableSetOf<String>()
    var currentNames = ids

    while (currentNames.isNotEmpty()) {
        val nextNames = mutableListOf<String>()
        for (pName in currentNames) {
            childrenMap[pName]?.forEach { (cName, cGuid) ->
                if (cGuid.isEmpty() || visitedGuids.add(cGuid)) {
                    resultNames.add(cName)
                    nextNames.add(cName)
                }
            }
        }
        currentNames = nextNames
    }

    return resultNames
}

fun getProductsGroupCodesByName(ids: List<String>): List<String> {
    if (ids.isEmpty()) return emptyList()
    val db = DatabaseHolder.instance
    val allProductGroups = db.productGroupMasterQueries.selectAll(0, emptyList()).executeAsList()
    if (allProductGroups.isEmpty()) return ids

    val childrenMap = HashMap<String, MutableList<Pair<String, String>>>()
    for (group in allProductGroups) {
        val parentName = group.GroupName ?: continue
        val childName = group.Name ?: continue
        val childGuid = group.GUID ?: ""
        childrenMap.getOrPut(parentName) { mutableListOf() }.add(Pair(childName, childGuid))
    }

    val resultNames = ids.toMutableList()
    val visitedGuids = mutableSetOf<String>()
    var currentNames = ids

    while (currentNames.isNotEmpty()) {
        val nextNames = mutableListOf<String>()
        for (pName in currentNames) {
            childrenMap[pName]?.forEach { (cName, cGuid) ->
                if (cGuid.isEmpty() || visitedGuids.add(cGuid)) {
                    resultNames.add(cName)
                    nextNames.add(cName)
                }
            }
        }
        currentNames = nextNames
    }

    return resultNames
}

fun getProductsGroupCodesByGuid(ids: List<String>): List<String> {
    if (ids.isEmpty()) return emptyList()
    val db = DatabaseHolder.instance
    val allProductGroups = db.productGroupMasterQueries.selectAll(0, emptyList()).executeAsList()
    if (allProductGroups.isEmpty()) return ids

    val childrenMap = HashMap<Double, MutableList<String>>()
    for (group in allProductGroups) {
        val gCode = group.GroupCode ?: continue
        val guid = group.GUID ?: continue
        childrenMap.getOrPut(gCode) { mutableListOf() }.add(guid)
    }

    val resultGuids = ids.toMutableList()
    val visitedGuids = ids.toMutableSet()
    var currentCodes = ids.mapNotNull { it.toDoubleOrNull() }

    while (currentCodes.isNotEmpty()) {
        val nextCodes = mutableListOf<Double>()
        for (code in currentCodes) {
            childrenMap[code]?.forEach { childGuid ->
                if (visitedGuids.add(childGuid)) {
                    resultGuids.add(childGuid)
                    childGuid.toDoubleOrNull()?.let { nextCodes.add(it) }
                }
            }
        }
        currentCodes = nextCodes
    }

    return resultGuids
}

fun getSalemanPCFilter(groupCodes: List<String>): List<String> {
    if (groupCodes.isEmpty()) return emptyList()
    val db = DatabaseHolder.instance
    val allGroups = db.ledgerGroupMasterQueries.simpleSelectAll().executeAsList()
    if (allGroups.isEmpty()) return groupCodes

    val groupSet = groupCodes.toSet()
    val parents = allGroups.filter { it.GUID != null && groupSet.contains(it.GUID) }.mapNotNull { it.GUID }

    val allIds = (groupCodes + parents).toMutableSet()
    val childrenMap = HashMap<String, MutableList<String>>()
    for (group in allGroups) {
        val gCode = group.GroupCode
        val guid = group.GUID ?: continue
        childrenMap.getOrPut(gCode) { mutableListOf() }.add(guid)
    }

    val queue = ArrayDeque<String>().apply { addAll(allIds) }

    while (queue.isNotEmpty()) {
        val current = queue.removeFirst()
        childrenMap[current]?.forEach { childGuid ->
            if (allIds.add(childGuid)) {
                queue.add(childGuid)
            }
        }
    }

    return allIds.toList()
}

fun filterGroupCodes(): List<Double> {
    val perms = SharedPrefs.Permissions.get()
    val groupCodes = perms?.ConfigAGRP.parseToStringList()
    if (groupCodes.isEmpty()) return emptyList()
    return getSalemanPCFilter(groupCodes).mapNotNull { it.toDoubleOrNull() }
}

fun filterOneGroupCode(codes: List<String>): List<Double> {
    if (codes.isEmpty()) return emptyList()
    return getSalemanPCFilter(codes).mapNotNull { it.toDoubleOrNull() }
}

fun filterItemGroupCodes(): List<Double> {
    val perms = SharedPrefs.Permissions.get()
    val groupCodes = perms?.ConfigIGRP.parseToStringList()
    if (groupCodes.isEmpty()) return emptyList()
    return getProductsGroupCodesByGuid(groupCodes).mapNotNull { it.toDoubleOrNull() }
}

fun filterItemGroupCodesByName(): List<Double> {
    val perms = SharedPrefs.Permissions.get()
    val groupCodes = perms?.ConfigIGRP.parseToStringList()
    if (groupCodes.isEmpty()) return emptyList()
    return getProductsGroupCodesByGuid(groupCodes).mapNotNull { it.toDoubleOrNull() }
}
