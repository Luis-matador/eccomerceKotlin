package com.example.myapplication.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentOrdersBinding
import com.example.myapplication.util.showIf
import com.example.myapplication.view.adapter.OrderAdapter

class OrdersFragment : Fragment() {

    private var _binding: FragmentOrdersBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: OrderAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = OrderAdapter { order ->
            (requireActivity() as MainActivity).openOrderDetail(order.order.id)
        }
        binding.recyclerOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerOrders.adapter = adapter
        renderOrders()
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).refreshChrome(getString(R.string.menu_orders))
        if (this::adapter.isInitialized) renderOrders()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun renderOrders() {
        val orders = (requireActivity() as MainActivity).storeController.getOrders()
        adapter.submitList(orders)
        binding.tvEmptyOrders.showIf(orders.isEmpty())
        binding.recyclerOrders.showIf(orders.isNotEmpty())
    }

    companion object {
        fun newInstance() = OrdersFragment()
    }
}
