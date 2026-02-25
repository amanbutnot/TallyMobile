package org.prime.easykarobar.data.utils

fun showQtyToSalesman(): Boolean {
    val permissions = SharedPrefs.Permissions.get()
    return if (permissions?.FilterMobile == "N" || permissions == null || SharedPrefs.Permissions.get()?.FilterQty == "False" || SharedPrefs.Permissions.get()?.FilterQty == null) true else false
}

fun showAmtToSalesman(): Boolean {
    val permissions = SharedPrefs.Permissions.get()
    return if (permissions?.FilterMobile == "N" || permissions == null || SharedPrefs.Permissions.get()?.FilterAmount == "False" || SharedPrefs.Permissions.get()?.FilterAmount == null) true else false
}