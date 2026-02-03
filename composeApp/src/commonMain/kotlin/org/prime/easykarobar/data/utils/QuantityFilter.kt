package org.prime.easykarobar.data.utils

fun showQtyToSalesman():Boolean{
    return if (SharedPrefs.Permissions.get()?.FilterQty == "False" || SharedPrefs.Permissions.get()?.FilterQty == null) true else false
}
fun showAmtToSalesman():Boolean{
    return if (SharedPrefs.Permissions.get()?.FilterAmount == "False" || SharedPrefs.Permissions.get()?.FilterAmount == null) true else false
}