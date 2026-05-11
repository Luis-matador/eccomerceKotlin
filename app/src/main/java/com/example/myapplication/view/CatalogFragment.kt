package com.example.myapplication.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.util.showIf
import com.example.myapplication.view.adapter.ProductAdapter

class CatalogFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var helperView: TextView
    private lateinit var adapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.fragment_catalog, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recyclerProducts)
        emptyView = view.findViewById(R.id.tvEmptyCatalog)
        helperView = view.findViewById(R.id.tvCatalogHelper)

        adapter = ProductAdapter(
            items = emptyList(),
            onOpen = { product ->
                (requireActivity() as MainActivity).openProductDetail(product.id)
            },
            onAdd = { product ->
                val message = (requireActivity() as MainActivity).storeController.addToCart(product.id)
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                (requireActivity() as MainActivity).refreshChrome()
            },
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        renderCatalog()
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).refreshChrome(getString(R.string.menu_catalog))
        if (this::adapter.isInitialized) {
            renderCatalog()
        }
    }

    private fun renderCatalog() {
        val controller = (requireActivity() as MainActivity).storeController
        val products = controller.getProducts()
        adapter.update(products)
        helperView.text = getString(
            if (controller.getCurrentUser().role == "admin") {
                R.string.catalog_header_admin
            } else {
                R.string.catalog_header_user
            },
        )
        emptyView.showIf(products.isEmpty())
        recyclerView.showIf(products.isNotEmpty())
    }

    companion object {
        fun newInstance() = CatalogFragment()
    }
}

