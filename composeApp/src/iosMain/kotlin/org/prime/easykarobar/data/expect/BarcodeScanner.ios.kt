package org.prime.easykarobar.data.expect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureMetadataOutput
import platform.AVFoundation.AVCaptureMetadataOutputObjectsDelegateProtocol
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.AVMetadataMachineReadableCodeObject
import platform.AVFoundation.AVMetadataObjectTypeAztecCode
import platform.AVFoundation.AVMetadataObjectTypeCode128Code
import platform.AVFoundation.AVMetadataObjectTypeCode39Code
import platform.AVFoundation.AVMetadataObjectTypeCode93Code
import platform.AVFoundation.AVMetadataObjectTypeEAN13Code
import platform.AVFoundation.AVMetadataObjectTypeEAN8Code
import platform.AVFoundation.AVMetadataObjectTypePDF417Code
import platform.AVFoundation.AVMetadataObjectTypeQRCode
import platform.AVFoundation.AVMetadataObjectTypeUPCECode
import platform.Foundation.NSSelectorFromString
import platform.UIKit.NSLayoutConstraint
import platform.UIKit.UIApplication
import platform.UIKit.UIButton
import platform.UIKit.UIButtonTypeSystem
import platform.UIKit.UIColor
import platform.UIKit.UIControlEventTouchUpInside
import platform.UIKit.UIControlStateNormal
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.darwin.DISPATCH_QUEUE_PRIORITY_DEFAULT
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_global_queue
import platform.darwin.dispatch_get_main_queue

@Composable
actual fun rememberBarcodeScanner(onResult: (String?) -> Unit): BarcodeScannerLauncher {
    return remember {
        BarcodeScannerLauncher {
            val window = UIApplication.sharedApplication.windows.first() as? UIWindow
            val rootViewController = window?.rootViewController

            val scannerViewController = ScannerViewController(onResult)
            rootViewController?.presentViewController(
                scannerViewController,
                animated = true,
                completion = null
            )
        }
    }
}

actual class BarcodeScannerLauncher(
    private val onLaunch: () -> Unit
) {
    actual fun launch() {
        onLaunch()
    }
}

@OptIn(ExperimentalForeignApi::class)
private class ScannerViewController(
    private val onResult: (String?) -> Unit
) : UIViewController(null, null), AVCaptureMetadataOutputObjectsDelegateProtocol {

    private var captureSession: AVCaptureSession? = null
    private var previewLayer: AVCaptureVideoPreviewLayer? = null

    override fun viewDidLoad() {
        super.viewDidLoad()

        view.backgroundColor = UIColor.blackColor
        val session = AVCaptureSession()
        captureSession = session

        val videoCaptureDevice = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
        if (videoCaptureDevice == null) {
            onResult(null)
            return
        }

        val videoInput = try {
            AVCaptureDeviceInput.deviceInputWithDevice(
                videoCaptureDevice,
                null
            ) as? AVCaptureDeviceInput
        } catch (e: Exception) {
            null
        }

        if (videoInput != null && session.canAddInput(videoInput)) {
            session.addInput(videoInput)
        } else {
            onResult(null)
            return
        }

        val metadataOutput = AVCaptureMetadataOutput()

        if (session.canAddOutput(metadataOutput)) {
            session.addOutput(metadataOutput)

            metadataOutput.setMetadataObjectsDelegate(this, dispatch_get_main_queue())
            metadataOutput.metadataObjectTypes = listOf(
                AVMetadataObjectTypeQRCode,
                AVMetadataObjectTypeEAN13Code,
                AVMetadataObjectTypeEAN8Code,
                AVMetadataObjectTypeCode128Code,
                AVMetadataObjectTypeCode39Code,
                AVMetadataObjectTypeCode93Code,
                AVMetadataObjectTypeUPCECode,
                AVMetadataObjectTypePDF417Code,
                AVMetadataObjectTypeAztecCode
            )
        } else {
            onResult(null)
            return
        }

        val preview = AVCaptureVideoPreviewLayer.layerWithSession(session)
        preview.frame = view.layer.bounds
        preview.videoGravity = AVLayerVideoGravityResizeAspectFill
        view.layer.addSublayer(preview)
        previewLayer = preview

        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), 0u)) {
            session.startRunning()
        }

        setupUI()
    }

    private fun setupUI() {
        val cancelButton = UIButton.buttonWithType(UIButtonTypeSystem)
        cancelButton.setTitle("Cancel", forState = UIControlStateNormal)
        cancelButton.setTitleColor(UIColor.whiteColor, forState = UIControlStateNormal)
        cancelButton.backgroundColor = UIColor.blackColor.colorWithAlphaComponent(0.5)
        cancelButton.layer.cornerRadius = 8.0

        cancelButton.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(cancelButton)

        NSLayoutConstraint.activateConstraints(
            listOf(
                cancelButton.bottomAnchor.constraintEqualToAnchor(
                    view.safeAreaLayoutGuide.bottomAnchor,
                    -32.0
                ),
                cancelButton.centerXAnchor.constraintEqualToAnchor(view.centerXAnchor),
                cancelButton.widthAnchor.constraintEqualToConstant(100.0),
                cancelButton.heightAnchor.constraintEqualToConstant(44.0)
            )
        )

        cancelButton.addTarget(
            this,
            NSSelectorFromString("cancelPressed"),
            UIControlEventTouchUpInside
        )
    }

    @ObjCAction
    fun cancelPressed() {
        stopSession()
        dismissViewControllerAnimated(true) {
            onResult(null)
        }
    }

    private fun stopSession() {
        captureSession?.let {
            if (it.isRunning()) {
                it.stopRunning()
            }
        }
    }

    override fun captureOutput(
        output: AVCaptureOutput,
        didOutputMetadataObjects: List<*>,
        fromConnection: AVCaptureConnection
    ) {
        val metadataObject =
            didOutputMetadataObjects.firstOrNull() as? AVMetadataMachineReadableCodeObject
        if (metadataObject != null) {
            val stringValue = metadataObject.stringValue
            if (stringValue != null) {
                stopSession()
                dismissViewControllerAnimated(true) {
                    onResult(stringValue)
                }
            }
        }
    }

    override fun viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()
        previewLayer?.frame = view.layer.bounds
    }

    override fun prefersStatusBarHidden(): Boolean = true
}