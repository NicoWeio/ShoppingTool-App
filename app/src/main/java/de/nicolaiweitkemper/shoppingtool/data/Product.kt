package de.nicolaiweitkemper.shoppingtool.data

import com.beust.klaxon.Json
import java.io.Serializable

class Product : Serializable {

    @Json(name = "_id")
    var id = "0"

    var name = ""

    var manufacturer = ""

    var barcodes = ArrayList<String>()

    var image = ""

    var thumbnail = ""

    var rating = 0f

    var tags = ArrayList<String>()

    var description = ""
}