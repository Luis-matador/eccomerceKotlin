package com.example.myapplication.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.util.toEuroString

class CheckoutFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.fragment_checkout, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val controller = (requireActivity() as MainActivity).storeController
        val currentUser = controller.getCurrentUser()

        val total = view.findViewById<TextView>(R.id.tvCheckoutTotal)
        val email = view.findViewById<EditText>(R.id.etBuyerEmail)
        val name = view.findViewById<EditText>(R.id.etBuyerName)
        val paymentSpinner = view.findViewById<Spinner>(R.id.spinnerPaymentMethod)
        val confirmButton = view.findViewById<Button>(R.id.btnConfirmPurchase)

        total.text = getString(R.string.total_format, controller.getCartTotal().toEuroString())
        email.setText(currentUser.email)
        name.setText(currentUser.name)

        val methods = listOf("Tarjeta", "PayPal", "Bizum", "Saldo G2A")
        paymentSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, methods)

        confirmButton.setOnClickListener {
            val buyerName = name.text.toString().trim()
            val buyerEmail = email.text.toString().trim()
            val paymentMethod = paymentSpinner.selectedItem.toString()

            if (buyerName.isBlank()) {
                name.error = getString(R.string.required_field)
                return@setOnClickListener
            }
            if (!buyerEmail.contains("@")) {
                email.error = getString(R.string.invalid_email)
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

    companion object {
        fun newInstance() = CheckoutFragment()
    }
}

