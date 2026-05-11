package com.example.myapplication.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentCartBinding
import com.example.myapplication.util.showIf
import com.example.myapplication.util.toEuroString
import com.example.myapplication.view.adapter.CartAdapter

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = CartAdapter(
            onIncrease = { line ->
                Toast.makeText(requireContext(), controller().increaseCartItem(line.productId), Toast.LENGTH_SHORT).show()
                renderCart()
            },
            onDecrease = { line ->
                controller().decreaseCartItem(line.productId)
                renderCart()
            },
            onRemove = { line ->
                controller().removeCartItem(line.productId)
                Toast.makeText(requireContext(), R.string.item_removed_from_cart, Toast.LENGTH_SHORT).show()
                renderCart()
            },
        )

        binding.recyclerCart.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerCart.adapter = adapter

        binding.btnContinueShopping.setOnClickListener {
            (requireActivity() as MainActivity).findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigation).selectedItemId = R.id.nav_catalog
        }
        binding.btnCheckout.setOnClickListener {
            (requireActivity() as MainActivity).openCheckout()
        }
        renderCart()
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).refreshChrome(getString(R.string.menu_cart))
        if (this::adapter.isInitialized) renderCart()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun renderCart() {
        val items = controller().getCart()
        adapter.submitList(items)
        val total = controller().getCartTotal()
        binding.tvCartTotal.text = getString(R.string.total_format, total.toEuroString())
        binding.tvEmptyCart.showIf(items.isEmpty())
        binding.recyclerCart.showIf(items.isNotEmpty())
        binding.btnCheckout.isEnabled = items.isNotEmpty()
        (requireActivity() as MainActivity).refreshChrome(getString(R.string.menu_cart))
    }

    private fun controller() = (requireActivity() as MainActivity).storeController

    companion object {
        fun newInstance() = CartFragment()
    }
}
