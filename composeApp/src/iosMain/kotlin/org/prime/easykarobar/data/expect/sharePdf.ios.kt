package org.prime.easykarobar.data.expect

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UINavigationController

@OptIn(ExperimentalForeignApi::class)
actual fun sharePdf(filePath: String) {
    val fileURL = NSURL.fileURLWithPath(filePath)

    val activityViewController = UIActivityViewController(
        activityItems = listOf(fileURL),
        applicationActivities = null
    )

    var topViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
    while (topViewController?.presentedViewController != null) {
        topViewController = topViewController.presentedViewController
    }
    if (topViewController is UINavigationController) {
        topViewController = topViewController.topViewController
    }

    topViewController?.presentViewController(activityViewController, animated = true, completion = null)
}
