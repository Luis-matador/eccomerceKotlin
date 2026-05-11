package com.example.myapplication.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.OrderItem
import com.example.myapplication.util.toEuroString

class OrderItemAdapter : ListAdapter<OrderItem, OrderItemAdapter.OrderItemViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order_detail_product, parent, false)
        return OrderItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class OrderItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title = itemView.findViewById<TextView>(R.id.tvOrderDetailItemTitle)
        private val meta = itemView.findViewById<TextView>(R.id.tvOrderDetailItemMeta)
        private val price = itemView.findViewById<TextView>(R.id.tvOrderDetailItemPrice)
        private val keys = itemView.findViewById<TextView>(R.id.tvOrderDetailKeys)

        fun bind(item: OrderItem) {
            title.text = item.title
            meta.text = itemView.context.getString(R.string.order_detail_item_meta, item.platform, item.quantity)
            price.text = item.unitPrice.toEuroString()
            keys.text = item.generatedKeys
        }
    }

    private companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<OrderItem>() {
            override fun areItemsTheSame(oldItem: OrderItem, newItem: OrderItem): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: OrderItem, newItem: OrderItem): Boolean =
                oldItem == newItem
        }
    }
}

