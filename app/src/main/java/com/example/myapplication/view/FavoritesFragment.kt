package com.example.myapplication.view

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentFavoritesBinding
import com.example.myapplication.model.Product
import com.example.myapplication.model.StoreSortOption
import com.example.myapplication.util.showIf
import com.example.myapplication.view.adapter.ProductAdapter

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ProductAdapter
    private val handler = Handler(Looper.getMainLooper())
    private var debounceRunnable: Runnable? = null
    private var selectedCategory: String = "Todas"
    private var searchText: String = ""
    private var selectedSort: StoreSortOption = StoreSortOption.RELEVANCE

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isAdmin = controller().getCurrentUserOrNull()?.role == "admin"

        adapter = ProductAdapter(
            isAdmin = isAdmin,
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
            onDelete = { product ->
                showDeleteConfirmation(product)
            }
        )

        binding.recyclerFavorites.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerFavorites.adapter = adapter
        setupFilters()
        renderFavorites()
    }

    private fun showDeleteConfirmation(product: Product) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_product)
            .setMessage(R.string.confirm_delete_product)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ ->
                controller().deleteProduct(product.id)
                Toast.makeText(requireContext(), R.string.product_deleted, Toast.LENGTH_SHORT).show()
                renderFavorites()
            }
            .show()
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).refreshChrome(getString(R.string.menu_favorites))
        if (this::adapter.isInitialized) renderFavorites()
    }

    override fun onDestroyView() {
        debounceRunnable?.let(handler::removeCallbacks)
        super.onDestroyView()
        _binding = null
    }

    private fun setupFilters() {
        val categories = controller().getCategories()
        binding.spinnerFavoritesCategory.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            categories,
        )
        binding.spinnerFavoritesCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCategory = categories[position]
                renderFavorites()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        val sortLabels = listOf(
            getString(R.string.sort_relevance),
            getString(R.string.sort_price_asc),
            getString(R.string.sort_price_desc),
            getString(R.string.sort_popularity),
        )
        binding.spinnerFavoritesSort.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            sortLabels,
        )
        binding.spinnerFavoritesSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedSort = when (position) {
                    1 -> StoreSortOption.PRICE_ASC
                    2 -> StoreSortOption.PRICE_DESC
                    3 -> StoreSortOption.POPULARITY
                    else -> StoreSortOption.RELEVANCE
                }
                renderFavorites()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
        binding.etFavoritesSearch.doAfterTextChanged {
            searchText = it?.toString().orEmpty()
            debounceRunnable?.let(handler::removeCallbacks)
            debounceRunnable = Runnable { renderFavorites() }
            handler.postDelayed(debounceRunnable!!, 300)
        }
    }

    private fun renderFavorites() {
        val items = controller().getProducts(searchText, selectedCategory, favoritesOnly = true, sortOption = selectedSort)
        adapter.submitList(items)
        binding.tvEmptyFavorites.showIf(items.isEmpty())
        binding.recyclerFavorites.showIf(items.isNotEmpty())
    }

    private fun controller() = (requireActivity() as MainActivity).storeController

    companion object {
        fun newInstance() = FavoritesFragment()
    }
}
