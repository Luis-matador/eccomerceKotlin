package com.example.myapplication.view

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.databinding.DialogProductFormBinding
import com.example.myapplication.databinding.FragmentProfileBinding
import com.example.myapplication.model.Product
import com.example.myapplication.util.loadStoreImage
import com.example.myapplication.util.showIf

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private var onImageSelected: ((String?) -> Unit)? = null

    private val profileImagePicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            persistPermission(it)
            controller().addCurrentUserPhoto(it.toString())
            bindProfile()
            Toast.makeText(requireContext(), R.string.profile_photo_updated, Toast.LENGTH_SHORT).show()
        }
    }

    private val productImagePicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            persistPermission(it)
            onImageSelected?.invoke(it.toString())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnChangeProfilePhoto.setOnClickListener {
            profileImagePicker.launch(arrayOf("image/*"))
        }
        binding.btnCreateProduct.setOnClickListener {
            showProductDialog(null)
        }
        binding.btnEditProduct.setOnClickListener {
            val product = getAdminProducts().getOrNull(binding.spinnerAdminProducts.selectedItemPosition)
            if (product == null) {
                Toast.makeText(requireContext(), R.string.no_products_available, Toast.LENGTH_SHORT).show()
            } else {
                showProductDialog(product)
            }
        }
        binding.btnLogout.setOnClickListener {
            (requireActivity() as MainActivity).logout()
        }

        bindProfile()
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).refreshChrome(getString(R.string.menu_profile))
        bindProfile()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun bindProfile() {
        val user = controller().getCurrentUser()
        binding.ivProfile.loadStoreImage(user.photoUri, profile = true)
        binding.tvProfileName.text = user.name
        binding.tvProfileEmail.text = user.email
        binding.tvProfileRole.text = getString(R.string.role_format, user.role)
        binding.layoutAdminPanel.showIf(user.role == "admin")
        if (user.role == "admin") {
            val titles = getAdminProducts().map { it.title }
            binding.spinnerAdminProducts.adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                if (titles.isEmpty()) listOf(getString(R.string.no_products_available)) else titles,
            )
        }
    }

    private fun getAdminProducts(): List<Product> = controller().getProducts()

    private fun controller() = (requireActivity() as MainActivity).storeController

    private fun showProductDialog(existing: Product?) {
        val dialogBinding = DialogProductFormBinding.inflate(layoutInflater)

        var selectedImageUri: String? = existing?.imageUri
        dialogBinding.etProductTitle.setText(existing?.title.orEmpty())
        dialogBinding.etProductPlatform.setText(existing?.platform.orEmpty())
        dialogBinding.etProductCategory.setText(existing?.category.orEmpty())
        dialogBinding.etProductPrice.setText(existing?.price?.toString().orEmpty())
        dialogBinding.etProductStock.setText(existing?.stock?.toString().orEmpty())
        dialogBinding.etProductDescription.setText(existing?.description.orEmpty())
        dialogBinding.switchFeatured.isChecked = existing?.featured == true
        dialogBinding.ivProductPreview.loadStoreImage(selectedImageUri)

        dialogBinding.btnPickProductImage.setOnClickListener {
            onImageSelected = { uriString ->
                selectedImageUri = uriString
                dialogBinding.ivProductPreview.loadStoreImage(uriString)
            }
            productImagePicker.launch(arrayOf("image/*"))
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (existing == null) R.string.create_product else R.string.edit_product)
            .setView(dialogBinding.root)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.save, null)
            .create()
            .also { dialog ->
                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val title = dialogBinding.etProductTitle.text.toString().trim()
                        val platform = dialogBinding.etProductPlatform.text.toString().trim()
                        val category = dialogBinding.etProductCategory.text.toString().trim()
                        val description = dialogBinding.etProductDescription.text.toString().trim()
                        val price = dialogBinding.etProductPrice.text.toString().trim().toDoubleOrNull()
                        val stock = dialogBinding.etProductStock.text.toString().trim().toIntOrNull()

                        if (title.isBlank() || platform.isBlank() || category.isBlank() || description.isBlank() || price == null || stock == null) {
                            Toast.makeText(requireContext(), R.string.complete_product_form, Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        controller().saveProduct(
                            Product(
                                id = existing?.id ?: 0,
                                title = title,
                                platform = platform,
                                category = category,
                                description = description,
                                price = price,
                                stock = stock,
                                imageUri = selectedImageUri,
                                featured = dialogBinding.switchFeatured.isChecked,
                            ),
                        )
                        bindProfile()
                        Toast.makeText(requireContext(), R.string.product_saved, Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                }
                dialog.show()
            }
    }

    private fun persistPermission(uri: Uri) {
        try {
            requireContext().contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        } catch (_: SecurityException) {
        } catch (_: IllegalArgumentException) {
        }
        try {
            DocumentsContract.getDocumentId(uri)
        } catch (_: Exception) {
        }
    }

    companion object {
        fun newInstance() = ProfileFragment()
    }
}
