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
import com.example.myapplication.util.showIf
import com.example.myapplication.util.toEuroString

class ProductAdapter(
    private val isAdmin: Boolean = false,
    private val onOpen: (Product) -> Unit,
    private val onAdd: (Product) -> Unit,
    private val onToggleFavorite: (Product) -> Unit,
    private val onDelete: (Product) -> Unit = {},
) : ListAdapter<Product, ProductAdapter.ProductViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val image = itemView.findViewById<ImageView>(R.id.ivProduct)
        private val title = itemView.findViewById<TextView>(R.id.tvProductTitle)
        private val meta = itemView.findViewById<TextView>(R.id.tvProductMeta)
        private val price = itemView.findViewById<TextView>(R.id.tvProductPrice)
        private val stock = itemView.findViewById<TextView>(R.id.tvProductStock)
        private val featured = itemView.findViewById<TextView>(R.id.tvFeatured)
        private val openButton = itemView.findViewById<Button>(R.id.btnOpenProduct)
        private val addButton = itemView.findViewById<Button>(R.id.btnAddProduct)
        private val favoriteButton = itemView.findViewById<ImageButton>(R.id.btnFavoriteProduct)
        private val deleteButton = itemView.findViewById<ImageButton>(R.id.btnDeleteProductItem)

        fun bind(product: Product) {
            image.loadStoreImage(product.imageUri)
            title.text = product.title
            meta.text = itemView.context.getString(R.string.product_meta_format, product.platform, product.category)
            price.text = product.price.toEuroString()
            stock.text = itemView.context.getString(R.string.stock_format, product.stock)
            featured.showIf(product.featured)
            addButton.isEnabled = product.stock > 0
            favoriteButton.setImageResource(
                if (product.isFavorite) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline,
            )
            
            deleteButton.showIf(isAdmin)
            
            openButton.setOnClickListener { onOpen(product) }
            addButton.setOnClickListener { onAdd(product) }
            favoriteButton.setOnClickListener { onToggleFavorite(product) }
            deleteButton.setOnClickListener { onDelete(product) }
            itemView.setOnClickListener { onOpen(product) }
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
