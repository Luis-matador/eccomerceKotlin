package com.example.myapplication.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.util.showIf
import com.example.myapplication.util.toEuroString
import com.example.myapplication.view.adapter.CartAdapter

class CartFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var totalView: TextView
    private lateinit var checkoutButton: Button
    private lateinit var adapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.fragment_cart, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recyclerCart)
        emptyView = view.findViewById(R.id.tvEmptyCart)
        totalView = view.findViewById(R.id.tvCartTotal)
        checkoutButton = view.findViewById(R.id.btnCheckout)
        val continueButton = view.findViewById<Button>(R.id.btnContinueShopping)

        adapter = CartAdapter(
            items = emptyList(),
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

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        continueButton.setOnClickListener {
            (requireActivity() as MainActivity).findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigation).selectedItemId = R.id.nav_catalog
        }
        checkoutButton.setOnClickListener {
            (requireActivity() as MainActivity).openCheckout()
        }
        renderCart()
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).refreshChrome(getString(R.string.menu_cart))
        if (this::adapter.isInitialized) renderCart()
    }

    private fun renderCart() {
        val items = controller().getCart()
        adapter.update(items)
        val total = controller().getCartTotal()
        totalView.text = getString(R.string.total_format, total.toEuroString())
        emptyView.showIf(items.isEmpty())
        recyclerView.showIf(items.isNotEmpty())
        checkoutButton.isEnabled = items.isNotEmpty()
        (requireActivity() as MainActivity).refreshChrome(getString(R.string.menu_cart))
    }

    private fun controller() = (requireActivity() as MainActivity).storeController

    companion object {
        fun newInstance() = CartFragment()
    }
}

