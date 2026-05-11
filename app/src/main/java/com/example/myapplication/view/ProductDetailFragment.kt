package com.example.myapplication.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentProductDetailBinding
import com.example.myapplication.util.loadStoreImage
import com.example.myapplication.util.showIf
import com.example.myapplication.util.toEuroString

class ProductDetailFragment : Fragment() {

    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindProduct()
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).refreshChrome(getString(R.string.product_detail_title))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun bindProduct() {
        val productId = requireArguments().getInt(ARG_PRODUCT_ID)
        val controller = (requireActivity() as MainActivity).storeController
        val product = controller.getProduct(productId)

        if (product == null) {
            Toast.makeText(requireContext(), R.string.product_not_found, Toast.LENGTH_SHORT).show()
            requireActivity().onBackPressedDispatcher.onBackPressed()
            return
        }

        binding.ivProductDetail.loadStoreImage(product.imageUri)
        binding.tvDetailTitle.text = product.title
        binding.tvDetailMeta.text = getString(R.string.product_meta_format, product.platform, product.category)
        binding.tvDetailPrice.text = product.price.toEuroString()
        binding.tvDetailStock.text = getString(R.string.stock_format, product.stock)
        binding.tvDetailDescription.text = product.description
        binding.tvUnavailable.showIf(product.stock <= 0)
        binding.btnAddToCart.isEnabled = product.stock > 0
        binding.btnDetailFavorite.setImageResource(if (product.isFavorite) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline)

        binding.btnAddToCart.setOnClickListener {
            Toast.makeText(requireContext(), controller.addToCart(product.id), Toast.LENGTH_SHORT).show()
            (requireActivity() as MainActivity).refreshChrome(getString(R.string.product_detail_title))
        }
        binding.btnDetailFavorite.setOnClickListener {
            val active = controller.toggleFavorite(product.id)
            Toast.makeText(
                requireContext(),
                if (active) R.string.favorite_added else R.string.favorite_removed,
                Toast.LENGTH_SHORT,
            ).show()
            bindProduct()
        }
        binding.btnGoToCart.setOnClickListener {
            (requireActivity() as MainActivity).findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigation).selectedItemId = R.id.nav_cart
        }
    }

    companion object {
        private const val ARG_PRODUCT_ID = "product_id"

        fun newInstance(productId: Int) = ProductDetailFragment().apply {
            arguments = bundleOf(ARG_PRODUCT_ID to productId)
        }
    }
}
