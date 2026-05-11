package com.example.myapplication.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.Product
import com.example.myapplication.util.loadStoreImage
import com.example.myapplication.util.toEuroString

class FeaturedProductAdapter(
    private val onOpen: (Product) -> Unit,
    private val onAdd: (Product) -> Unit,
    private val onToggleFavorite: (Product) -> Unit,
) : ListAdapter<Product, FeaturedProductAdapter.FeaturedViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeaturedViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_featured_product, parent, false)
        return FeaturedViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeaturedViewHolder, position: Int) {
        holder.bind(getItem(position))
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

    private companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<Product>() {
            override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean =
                oldItem == newItem
        }
    }
}
