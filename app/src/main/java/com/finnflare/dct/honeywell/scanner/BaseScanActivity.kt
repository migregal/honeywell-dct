package com.finnflare.dct.honeywell.scanner

import android.os.Bundle
import android.util.Log
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
//            it.setProperties(conf)

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
        Log.e("wtf", event.codeId + " : " + event.barcodeData)
        when (event.codeId) {
            EAN13 -> onScanResult(event.barcodeData, EAN13)
            CODE128 -> onScanResult(event.barcodeData, CODE128)
            GS1_128 -> onScanResult(event.barcodeData, GS1_128)
            DATAMATRIX -> onScanResult(event.barcodeData, DATAMATRIX)
            else -> return
        }
    }

    override fun onFailureEvent(event: BarcodeFailureEvent) {

    }

    override fun onTriggerEvent(event: TriggerStateChangeEvent) {

    }

    protected companion object {
        const val EAN13 = "d"
        const val CODE128 = "j"
        const val GS1_128 = "I"
        const val DATAMATRIX = "w"

        val conf = mapOf<String, Any>(
            BarcodeReader.PROPERTY_CODE_128_ENABLED to true,
            BarcodeReader.PROPERTY_DATAMATRIX_ENABLED to true,
            BarcodeReader.PROPERTY_EAN_13_ENABLED to true,
            BarcodeReader.PROPERTY_EAN_13_CHECK_DIGIT_TRANSMIT_ENABLED to true,
            BarcodeReader.PROPERTY_GS1_128_ENABLED to true,

            BarcodeReader.PROPERTY_ISBT_128_ENABLED to false,
            BarcodeReader.PROPERTY_GRIDMATRIX_ENABLED to false,
            BarcodeReader.PROPERTY_UPC_A_COUPON_CODE_MODE_ENABLED to false,
            BarcodeReader.PROPERTY_UPC_E_ENABLED to false,
            BarcodeReader.PROPERTY_UPC_E_E1_ENABLED to false,
            BarcodeReader.PROPERTY_EAN_8_ENABLED to false,
            BarcodeReader.PROPERTY_AZTEC_ENABLED to false,
            BarcodeReader.PROPERTY_CHINA_POST_ENABLED to false,
            BarcodeReader.PROPERTY_CODABAR_ENABLED to false,
            BarcodeReader.PROPERTY_QR_CODE_ENABLED to false,
            BarcodeReader.PROPERTY_CODABLOCK_A_ENABLED to false,
            BarcodeReader.PROPERTY_CODABLOCK_F_ENABLED to false,
            BarcodeReader.PROPERTY_CODE_11_ENABLED to false,
            BarcodeReader.PROPERTY_CODE_93_ENABLED to false,
            BarcodeReader.PROPERTY_COMPOSITE_ENABLED to false,
            BarcodeReader.PROPERTY_COMPOSITE_WITH_UPC_ENABLED to false,
            BarcodeReader.PROPERTY_DIGIMARC_ENABLED to false,
            BarcodeReader.PROPERTY_CODE_DOTCODE_ENABLED to false,
            BarcodeReader.PROPERTY_HAX_XIN_ENABLED to false,
            BarcodeReader.PROPERTY_IATA_25_ENABLED to false,
            BarcodeReader.PROPERTY_INTERLEAVED_25_ENABLED to false,
            BarcodeReader.PROPERTY_KOREAN_POST_ENABLED to false,
            BarcodeReader.PROPERTY_MATRIX_25_ENABLED to false,
            BarcodeReader.PROPERTY_MAXICODE_ENABLED to false,
            BarcodeReader.PROPERTY_MICRO_PDF_417_ENABLED to false,
            BarcodeReader.PROPERTY_MSI_ENABLED to false,
            BarcodeReader.PROPERTY_PDF_417_ENABLED to false,
            BarcodeReader.PROPERTY_QR_CODE_ENABLED to false,
            BarcodeReader.PROPERTY_RSS_ENABLED to false,
            BarcodeReader.PROPERTY_RSS_LIMITED_ENABLED to false,
            BarcodeReader.PROPERTY_RSS_EXPANDED_ENABLED to false,
            BarcodeReader.PROPERTY_STANDARD_25_ENABLED to false,
            BarcodeReader.PROPERTY_TELEPEN_ENABLED to false,
            BarcodeReader.PROPERTY_TELEPEN_OLD_STYLE_ENABLED to false,
            BarcodeReader.PROPERTY_TLC_39_ENABLED to false,
            BarcodeReader.PROPERTY_TRIOPTIC_ENABLED to false,

            BarcodeReader.PROPERTY_CENTER_DECODE to false,
            BarcodeReader.PROPERTY_NOTIFICATION_BAD_READ_ENABLED to false,
        )
    }
}