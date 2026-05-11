package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.example.myapplication.controller.StoreController
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.view.CartFragment
import com.example.myapplication.view.CatalogFragment
import com.example.myapplication.view.CheckoutFragment
import com.example.myapplication.view.OrdersFragment
import com.example.myapplication.view.ProductDetailFragment
import com.example.myapplication.view.ProfileFragment

class MainActivity : AppCompatActivity() {

    lateinit var storeController: StoreController
        private set

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storeController = StoreController(this)

        setSupportActionBar(binding.toolbar)
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_catalog -> {
                    showRootFragment(CatalogFragment.newInstance())
                    true
                }
                R.id.nav_cart -> {
                    showRootFragment(CartFragment.newInstance())
                    true
                }
                R.id.nav_orders -> {
                    showRootFragment(OrdersFragment.newInstance())
                    true
                }
                R.id.nav_profile -> {
                    showRootFragment(ProfileFragment.newInstance())
                    true
                }
                else -> false
            }
        }

        if (savedInstanceState == null) {
            binding.bottomNavigation.selectedItemId = R.id.nav_catalog
        } else {
            refreshChrome()
        }
    }

    fun openProductDetail(productId: Int) {
        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, ProductDetailFragment.newInstance(productId))
            addToBackStack("product_detail")
        }
        refreshChrome("Ficha de producto")
    }

    fun openCheckout() {
        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, CheckoutFragment.newInstance())
            addToBackStack("checkout")
        }
        refreshChrome("Pago seguro")
    }

    fun navigateToOrdersAfterPurchase() {
        supportFragmentManager.popBackStack(null, 1)
        binding.bottomNavigation.selectedItemId = R.id.nav_orders
    }

    fun refreshChrome(customTitle: String? = null) {
        val user = storeController.getCurrentUser()
        val cartCount = storeController.getCartCount()
        supportActionBar?.title = customTitle ?: getString(R.string.app_name)
        supportActionBar?.subtitle = getString(
            R.string.toolbar_subtitle,
            user.name,
            user.role.uppercase(),
            cartCount,
        )
    }

    private fun showRootFragment(fragment: Fragment) {
        supportFragmentManager.popBackStack(null, 1)
        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, fragment)
        }
        refreshChrome()
    }
}

