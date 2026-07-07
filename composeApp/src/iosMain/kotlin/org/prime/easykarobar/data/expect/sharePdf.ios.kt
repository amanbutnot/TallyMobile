package org.prime.easykarobar.data.expect

import androidx.compose.runtime.Composable
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSFilePosixPermissions
import platform.Foundation.NSFileProtectionKey
import platform.Foundation.NSFileProtectionNone
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.writeToFile
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIDevice
import platform.UIKit.UIPrintInfo
import platform.UIKit.UIPrintInteractionController
import platform.UIKit.UISceneActivationStateForegroundActive
import platform.UIKit.UIUserInterfaceIdiomPad
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UIKit.popoverPresentationController
import platform.darwin.DISPATCH_TIME_NOW
import platform.darwin.dispatch_after
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_time
import platform.posix.chmod

@OptIn(ExperimentalForeignApi::class)
actual fun sharePdf(filePath: String) {
    println("📤 [sharePdf] Called with path: $filePath")

    val fileManager = NSFileManager.defaultManager

    if (!fileManager.fileExistsAtPath(filePath)) {
        println("❌ [sharePdf] Aborting — file does not exist at: $filePath")
        return
    }

    val originalSize = fileManager.attributesOfItemAtPath(filePath, null)?.get("NSFileSize")
    println("📤 [sharePdf] Original file size: $originalSize bytes")

    val shareDir = NSSearchPathForDirectoriesInDomains(
        NSCachesDirectory, NSUserDomainMask, true
    ).firstOrNull() as? String

    val fileName = filePath.substringAfterLast("/")
    val sharePath = if (shareDir != null) "$shareDir/shared_$fileName" else filePath
    println("📤 [sharePdf] Target share path: $sharePath")

    if (filePath != sharePath) {
        if (fileManager.fileExistsAtPath(sharePath)) {
            println("📤 [sharePdf] Removing existing file at share path")
            fileManager.removeItemAtPath(sharePath, null)
        }

        val data = NSData.dataWithContentsOfFile(filePath)
        if (data == null) {
            println("❌ [sharePdf] Aborting — could not read source file into NSData")
            return
        }
        val written = data.writeToFile(sharePath, atomically = true)
        println("📤 [sharePdf] Write success: $written")

        if (!written) {
            println("❌ [sharePdf] Aborting — NSData writeToFile failed")
            return
        }
    }

    val attributes: Map<Any?, *> = mapOf(
        NSFilePosixPermissions to 420,
        NSFileProtectionKey to NSFileProtectionNone
    )
    fileManager.setAttributes(attributes, sharePath, null)
    chmod(sharePath, 420u)

    val verifyAttr = fileManager.attributesOfItemAtPath(sharePath, null)
    println(
        "📤 [sharePdf] Attributes set. Permissions: ${verifyAttr?.get(NSFilePosixPermissions)}, Protection: ${
            verifyAttr?.get(
                NSFileProtectionKey
            )
        }"
    )

    if (!fileManager.fileExistsAtPath(sharePath)) {
        println("❌ [sharePdf] Aborting — share file missing after write")
        return
    }

    // ✅ CHANGE 1: Use dispatch_after with 300ms delay instead of dispatch_async.
    // This ensures Compose has finished recomposing (e.g. after loading = false)
    // before we try to present. Presenting during a recompose is silently dropped by iOS.
    println("📤 [sharePdf] Scheduling presentation with 300ms delay...")
    dispatch_after(
        dispatch_time(DISPATCH_TIME_NOW, 300_000_000L), // 300ms in nanoseconds
        dispatch_get_main_queue()
    ) {
        println("📤 [sharePdf] On main queue (delayed), building UIActivityViewController")

        val fileURL = NSURL.fileURLWithPath(sharePath, isDirectory = false)
        println("📤 [sharePdf] File URL: $fileURL")

        val activityViewController = UIActivityViewController(
            activityItems = listOf(fileURL),
            applicationActivities = null
        )

        // ✅ CHANGE 2: Prefer the foreground-active scene over just any key window.
        // Compose's root view controller can silently block presentation — getting
        // the scene that is actually in the foreground is more reliable.
        println("📤 [sharePdf] Resolving foreground active window scene...")
        val windowScene = UIApplication.sharedApplication
            .connectedScenes
            .filterIsInstance<UIWindowScene>()
            .firstOrNull { it.activationState == UISceneActivationStateForegroundActive }
            ?: UIApplication.sharedApplication
                .connectedScenes
                .filterIsInstance<UIWindowScene>()
                .firstOrNull()

        if (windowScene == null) {
            println("❌ [sharePdf] No window scene found — aborting")
            return@dispatch_after
        }

        val window: UIWindow? = windowScene.windows
            .filterIsInstance<UIWindow>()
            .firstOrNull { it.isKeyWindow() }
            ?: windowScene.windows.filterIsInstance<UIWindow>().firstOrNull()

        println("📤 [sharePdf] Window resolved: $window")

        var topController: UIViewController? = window?.rootViewController
        println("📤 [sharePdf] Root controller: $topController")

        while (topController?.presentedViewController != null) {
            topController = topController?.presentedViewController
            println("📤 [sharePdf] Traversing to presented controller: $topController")
        }

        println("📤 [sharePdf] Top controller: $topController")

        if (topController == null) {
            println("❌ [sharePdf] topController is null — cannot present share sheet")
            return@dispatch_after
        }

        if (UIDevice.currentDevice.userInterfaceIdiom == UIUserInterfaceIdiomPad) {
            println("📤 [sharePdf] iPad detected — configuring popover")
            activityViewController.popoverPresentationController?.apply {
                sourceView = topController.view
                sourceRect = topController.view?.bounds ?: CGRectZero.readValue()
                permittedArrowDirections = 0u
            }
        }

        println("📤 [sharePdf] Presenting UIActivityViewController...")
        topController.presentViewController(
            activityViewController,
            animated = true,
            completion = {
                println("✅ [sharePdf] Share sheet presented successfully")
            }
        )
    }
}

actual fun printPdf(filePath: String) {
    val fileURL = NSURL.fileURLWithPath(filePath, isDirectory = false)
    val printController = UIPrintInteractionController.sharedPrintController()
    val printInfo = UIPrintInfo.printInfoWithDictionary(null)
    printInfo.outputType = platform.UIKit.UIPrintInfoOutputGeneral
    printInfo.jobName = filePath.substringAfterLast("/")
    printController.printInfo = printInfo
    printController.printingItem = fileURL

    val windowScene = UIApplication.sharedApplication
        .connectedScenes
        .filterIsInstance<UIWindowScene>()
        .firstOrNull { it.activationState == UISceneActivationStateForegroundActive }
        ?: UIApplication.sharedApplication
            .connectedScenes
            .filterIsInstance<UIWindowScene>()
            .firstOrNull()

    val window: UIWindow? = windowScene?.windows
        ?.filterIsInstance<UIWindow>()
        ?.firstOrNull { it.isKeyWindow() }
        ?: windowScene?.windows?.filterIsInstance<UIWindow>()?.firstOrNull()

    var topController: UIViewController? = window?.rootViewController
    while (topController?.presentedViewController != null) {
        topController = topController?.presentedViewController
    }

    topController?.let {
        printController.presentAnimated(true, completionHandler = null)
    }
}

@Composable
actual fun shareText(text: String) {
    val activityVC = UIActivityViewController(
        activityItems = listOf(text),
        applicationActivities = null
    )

    val rootVC = UIApplication.sharedApplication
        .keyWindow
        ?.rootViewController

    rootVC?.presentViewController(
        activityVC,
        animated = true,
        completion = null
    )
}