package com.example.myapplication.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.Product
import com.example.myapplication.util.loadStoreImage
import com.example.myapplication.util.toEuroString

class FeaturedProductAdapter(
    private var items: List<Product>,
    private val onOpen: (Product) -> Unit,
    private val onAdd: (Product) -> Unit,
    private val onToggleFavorite: (Product) -> Unit,
) : RecyclerView.Adapter<FeaturedProductAdapter.FeaturedViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeaturedViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_featured_product, parent, false)
        return FeaturedViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeaturedViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun update(newItems: List<Product>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class FeaturedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val image = itemView.findViewById<ImageView>(R.id.ivFeaturedProduct)
        private val title = itemView.findViewById<TextView>(R.id.tvFeaturedProductTitle)
        private val meta = itemView.findViewById<TextView>(R.id.tvFeaturedProductMeta)
        private val price = itemView.findViewById<TextView>(R.id.tvFeaturedProductPrice)
        private val addButton = itemView.findViewById<Button>(R.id.btnFeaturedAdd)
        private val favoriteButton = itemView.findViewById<ImageButton>(R.id.btnFeaturedFavorite)

        fun bind(product: Product) {
            image.loadStoreImage(product.imageUri)
            title.text = product.title
            meta.text = itemView.context.getString(R.string.product_meta_format, product.platform, product.category)
            price.text = product.price.toEuroString()
            favoriteButton.setImageResource(
                if (product.isFavorite) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline,
            )
            itemView.setOnClickListener { onOpen(product) }
            addButton.setOnClickListener { onAdd(product) }
            favoriteButton.setOnClickListener { onToggleFavorite(product) }
        }
    }
}

