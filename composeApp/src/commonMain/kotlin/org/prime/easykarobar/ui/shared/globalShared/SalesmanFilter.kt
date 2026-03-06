package org.prime.easykarobar.ui.shared.globalShared

import org.prime.easykarobar.data.utils.SharedPrefs


val perms get() = SharedPrefs.Permissions.get()

fun filterItemGroups(): Long {
    return if (perms?.FilterIGRP == "Y") 1L else 0L
}

fun itemGroupCodes(): List<Double> {
    return filterItemGroupCodes()
}
fun filterAGRPGroups(): Long {
    return if (perms?.FilterAGRP == "Y") 1L else 0L
}

fun agrpGroupCodes(): List<String> {
    return filterGroupCodes().map { it.toString() }
}
fun filterAccountGroups(): Long {
    return if (perms?.FilterAccounts == "Y") 1L else 0L
}

fun accountGroupCodes(): List<String> {
    return perms?.ConfigAccounts.parseToStringList()
}
fun filterBroker(): Long {
    return if (perms?.FilterBroker == "Y") 1L else 0L
}

fun configBroker(): List<String> {
    return perms?.ConfigBroker.parseToStringList()
}