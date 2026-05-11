package com.example.myapplication.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapplication.MainActivity
import com.example.myapplication.R

class AuthFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.fragment_auth, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as MainActivity
        val controller = activity.storeController

        val name = view.findViewById<EditText>(R.id.etAuthName)
        val email = view.findViewById<EditText>(R.id.etAuthEmail)
        val password = view.findViewById<EditText>(R.id.etAuthPassword)
        val loginButton = view.findViewById<Button>(R.id.btnLogin)
        val registerButton = view.findViewById<Button>(R.id.btnRegister)
        val adminButton = view.findViewById<Button>(R.id.btnQuickAdmin)

        loginButton.setOnClickListener {
            val result = controller.login(
                email.text.toString().trim(),
                password.text.toString(),
            )
            Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
            if (result.success) {
                activity.onAuthSuccess()
            }
        }

        registerButton.setOnClickListener {
            val result = controller.register(
                name.text.toString().trim(),
                email.text.toString().trim(),
                password.text.toString(),
            )
            Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
            if (result.success) {
                activity.onAuthSuccess()
            }
        }

        adminButton.setOnClickListener {
            email.setText("admin@g2a.local")
            password.setText("admin123")
        }
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).refreshChrome(getString(R.string.auth_title))
    }

    companion object {
        fun newInstance() = AuthFragment()
    }
}

