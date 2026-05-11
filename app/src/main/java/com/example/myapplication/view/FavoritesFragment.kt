package com.example.myapplication.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.util.showIf
import com.example.myapplication.view.adapter.ProductAdapter

class FavoritesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var searchView: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var adapter: ProductAdapter
    private var selectedCategory: String = "Todas"
    private var searchText: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.fragment_favorites, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recyclerFavorites)
        emptyView = view.findViewById(R.id.tvEmptyFavorites)
        searchView = view.findViewById(R.id.etFavoritesSearch)
        categorySpinner = view.findViewById(R.id.spinnerFavoritesCategory)

        adapter = ProductAdapter(
            items = emptyList(),
            onOpen = { product -> (requireActivity() as MainActivity).openProductDetail(product.id) },
            onAdd = { product ->
                Toast.makeText(requireContext(), controller().addToCart(product.id), Toast.LENGTH_SHORT).show()
                (requireActivity() as MainActivity).refreshChrome(getString(R.string.menu_favorites))
            },
            onToggleFavorite = { product ->
                val active = controller().toggleFavorite(product.id)
                Toast.makeText(
                    requireContext(),
                    if (active) R.string.favorite_added else R.string.favorite_removed,
                    Toast.LENGTH_SHORT,
                ).show()
                renderFavorites()
            },
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        setupFilters()
        renderFavorites()
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).refreshChrome(getString(R.string.menu_favorites))
        if (this::adapter.isInitialized) renderFavorites()
    }

    private fun setupFilters() {
        val categories = controller().getCategories()
        categorySpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            categories,
        )
        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCategory = categories[position]
                renderFavorites()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
        searchView.doAfterTextChanged {
            searchText = it?.toString().orEmpty()
            renderFavorites()
        }
    }

    private fun renderFavorites() {
        val items = controller().getProducts(searchText, selectedCategory, favoritesOnly = true)
        adapter.update(items)
        emptyView.showIf(items.isEmpty())
        recyclerView.showIf(items.isNotEmpty())
    }

    private fun controller() = (requireActivity() as MainActivity).storeController

    companion object {
        fun newInstance() = FavoritesFragment()
    }
}

