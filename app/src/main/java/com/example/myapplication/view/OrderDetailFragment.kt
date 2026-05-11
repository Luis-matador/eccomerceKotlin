package com.example.myapplication.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentOrderDetailBinding
import com.example.myapplication.util.showIf
import com.example.myapplication.util.toEuroString
import com.example.myapplication.view.adapter.OrderItemAdapter

class OrderDetailFragment : Fragment() {

    private var _binding: FragmentOrderDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: OrderItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentOrderDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = OrderItemAdapter()
        binding.recyclerOrderDetailItems.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerOrderDetailItems.adapter = adapter
        renderOrder()
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).refreshChrome(getString(R.string.order_detail_title))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun renderOrder() {
        val orderId = requireArguments().getInt(ARG_ORDER_ID)
        val order = (requireActivity() as MainActivity).storeController.getOrder(orderId)
        if (order == null) {
            Toast.makeText(requireContext(), R.string.order_not_found, Toast.LENGTH_SHORT).show()
            requireActivity().onBackPressedDispatcher.onBackPressed()
            return
        }

        binding.tvOrderDetailTitle.text = getString(R.string.order_number_format, order.order.id)
        binding.tvOrderDetailMeta.text = getString(
            R.string.order_meta_format,
            order.order.createdAt,
            order.order.paymentMethod,
            order.order.status,
        )
        binding.tvOrderDetailBuyer.text = getString(R.string.order_detail_buyer_format, order.order.buyerName, order.order.buyerEmail)
        binding.tvOrderDetailTotal.text = getString(R.string.total_format, order.order.total.toEuroString())
        binding.tvOrderDetailEmpty.showIf(order.items.isEmpty())
        binding.recyclerOrderDetailItems.showIf(order.items.isNotEmpty())
        adapter.submitList(order.items)
    }

    companion object {
        private const val ARG_ORDER_ID = "order_id"

        fun newInstance(orderId: Int) = OrderDetailFragment().apply {
            arguments = bundleOf(ARG_ORDER_ID to orderId)
        }
    }
}

