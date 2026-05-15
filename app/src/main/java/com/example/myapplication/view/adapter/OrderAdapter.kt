package com.example.myapplication.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.OrderWithItems
import com.example.myapplication.util.toEuroString

class OrderAdapter(
    private val onOpen: (OrderWithItems) -> Unit,
) : ListAdapter<OrderWithItems, OrderAdapter.OrderViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title = itemView.findViewById<TextView>(R.id.tvOrderTitle)
        private val meta = itemView.findViewById<TextView>(R.id.tvOrderMeta)
        private val total = itemView.findViewById<TextView>(R.id.tvOrderTotal)
        private val itemsText = itemView.findViewById<TextView>(R.id.tvOrderItems)

        fun bind(orderWithItems: OrderWithItems) {
            title.text = itemView.context.getString(R.string.order_number_format, orderWithItems.order.id)
            meta.text = itemView.context.getString(
                R.string.order_meta_format,
                orderWithItems.order.createdAt,
                orderWithItems.order.paymentMethod,
                orderWithItems.order.status,
            )
            total.text = itemView.context.getString(
                R.string.total_format,
                orderWithItems.order.total.toEuroString(),
            )
            itemsText.text = itemView.context.resources.getQuantityString(
                R.plurals.order_items_count,
                orderWithItems.items.size,
                orderWithItems.items.size,
            )
            itemView.setOnClickListener { onOpen(orderWithItems) }
        }
    }

    private companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<OrderWithItems>() {
            override fun areItemsTheSame(oldItem: OrderWithItems, newItem: OrderWithItems): Boolean =
                oldItem.order.id == newItem.order.id

            override fun areContentsTheSame(oldItem: OrderWithItems, newItem: OrderWithItems): Boolean =
                oldItem == newItem
        }
    }
}
