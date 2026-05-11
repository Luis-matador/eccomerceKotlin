package com.example.myapplication.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.OrderWithItems
import com.example.myapplication.util.toEuroString

class OrderAdapter(
    private var items: List<OrderWithItems>,
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun update(newItems: List<OrderWithItems>) {
        items = newItems
        notifyDataSetChanged()
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
            itemsText.text = orderWithItems.items.joinToString("\n\n") { item ->
                buildString {
                    append("• ${item.title} (${item.platform}) x${item.quantity}\n")
                    append("Claves:\n${item.generatedKeys}")
                }
            }
        }
    }
}

