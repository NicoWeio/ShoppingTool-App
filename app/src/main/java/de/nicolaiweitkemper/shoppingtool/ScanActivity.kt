package de.nicolaiweitkemper.shoppingtool

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import me.dm7.barcodescanner.zbar.ZBarScannerView

class ScanActivity : AppCompatActivity() {

    //lateinit var scannerView: ZXingScannerView
    lateinit var scannerView: ZBarScannerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //scannerView = ZXingScannerView(this)
        scannerView = ZBarScannerView(this)
        setResultHandler()
//        scannerView.setResultHandler {
//            startActivity(
//                Intent(applicationContext, ProductActivity::class.java).putExtra(
//                    "barcode",
//                    //it.text
//                    it.contents
//                )
//            )
//            finish()
//        }
        setContentView(scannerView)
        scannerView.startCamera()
    }

    override fun onPause() {
        scannerView.stopCamera()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        setResultHandler()
//        scannerView.setResultHandler {
//            startActivity(
//                Intent(applicationContext, ProductActivity::class.java).putExtra(
//                    "barcode",
//                    //it.text
//                    it.contents
//                )
//            )
//            finish()
//        }
        scannerView.startCamera()
    }

    fun setResultHandler() {
        scannerView.setResultHandler {
            startActivity(
                Intent(applicationContext, ProductActivity::class.java).putExtra(
                    "barcode",
                    //it.text
                    it.contents
                )
            )
            finish()
        }
    }
}
