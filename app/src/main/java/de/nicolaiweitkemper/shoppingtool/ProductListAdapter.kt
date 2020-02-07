package de.nicolaiweitkemper.shoppingtool

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import de.nicolaiweitkemper.shoppingtool.data.Product
import kotlinx.android.synthetic.main.product_list_item.view.*

internal class ProductListAdapter(
    private val arrayList: ArrayList<Product>,
    private val context: Context,
    private val layout: Int
) : RecyclerView.Adapter<ProductListAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(arrayList[position])
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    internal inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(item: Product) {
            itemView.nameView.text = item.name
            itemView.manufacturerView.text = item.manufacturer
            itemView.ratingBar.rating = item.rating

            Picasso.get()
                .load(if (item.thumbnail.isNotEmpty()) item.thumbnail else "stub://stub.stub")
                .placeholder(R.drawable.image_placeholder)
                .error(R.drawable.image_placeholder)
                .into(itemView.imageView)

            itemView.cardView.setOnClickListener {
                val intent = Intent(context, ProductActivity::class.java)
                intent.putExtra("id", item.id)
                context.startActivity(intent)
            }
        }
    }
}
