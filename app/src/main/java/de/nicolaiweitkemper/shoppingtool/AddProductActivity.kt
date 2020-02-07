package de.nicolaiweitkemper.shoppingtool

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.koushikdutta.ion.Ion
import kotlinx.android.synthetic.main.activity_add_product.*


class AddProductActivity : AppCompatActivity() {

    lateinit var barcode: String

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)

        barcode = intent.getStringExtra("barcode")!!
        barcodeView.text = "Barcode: $barcode"

        saveButton.setOnClickListener { commit() }
    }

    fun commit() {
        //saveButton.isEnabled = false

        val product = JsonObject()
        product.addProperty("name", nameInput.text.toString())
        val barcodes = JsonArray()
        barcodes.add(JsonPrimitive(barcode))
        product.add("barcodes", barcodes)

        Ion.getDefault(applicationContext).conscryptMiddleware.enable(false)

        Ion.with(applicationContext)
            .load("https://shopping-tool.herokuapp.com/addProduct")
            .setJsonObjectBody(product)
            .asJsonObject()
            .setCallback { e, result ->
                if (result === null) {
                    Log.w("err", e)
                } else {
                    Log.d("answer", result.toString())
                    if (result.get("success").asBoolean) {
                        startActivity(
                            Intent(
                                applicationContext,
                                ProductActivity::class.java
                            ).putExtra("barcode", barcode)
                        )
                        finish()
                    }
                }
            }
    }
}
