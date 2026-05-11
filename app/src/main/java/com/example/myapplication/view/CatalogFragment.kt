package com.example.myapplication.view

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
import com.example.myapplication.databinding.FragmentCatalogBinding
import com.example.myapplication.model.StoreSortOption
import com.example.myapplication.util.showIf
import com.example.myapplication.view.adapter.FeaturedProductAdapter
import com.example.myapplication.view.adapter.ProductAdapter

class CatalogFragment : Fragment() {

    private var _binding: FragmentCatalogBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ProductAdapter
    private lateinit var featuredAdapter: FeaturedProductAdapter
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
        _binding = FragmentCatalogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ProductAdapter(
            onOpen = { product -> (requireActivity() as MainActivity).openProductDetail(product.id) },
            onAdd = { product ->
                Toast.makeText(requireContext(), controller().addToCart(product.id), Toast.LENGTH_SHORT).show()
                (requireActivity() as MainActivity).refreshChrome(getString(R.string.menu_catalog))
            },
            onToggleFavorite = { product ->
                val active = controller().toggleFavorite(product.id)
                Toast.makeText(
                    requireContext(),
                    if (active) R.string.favorite_added else R.string.favorite_removed,
                    Toast.LENGTH_SHORT,
                ).show()
                renderCatalog()
            },
        )

        featuredAdapter = FeaturedProductAdapter(
            onOpen = { product -> (requireActivity() as MainActivity).openProductDetail(product.id) },
            onAdd = { product ->
                Toast.makeText(requireContext(), controller().addToCart(product.id), Toast.LENGTH_SHORT).show()
                (requireActivity() as MainActivity).refreshChrome(getString(R.string.menu_catalog))
            },
            onToggleFavorite = { product ->
                controller().toggleFavorite(product.id)
                renderCatalog()
            },
        )

        binding.recyclerProducts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerProducts.adapter = adapter
        binding.recyclerFeaturedProducts.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerFeaturedProducts.adapter = featuredAdapter

        setupFilters()
        renderCatalog()
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).refreshChrome(getString(R.string.menu_catalog))
        if (this::adapter.isInitialized) renderCatalog()
    }

    override fun onDestroyView() {
        debounceRunnable?.let(handler::removeCallbacks)
        super.onDestroyView()
        _binding = null
    }

    private fun setupFilters() {
        val categories = controller().getCategories()
        binding.spinnerCategory.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            categories,
        )
        binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCategory = categories[position]
                renderCatalog()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        val sortLabels = listOf(
            getString(R.string.sort_relevance),
            getString(R.string.sort_price_asc),
            getString(R.string.sort_price_desc),
            getString(R.string.sort_popularity),
        )
        binding.spinnerSort.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            sortLabels,
        )
        binding.spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedSort = when (position) {
                    1 -> StoreSortOption.PRICE_ASC
                    2 -> StoreSortOption.PRICE_DESC
                    3 -> StoreSortOption.POPULARITY
                    else -> StoreSortOption.RELEVANCE
                }
                renderCatalog()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        binding.etCatalogSearch.doAfterTextChanged {
            searchText = it?.toString().orEmpty()
            debounceRunnable?.let(handler::removeCallbacks)
            debounceRunnable = Runnable { renderCatalog() }
            handler.postDelayed(debounceRunnable!!, 300)
        }
        binding.switchOnlyFavorites.setOnCheckedChangeListener { _, _ -> renderCatalog() }
    }

    private fun renderCatalog() {
        val products = controller().getProducts(searchText, selectedCategory, binding.switchOnlyFavorites.isChecked, selectedSort)
        val featuredProducts = controller().getFeaturedProducts()
        adapter.submitList(products)
        featuredAdapter.submitList(featuredProducts)
        binding.tvCatalogHelper.text = getString(
            if (controller().getCurrentUser().role == "admin") {
                R.string.catalog_header_admin
            } else {
                R.string.catalog_header_user
            },
        )
        binding.tvEmptyCatalog.showIf(products.isEmpty())
        binding.recyclerProducts.showIf(products.isNotEmpty())
        binding.recyclerFeaturedProducts.showIf(featuredProducts.isNotEmpty())
    }

    private fun controller() = (requireActivity() as MainActivity).storeController

    companion object {
        fun newInstance() = CatalogFragment()
    }
}
