package com.example.myapplication.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.CartLine
import com.example.myapplication.util.loadStoreImage
import com.example.myapplication.util.toEuroString

class CartAdapter(
    private val onIncrease: (CartLine) -> Unit,
    private val onDecrease: (CartLine) -> Unit,
    private val onRemove: (CartLine) -> Unit,
) : ListAdapter<CartLine, CartAdapter.CartViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val image = itemView.findViewById<ImageView>(R.id.ivCartProduct)
        private val title = itemView.findViewById<TextView>(R.id.tvCartTitle)
        private val meta = itemView.findViewById<TextView>(R.id.tvCartMeta)
        private val qty = itemView.findViewById<TextView>(R.id.tvCartQty)
        private val price = itemView.findViewById<TextView>(R.id.tvCartPrice)
        private val btnPlus = itemView.findViewById<Button>(R.id.btnQtyPlus)
        private val btnMinus = itemView.findViewById<Button>(R.id.btnQtyMinus)
        private val btnRemove = itemView.findViewById<Button>(R.id.btnRemoveItem)

        fun bind(line: CartLine) {
            image.loadStoreImage(line.imageUri)
            title.text = line.title
            meta.text = itemView.context.getString(R.string.cart_meta_format, line.platform, line.stock)
            qty.text = line.quantity.toString()
            price.text = line.subtotal.toEuroString()
            btnPlus.setOnClickListener { onIncrease(line) }
            btnMinus.setOnClickListener { onDecrease(line) }
            btnRemove.setOnClickListener { onRemove(line) }
        }
    }

    private companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<CartLine>() {
            override fun areItemsTheSame(oldItem: CartLine, newItem: CartLine): Boolean =
                oldItem.productId == newItem.productId

            override fun areContentsTheSame(oldItem: CartLine, newItem: CartLine): Boolean =
                oldItem == newItem
        }
    }
}
