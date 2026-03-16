package org.prime.easykarobar.ui.shared.globalShared

fun googleMapsLink(lat: String, lon: String): String {
    return "https://www.google.com/maps/search/?api=1&query=$lat,$lon"
}