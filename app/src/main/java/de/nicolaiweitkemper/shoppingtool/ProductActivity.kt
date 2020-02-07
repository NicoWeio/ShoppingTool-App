package de.nicolaiweitkemper.shoppingtool

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.beust.klaxon.Klaxon
import com.google.android.material.chip.Chip
import com.google.gson.JsonObject
import com.koushikdutta.ion.Ion
import com.squareup.picasso.Picasso
import de.nicolaiweitkemper.shoppingtool.data.Product
import kotlinx.android.synthetic.main.activity_product.*
import kotlin.concurrent.thread


class ProductActivity : AppCompatActivity() {

    lateinit var product: Product
    lateinit var barcode: String
    lateinit var id: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        barcode = intent.getStringExtra("barcode") ?: ""
        id = intent.getStringExtra("id") ?: ""

        load()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_product_view, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.edit -> if (::product.isInitialized) {
                startActivity(
                    Intent(
                        applicationContext,
                        EditProductActivity::class.java
                    ).putExtra("product", product)
                )
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun load() {
        val json = JsonObject()
        if (barcode.isNotEmpty()) json.addProperty("barcode", barcode)
        if (id.isNotEmpty()) json.addProperty("id", id)

        Ion.getDefault(applicationContext).conscryptMiddleware.enable(false)

        Ion.with(applicationContext)
            .load("https://shopping-tool.herokuapp.com/getProduct")
            .setJsonObjectBody(json)
            .asJsonObject()
            .setCallback { e, result ->
                if (result === null) {
                    Log.w("err", e)
                    startActivity(
                        Intent(applicationContext, AddProductActivity::class.java).putExtra(
                            "barcode", barcode
                        )
                    )
                    finish()
                } else {
                    thread {
                        Log.d("answer", result.toString())
                        product = Klaxon().parse<Product>(result.toString())!!
                        id = product.id
                        runOnUiThread { display() }
                    }
                }
            }
    }

    fun display() {
        nameView.text = product.name
        manufacturerView.text =
            if (product.manufacturer.isNotEmpty()) product.manufacturer else "unbekannter Hersteller"
        descriptionView.text =
            if (product.description.isNotEmpty()) product.description else "keine Beschreibung"
        ratingBar.rating = product.rating

        val sb = StringBuilder()
        for (s in product.barcodes) {
            sb.append(s)
            sb.append("\t")
        }
        barcodesView.text = "Barcode(s): $sb"

        Picasso.get()
            .load(if (product.image.isNotEmpty()) product.image else "stub://stub.stub")
            .placeholder(R.drawable.image_placeholder)
            .error(R.drawable.image_placeholder)
            .into(imageView)

        tagView.removeAllViews()
        product.tags.forEach { tag ->
            val chip = LayoutInflater.from(this@ProductActivity).inflate(
                R.layout.chip,
                tagView,
                false
            ) as Chip
            chip.text = tag
            chip.isCloseIconVisible = false
            tagView.addView(chip)
        }
    }

}
