package com.example.myapplication.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.util.showIf
import com.example.myapplication.view.adapter.OrderAdapter

class OrdersFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var adapter: OrderAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.fragment_orders, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recyclerOrders)
        emptyView = view.findViewById(R.id.tvEmptyOrders)

        adapter = OrderAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        renderOrders()
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).refreshChrome(getString(R.string.menu_orders))
        if (this::adapter.isInitialized) renderOrders()
    }

    private fun renderOrders() {
        val orders = (requireActivity() as MainActivity).storeController.getOrders()
        adapter.update(orders)
        emptyView.showIf(orders.isEmpty())
        recyclerView.showIf(orders.isNotEmpty())
    }

    companion object {
        fun newInstance() = OrdersFragment()
    }
}

