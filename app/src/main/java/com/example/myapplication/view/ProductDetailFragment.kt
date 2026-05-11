package com.example.myapplication.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.util.loadStoreImage
import com.example.myapplication.util.showIf
import com.example.myapplication.util.toEuroString

class ProductDetailFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.fragment_product_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val productId = requireArguments().getInt(ARG_PRODUCT_ID)
        val controller = (requireActivity() as MainActivity).storeController
        val product = controller.getProduct(productId)

        val image = view.findViewById<ImageView>(R.id.ivProductDetail)
        val title = view.findViewById<TextView>(R.id.tvDetailTitle)
        val meta = view.findViewById<TextView>(R.id.tvDetailMeta)
        val price = view.findViewById<TextView>(R.id.tvDetailPrice)
        val stock = view.findViewById<TextView>(R.id.tvDetailStock)
        val description = view.findViewById<TextView>(R.id.tvDetailDescription)
        val addButton = view.findViewById<Button>(R.id.btnAddToCart)
        val cartButton = view.findViewById<Button>(R.id.btnGoToCart)
        val unavailable = view.findViewById<TextView>(R.id.tvUnavailable)

        if (product == null) {
            Toast.makeText(requireContext(), R.string.product_not_found, Toast.LENGTH_SHORT).show()
            requireActivity().onBackPressedDispatcher.onBackPressed()
            return
        }

        image.loadStoreImage(product.imageUri)
        title.text = product.title
        meta.text = getString(R.string.product_meta_format, product.platform, product.category)
        price.text = product.price.toEuroString()
        stock.text = getString(R.string.stock_format, product.stock)
        description.text = product.description
        unavailable.showIf(product.stock <= 0)
        addButton.isEnabled = product.stock > 0
        addButton.setOnClickListener {
            Toast.makeText(requireContext(), controller.addToCart(product.id), Toast.LENGTH_SHORT).show()
            (requireActivity() as MainActivity).refreshChrome(getString(R.string.product_detail_title))
        }
        cartButton.setOnClickListener {
            (requireActivity() as MainActivity).findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigation).selectedItemId = R.id.nav_cart
        }
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).refreshChrome(getString(R.string.product_detail_title))
    }

    companion object {
        private const val ARG_PRODUCT_ID = "product_id"

        fun newInstance(productId: Int) = ProductDetailFragment().apply {
            arguments = bundleOf(ARG_PRODUCT_ID to productId)
        }
    }
}

