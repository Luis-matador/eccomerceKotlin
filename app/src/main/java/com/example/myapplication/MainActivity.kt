package com.example.myapplication

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.example.myapplication.controller.StoreController
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.view.AuthFragment
import com.example.myapplication.view.CartFragment
import com.example.myapplication.view.CatalogFragment
import com.example.myapplication.view.CheckoutFragment
import com.example.myapplication.view.FavoritesFragment
import com.example.myapplication.view.OrderDetailFragment
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
                    showRootFragment(CatalogFragment.newInstance(), getString(R.string.menu_catalog))
                    true
                }
                R.id.nav_favorites -> {
                    showRootFragment(FavoritesFragment.newInstance(), getString(R.string.menu_favorites))
                    true
                }
                R.id.nav_cart -> {
                    showRootFragment(CartFragment.newInstance(), getString(R.string.menu_cart))
                    true
                }
                R.id.nav_orders -> {
                    showRootFragment(OrdersFragment.newInstance(), getString(R.string.menu_orders))
                    true
                }
                R.id.nav_profile -> {
                    showRootFragment(ProfileFragment.newInstance(), getString(R.string.menu_profile))
                    true
                }
                else -> false
            }
        }

        if (savedInstanceState == null) {
            if (storeController.isLoggedIn()) {
                showAuthenticatedShell()
            } else {
                showAuthScreen()
            }
        } else {
            if (storeController.isLoggedIn()) {
                binding.bottomNavigation.visibility = View.VISIBLE
                refreshChrome()
            } else {
                binding.bottomNavigation.visibility = View.GONE
                refreshChrome(getString(R.string.auth_title))
            }
        }
    }

    fun onAuthSuccess() {
        showAuthenticatedShell()
    }

    fun showAuthScreen() {
        supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
        binding.bottomNavigation.visibility = View.GONE
        supportActionBar?.title = getString(R.string.app_name)
        supportActionBar?.subtitle = getString(R.string.auth_toolbar_subtitle)
        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, AuthFragment.newInstance())
        }
    }

    fun logout() {
        storeController.logout()
        showAuthScreen()
    }

    fun openProductDetail(productId: Int) {
        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, ProductDetailFragment.newInstance(productId))
            addToBackStack("product_detail")
        }
        refreshChrome(getString(R.string.product_detail_title))
    }

    fun openCheckout() {
        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, CheckoutFragment.newInstance())
            addToBackStack("checkout")
        }
        refreshChrome(getString(R.string.checkout_title))
    }

    fun openOrderDetail(orderId: Int) {
        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, OrderDetailFragment.newInstance(orderId))
            addToBackStack("order_detail")
        }
        refreshChrome(getString(R.string.order_detail_title))
    }

    fun navigateToOrdersAfterPurchase() {
        supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
        binding.bottomNavigation.selectedItemId = R.id.nav_orders
    }

    fun refreshChrome(customTitle: String? = null) {
        val user = storeController.getCurrentUserOrNull()
        if (user == null) {
            supportActionBar?.title = customTitle ?: getString(R.string.app_name)
            supportActionBar?.subtitle = getString(R.string.auth_toolbar_subtitle)
            return
        }
        supportActionBar?.title = customTitle ?: getString(R.string.app_name)
        supportActionBar?.subtitle = getString(
            R.string.toolbar_subtitle,
            user.name,
            user.role.uppercase(),
            storeController.getCartCount(),
            storeController.getFavoritesCount(),
        )
    }

    private fun showAuthenticatedShell() {
        binding.bottomNavigation.visibility = View.VISIBLE
        refreshChrome(getString(R.string.menu_catalog))
        binding.bottomNavigation.selectedItemId = R.id.nav_catalog
    }

    private fun showRootFragment(fragment: Fragment, title: String) {
        supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, fragment)
        }
        refreshChrome(title)
    }
}
