package org.prime.easykarobar.data.expect

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIDevice
import platform.UIKit.UIUserInterfaceIdiomPad
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.popoverPresentationController
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@OptIn(ExperimentalForeignApi::class)
actual fun sharePdf(filePath: String) {
    println("📤 [sharePdf] Called with path: $filePath")

    val fileManager = NSFileManager.defaultManager

    if (!fileManager.fileExistsAtPath(filePath)) {
        println("❌ [sharePdf] Aborting — file does not exist at: $filePath")
        return
    }

    val originalSize = fileManager.attributesOfItemAtPath(filePath, null)?.get("NSFileSize")
    println("📤 [sharePdf] Original file exists: true | Size: $originalSize bytes")

    // ✅ Copy from /tmp to Documents — share sheet cannot access /tmp on simulator or some devices
    val documentsDir = NSSearchPathForDirectoriesInDomains(
        NSDocumentDirectory, NSUserDomainMask, true
    ).firstOrNull() as? String

    val fileName = filePath.substringAfterLast("/")
    val sharePath = if (documentsDir != null) "$documentsDir/$fileName" else filePath
    println("📤 [sharePdf] Target share path: $sharePath")

    if (filePath != sharePath) {
        if (fileManager.fileExistsAtPath(sharePath)) {
            println("📤 [sharePdf] Removing existing file at share path")
            fileManager.removeItemAtPath(sharePath, null)
        }
        val copied = fileManager.copyItemAtPath(filePath, toPath = sharePath, error = null)
        println("📤 [sharePdf] Copy success: $copied")
    }

    val shareFileExists = fileManager.fileExistsAtPath(sharePath)
    val shareFileSize = fileManager.attributesOfItemAtPath(sharePath, null)?.get("NSFileSize")
    println("📤 [sharePdf] Share file exists: $shareFileExists | Size: $shareFileSize bytes")

    if (!shareFileExists) {
        println("❌ [sharePdf] Aborting — file copy failed, nothing to share")
        return
    }

    println("📤 [sharePdf] Dispatching to main queue...")
    dispatch_async(dispatch_get_main_queue()) {
        println("📤 [sharePdf] On main queue, building UIActivityViewController")

        val fileURL = NSURL.fileURLWithPath(sharePath)
        println("📤 [sharePdf] File URL: $fileURL")

        val activityViewController = UIActivityViewController(
            activityItems = listOf(fileURL),
            applicationActivities = null
        )

        println("📤 [sharePdf] Resolving key window via connectedScenes...")
        val window: UIWindow? = UIApplication.sharedApplication
            .connectedScenes
            .flatMap { scene ->
                (scene as? platform.UIKit.UIWindowScene)?.windows?.toList() ?: emptyList()
            }
            .filterIsInstance<UIWindow>()
            .firstOrNull { it.isKeyWindow() }
            ?: UIApplication.sharedApplication
                .connectedScenes
                .flatMap { scene ->
                    (scene as? platform.UIKit.UIWindowScene)?.windows?.toList() ?: emptyList()
                }
                .filterIsInstance<UIWindow>()
                .firstOrNull()

        println("📤 [sharePdf] Window resolved: $window")

        var topController: UIViewController? = window?.rootViewController
        println("📤 [sharePdf] Root controller: $topController")

        while (topController?.presentedViewController != null) {
            topController = topController?.presentedViewController
            println("📤 [sharePdf] Traversing to presented controller: $topController")
        }

        println("📤 [sharePdf] Top controller: $topController")

        if (UIDevice.currentDevice.userInterfaceIdiom == UIUserInterfaceIdiomPad) {
            println("📤 [sharePdf] iPad detected — configuring popover")
            activityViewController.popoverPresentationController?.apply {
                sourceView = topController?.view
                sourceRect = topController?.view?.bounds ?: CGRectZero.readValue()
                permittedArrowDirections = 0u
            }
        }

        if (topController == null) {
            println("❌ [sharePdf] topController is null — cannot present share sheet")
            return@dispatch_async
        }

        println("📤 [sharePdf] Presenting UIActivityViewController...")
        topController?.presentViewController(
            activityViewController,
            animated = true,
            completion = {
                println("✅ [sharePdf] Share sheet presented successfully")
            }
        )
    }
}