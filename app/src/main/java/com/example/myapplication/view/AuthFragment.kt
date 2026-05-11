package com.example.myapplication.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentAuthBinding

class AuthFragment : Fragment() {

    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as MainActivity
        val controller = activity.storeController

        binding.btnLogin.setOnClickListener {
            val result = controller.login(
                binding.etAuthEmail.text.toString().trim(),
                binding.etAuthPassword.text.toString(),
            )
            Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
            if (result.success) activity.onAuthSuccess()
        }

        binding.btnRegister.setOnClickListener {
            val result = controller.register(
                binding.etAuthName.text.toString().trim(),
                binding.etAuthEmail.text.toString().trim(),
                binding.etAuthPassword.text.toString(),
            )
            Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
            if (result.success) activity.onAuthSuccess()
        }

        binding.btnQuickAdmin.setOnClickListener {
            binding.etAuthEmail.setText("admin@g2a.local")
            binding.etAuthPassword.setText("admin123")
        }
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).refreshChrome(getString(R.string.auth_title))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = AuthFragment()
    }
}
