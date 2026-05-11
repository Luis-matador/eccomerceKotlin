package com.example.myapplication.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentCheckoutBinding
import com.example.myapplication.util.toEuroString

class CheckoutFragment : Fragment() {

    private var _binding: FragmentCheckoutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCheckoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val controller = (requireActivity() as MainActivity).storeController
        val currentUser = controller.getCurrentUser()

        binding.tvCheckoutTotal.text = getString(R.string.total_format, controller.getCartTotal().toEuroString())
        binding.etBuyerEmail.setText(currentUser.email)
        binding.etBuyerName.setText(currentUser.name)

        val methods = listOf("Tarjeta", "PayPal", "Bizum", "Saldo G2A")
        binding.spinnerPaymentMethod.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, methods)

        binding.btnConfirmPurchase.setOnClickListener {
            val buyerName = binding.etBuyerName.text.toString().trim()
            val buyerEmail = binding.etBuyerEmail.text.toString().trim()
            val paymentMethod = binding.spinnerPaymentMethod.selectedItem.toString()

            if (buyerName.isBlank()) {
                binding.etBuyerName.error = getString(R.string.required_field)
                return@setOnClickListener
            }
            if (!buyerEmail.contains("@")) {
                binding.etBuyerEmail.error = getString(R.string.invalid_email)
                return@setOnClickListener
            }

            val result = controller.checkout(buyerName, buyerEmail, paymentMethod)
            Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
            if (result.success) {
                (requireActivity() as MainActivity).navigateToOrdersAfterPurchase()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).refreshChrome(getString(R.string.checkout_title))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = CheckoutFragment()
    }
}
