package org.prime.easykarobar.data.expect

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual fun callPhone(number: String) {
    val url = NSURL.URLWithString("tel:+91$number") ?: return

    if (UIApplication.sharedApplication.canOpenURL(url)) {
        UIApplication.sharedApplication.openURL(url)
    }
}