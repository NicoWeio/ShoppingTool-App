package de.nicolaiweitkemper.shoppingtool

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import com.google.android.material.chip.Chip
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.koushikdutta.ion.Ion
import com.squareup.picasso.Picasso
import com.yalantis.ucrop.UCrop
import de.nicolaiweitkemper.shoppingtool.data.Product
import kotlinx.android.synthetic.main.activity_edit_product.*
import java.io.File


class EditProductActivity : AppCompatActivity() {

    lateinit var product: Product
    lateinit var currentTags: ArrayList<String>

    private val TAKE_PICTURE = 1
    private var imageFileUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_product)

        abortButton.setOnClickListener {
            onAbort()
        }
        saveButton.setOnClickListener { commit() }

        imageView.setOnClickListener { openCamera() }

        /*ratingBar.setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                Log.d("TouchTest", "Touch down")
            } else if (event.action == MotionEvent.ACTION_UP) {
                Log.d("TouchTest", "Touch up")

                Log.d("Rating", "changed")
                val changes = JsonObject()
                changes.addProperty("rating", ratingBar.rating)
                modifyProduct(changes)

            }
            true
        }*/

        newTagEdit.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                currentTags.add(newTagEdit.text.toString())
                newTagEdit.text.clear()
                updateTagsView()
                true
            }
            false
        }

        product = intent.getSerializableExtra("product") as Product
        currentTags = product.tags.clone() as ArrayList<String>

        display()
    }

    fun getChanges(): JsonObject {
        val changes = JsonObject()
        if (nameEdit.text.toString() != product.name)
            changes.addProperty("name", nameEdit.text.toString().trim())
        if (manufacturerEdit.text.toString() != product.manufacturer)
            changes.addProperty("manufacturer", manufacturerEdit.text.toString().trim())
        if (descriptionEdit.text.toString() != product.description)
            changes.addProperty("description", descriptionEdit.text.toString().trim())
        if (ratingBar.rating != product.rating)
            changes.addProperty("rating", ratingBar.rating)
        if (currentTags != product.tags)
            changes.add("tags", GsonBuilder().create().toJsonTree(currentTags))
        return changes
    }

    fun commit() {
        val json = JsonObject()
        json.addProperty("id", product.id)
        val changes = getChanges()
        json.add("changes", changes)

        Ion.with(applicationContext)
            .load("https://shopping-tool.herokuapp.com/modifyProduct")
            .setJsonObjectBody(json)
            .asJsonObject()
            .setCallback { e, result ->
                if (result === null) {
                    Log.w("err", e)
                } else {
                    Log.d("answer", result.toString())
                    if (result.get("success").asBoolean) {
                        Toast.makeText(
                            applicationContext,
                            "Änderungen übernommen",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }
            }
    }

    fun display() {
        nameEdit.setText(product.name)
        manufacturerEdit.setText(product.manufacturer)
        ratingBar.rating = product.rating
        updateTagsView()
        Picasso.get()
            .load(if (product.image.isNotEmpty()) product.image else "stub://stub.stub")
            .placeholder(R.drawable.image_placeholder)
            .error(R.drawable.image_placeholder)
            .into(imageView)
    }

    fun updateTagsView() {

        /*val chips = ArrayList<Chip>()
        chips.add(Chip("Ulrike Bethmann", null))
        //DRAWABLE!!.setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP)
        //!!.setTint(Color.RED)
        chips.add(Chip(ContextCompat.getDrawable(applicationContext, R.drawable.ic_edit), "vegan", null))
        chips_input.filterableList = chips*/

        /*var spannedLength = 0
        val chipLength = 4

        phone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (charSequence.length == spannedLength - chipLength) {
                    spannedLength = charSequence.length
                }
            }

            override fun afterTextChanged(editable: Editable) {

                if (editable.length - spannedLength == chipLength) {
                    val chip = ChipDrawable.createFromResource(this@EditProductActivity, R.xml.chip)
                    chip.setText(editable.subSequence(spannedLength, editable.length))
                    chip.setBounds(0, 0, chip.intrinsicWidth, chip.intrinsicHeight)
                    val span = ImageSpan(chip)
                    editable.setSpan(span, spannedLength, editable.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    spannedLength = editable.length
                    Log.d("PIMMEL!", "KAKAAAAAAAAAAAAA")
                }

            }
        })*/

        //val chip = Chip(tagView.context)
        //chip.text = "Name Surname"
        //chip.chipIcon = ContextCompat.getDrawable(this@EditProductActivity, R.drawable.ic_edit)
        //chip.isCloseIconEnabled = true
        //chip.setTextAppearance()
        //chip.styl


        tagView.removeAllViews()
        currentTags.forEach { tag ->
            val chip = LayoutInflater.from(this@EditProductActivity).inflate(
                R.layout.chip,
                tagView,
                false
            ) as Chip
            chip.text = tag
            chip.setOnCloseIconClickListener { view ->
                val name = (view as Chip).text
                currentTags.remove(name)
                display()
            }
            tagView.addView(chip)
        }
    }

    fun openCamera() {
        val photoFile = File(cacheDir, product.id + ".jpg")
        imageFileUri = Uri.fromFile(photoFile)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageFileUri)
        } else {
            val file = File(imageFileUri!!.path!!)
            val externalImageUri =
                FileProvider.getUriForFile(
                    applicationContext,
                    applicationContext.packageName + ".provider",
                    file
                )
            intent.putExtra(MediaStore.EXTRA_OUTPUT, externalImageUri)
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        if (intent.resolveActivity(applicationContext.packageManager) != null) {
            startActivityForResult(intent, TAKE_PICTURE)
        }
    }

    fun startCrop() {
        UCrop.of(imageFileUri!!, imageFileUri!!)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(2000, 2000)
            .start(this)
    }

    fun uploadImage() {
        Log.d("Upload", "Starte!")
        Ion.with(applicationContext)
            .load("https://shopping-tool.herokuapp.com/uploadImage")
            .setMultipartFile("file", imageFileUri!!.toFile())
            .asJsonObject()
            .setCallback { e, result ->
                if (result === null) {
                    Log.w("err", e)
                } else {
                    Log.d("answer", result.toString())
                    addImage(result.get("imageUrl").asString, result.get("thumbnailUrl").asString)
                }
            }
    }

    fun addImage(url: String, thumbnailUrl: String) {
        val changes = JsonObject()
        changes.addProperty("image", url)
        changes.addProperty("thumbnail", thumbnailUrl)
        modifyProduct(changes)
    }

    fun modifyProduct(changes: JsonObject) {
        val json = JsonObject()
        json.addProperty("id", product.id)
        json.add("changes", changes)

        Ion.with(applicationContext)
            .load("https://shopping-tool.herokuapp.com/modifyProduct")
            .setJsonObjectBody(json)
            .asJsonObject()
            .setCallback { e, result ->
                if (result === null) {
                    Log.w("err", e)
                } else {
                    Log.d("answer", result.toString())
                    if (result.get("success").asBoolean) {
                        Toast.makeText(
                            applicationContext,
                            "Änderungen übernommen",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            TAKE_PICTURE -> if (resultCode == Activity.RESULT_OK) {
                try {
                    startCrop()
                } catch (e: Exception) {
                    Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT)
                        .show()
                    Log.e("Camera", e.toString())
                }

            }
            UCrop.REQUEST_CROP -> if (resultCode == Activity.RESULT_OK) {
                Picasso.get()
                    .load(imageFileUri)
                    .into(imageView)

                imageFileUri = UCrop.getOutput(data!!)
                uploadImage()
            }
        }
    }

    fun onAbort() {
        if (getChanges().entrySet().size > 0) {
            AlertDialog.Builder(this)
                .setTitle("Änderungen verwerfen?")
                .setMessage("Wirklich beenden, ohne zu speichern?")
                .setPositiveButton("verwerfen") { _, _ -> finish() }
                .setNegativeButton("weiter bearbeiten", null)
                .show()
        } else {
            finish()
        }
    }

    override fun onBackPressed() {
        onAbort()
    }
}
