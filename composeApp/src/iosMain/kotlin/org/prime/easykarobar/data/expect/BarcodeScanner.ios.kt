package org.prime.easykarobar.data.expect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import platform.AVFoundation.*
import platform.Foundation.NSSelectorFromString
import platform.UIKit.*
import platform.darwin.*

@Composable
actual fun rememberBarcodeScanner(
    onResult: (BarcodeScanResult) -> Unit
): BarcodeScannerLauncher {
    return remember {
        BarcodeScannerLauncher {
            val window = UIApplication.sharedApplication.windows.firstOrNull() as? UIWindow
            val rootViewController = window?.rootViewController ?: return@BarcodeScannerLauncher

            val scannerViewController = ScannerViewController(onResult)
            rootViewController.presentViewController(
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
    actual fun launch() = onLaunch()
}

@OptIn(ExperimentalForeignApi::class)
private class ScannerViewController(
    private val onResult: (BarcodeScanResult) -> Unit
) : UIViewController(null, null),
    AVCaptureMetadataOutputObjectsDelegateProtocol {

    private var captureSession: AVCaptureSession? = null
    private var previewLayer: AVCaptureVideoPreviewLayer? = null
    private var didEmitResult = false

    override fun viewDidLoad() {
        super.viewDidLoad()

        view.backgroundColor = UIColor.blackColor
        val session = AVCaptureSession()
        captureSession = session

        val device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
        if (device == null) {
            emitFailure()
            return
        }

        val input = runCatching {
            AVCaptureDeviceInput.deviceInputWithDevice(device, null)
        }.getOrNull()

        if (input == null || !session.canAddInput(input)) {
            emitFailure()
            return
        }

        session.addInput(input)

        val metadataOutput = AVCaptureMetadataOutput()
        if (!session.canAddOutput(metadataOutput)) {
            emitFailure()
            return
        }

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

        val preview = AVCaptureVideoPreviewLayer.layerWithSession(session)
        preview.frame = view.layer.bounds
        preview.videoGravity = AVLayerVideoGravityResizeAspectFill
        view.layer.addSublayer(preview)
        previewLayer = preview

        dispatch_async(
            dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), 0u)
        ) {
            session.startRunning()
        }

        setupUI()
    }

    private fun setupUI() {
        val cancelButton = UIButton.buttonWithType(UIButtonTypeSystem)
        cancelButton.setTitle("Cancel", forState = UIControlStateNormal)
        cancelButton.setTitleColor(UIColor.whiteColor, forState = UIControlStateNormal)
        cancelButton.backgroundColor =
            UIColor.blackColor.colorWithAlphaComponent(0.6)
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
                cancelButton.widthAnchor.constraintEqualToConstant(120.0),
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
        emitOnce(BarcodeScanResult.Cancelled)
    }

    override fun captureOutput(
        output: AVCaptureOutput,
        didOutputMetadataObjects: List<*>,
        fromConnection: AVCaptureConnection
    ) {
        val metadata =
            didOutputMetadataObjects.firstOrNull() as? AVMetadataMachineReadableCodeObject

        val value = metadata?.stringValue?.trim()
        if (!value.isNullOrEmpty()) {
            emitOnce(BarcodeScanResult.Success(value))
        }
    }

    private fun emitFailure() {
        emitOnce(BarcodeScanResult.Failure(null))
    }

    private fun emitOnce(result: BarcodeScanResult) {
        if (didEmitResult) return
        didEmitResult = true

        stopSession()
        dismissViewControllerAnimated(true) {
            onResult(result)
        }
    }

    private fun stopSession() {
        captureSession?.let {
            if (it.isRunning()) it.stopRunning()
        }
    }

    override fun viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()
        previewLayer?.frame = view.layer.bounds
    }

    override fun prefersStatusBarHidden(): Boolean = true
}
