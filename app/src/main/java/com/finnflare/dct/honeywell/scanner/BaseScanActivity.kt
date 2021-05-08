package com.finnflare.dct.honeywell.scanner

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.finnflare.dct.honeywell.R
import com.honeywell.aidc.*


open class BaseScannerActivity : AppCompatActivity(),
    BarcodeReader.BarcodeListener,
    BarcodeReader.TriggerListener {

    private var manager: AidcManager? = null
    private var barcodeReader: BarcodeReader? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AidcManager.create(this) { aidcManager ->
            manager = aidcManager
            try {
                barcodeReader = manager?.createBarcodeReader()
                configureBarcodeReader()
            } catch (e: InvalidScannerNameException) {
                Toast.makeText(
                    this,
                    getString(R.string.toast_msg_scanner_name_exception) + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    this,
                    getString(R.string.toast_msg_exception) + e.message,
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        barcodeReader?.let {
            it.release()
            try {
                it.claim()
            } catch (e: ScannerUnavailableException) {
                e.printStackTrace()
                Toast.makeText(
                    this,
                    getString(R.string.toast_msg_scanner_unavailable),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        barcodeReader?.release()
    }

    private fun configureBarcodeReader() {
        barcodeReader?.let {
            it.addBarcodeListener(this)
            try {
                it.setProperty(
                    BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE,
                    BarcodeReader.TRIGGER_CONTROL_MODE_AUTO_CONTROL
                )
            } catch (e: UnsupportedPropertyException) {
                Toast.makeText(
                    this,
                    getString(R.string.toast_msg_scanner_properties_error),
                    Toast.LENGTH_SHORT
                ).show()
            }

            it.addTriggerListener(this)
            it.setProperties(
                mutableMapOf<String, Any>(
                    BarcodeReader.PROPERTY_CODE_128_ENABLED to true,
                    BarcodeReader.PROPERTY_EAN_13_ENABLED to true,
                    BarcodeReader.PROPERTY_GS1_128_ENABLED to false,
                    BarcodeReader.PROPERTY_QR_CODE_ENABLED to false,
                    BarcodeReader.PROPERTY_CODE_39_ENABLED to false,
                    BarcodeReader.PROPERTY_UPC_A_ENABLE to false,
                    BarcodeReader.PROPERTY_AZTEC_ENABLED to false,
                    BarcodeReader.PROPERTY_CODABAR_ENABLED to false,
                    BarcodeReader.PROPERTY_INTERLEAVED_25_ENABLED to false,
                    BarcodeReader.PROPERTY_PDF_417_ENABLED to false,
                    BarcodeReader.PROPERTY_CENTER_DECODE to true,
                    BarcodeReader.PROPERTY_NOTIFICATION_BAD_READ_ENABLED to false
                )
            )

            try {
                it.claim()
            } catch (e: ScannerUnavailableException) {
                e.printStackTrace()
                Toast.makeText(
                    this,
                    getString(R.string.toast_msg_scanner_unavailable),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    open fun onScanResult(data: String, code: String) {}

    override fun onBarcodeEvent(event: BarcodeReadEvent) {
        onScanResult(event.barcodeData, event.codeId)
    }

    override fun onFailureEvent(event: BarcodeFailureEvent) {

    }

    override fun onTriggerEvent(event: TriggerStateChangeEvent) {

    }
}