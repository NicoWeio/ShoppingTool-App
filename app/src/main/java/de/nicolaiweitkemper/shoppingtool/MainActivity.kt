package de.nicolaiweitkemper.shoppingtool

import android.app.Activity
import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.beust.klaxon.Klaxon
import com.google.android.material.snackbar.Snackbar
import com.google.gson.JsonObject
import com.koushikdutta.ion.Ion
import de.nicolaiweitkemper.shoppingtool.data.Product
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {

    var listItems = ArrayList<Product>()

    var query: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        if (Intent.ACTION_SEARCH == intent.action) {
            intent.getStringExtra(SearchManager.QUERY)?.also { q ->
                query = q.trim().toLowerCase()

                SearchRecentSuggestions(
                    this,
                    MySuggestionProvider.AUTHORITY,
                    MySuggestionProvider.MODE
                )
                    .saveRecentQuery(query, null)

                supportActionBar?.title = "Suche: $query"
            }
        }

        fab.setOnClickListener {
            val integrator = BarcodeIntentIntegrator(this)
//            integrator.initiateScan(listOf("PRODUCT_MODE"))
            integrator.initiateScan()
        }

        fab.setOnLongClickListener {
            startActivity(Intent(applicationContext, ScanActivity::class.java))
            false
        }

        swiperefresh.setOnRefreshListener { load() }

        load()

        coursesList.layoutManager = LinearLayoutManager(this)
        coursesList.adapter = ProductListAdapter(listItems, this, R.layout.product_list_item)
    }

    fun load() {
        /*if (swiperefresh.isRefreshing) {
            Log.w("kak", "already refreshing")
            return
        }*/

        swiperefresh.isRefreshing = true

        val json = JsonObject()
        if (query?.isNotEmpty() == true) json.addProperty("query", query)

        Ion.getDefault(applicationContext).conscryptMiddleware.enable(false)

        Ion.with(applicationContext)
            //.load("http://192.168.178.45:8080/getProducts")
            //.load("https://shopping-tool.herokuapp.com/getProducts")
            //.load("http://192.168.178.45:8080/" + (if (query?.isNotEmpty() == true) "search" else "getProducts"))
            .load("https://shopping-tool.herokuapp.com/" + (if (query?.isNotEmpty() == true) "search" else "getProducts"))
            .setJsonObjectBody(json)
            .asJsonArray()
            .setCallback { e, result ->
                if (result === null) {
                    e.printStackTrace()
                    runOnUiThread {
                        swiperefresh.isRefreshing = false

                        AlertDialog.Builder(this)
                            .setTitle("Fehler beim Laden")
                            .setMessage(e.toString())
                            .show()
                    }
                } else {
                    Log.d("answer", result.toString())
                    thread {
                        listItems.clear()
                        for (productIter in result.asIterable()) {
                            val product = Klaxon().parse<Product>(productIter.toString())
                            listItems.add(product!!)
                        }

                        runOnUiThread {
                            coursesList.adapter!!.notifyDataSetChanged()
                            swiperefresh.isRefreshing = false
                        }
                    }
                }
            }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            R.id.search -> onSearchRequested()
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        val scanResult =
            BarcodeIntentIntegrator.parseActivityResult(requestCode, resultCode, intent)
        if (resultCode == Activity.RESULT_OK && scanResult != null) {
            Snackbar.make(fab, scanResult.contents, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()

            startActivity(
                Intent(applicationContext, ProductActivity::class.java).putExtra(
                    "barcode",
                    scanResult.contents
                )
            )
        }
        // else continue with any other code you need in the method
    }
}
