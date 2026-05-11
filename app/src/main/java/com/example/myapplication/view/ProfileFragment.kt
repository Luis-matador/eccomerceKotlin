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
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.model.Product
import com.example.myapplication.util.loadStoreImage
import com.example.myapplication.util.showIf

class ProfileFragment : Fragment() {

    private lateinit var ivProfile: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvRole: TextView
    private lateinit var adminPanel: View
    private lateinit var spinnerProducts: Spinner
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
    ): View = inflater.inflate(R.layout.fragment_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ivProfile = view.findViewById(R.id.ivProfile)
        tvName = view.findViewById(R.id.tvProfileName)
        tvEmail = view.findViewById(R.id.tvProfileEmail)
        tvRole = view.findViewById(R.id.tvProfileRole)
        adminPanel = view.findViewById(R.id.layoutAdminPanel)
        spinnerProducts = view.findViewById(R.id.spinnerAdminProducts)

        view.findViewById<Button>(R.id.btnChangeProfilePhoto).setOnClickListener {
            profileImagePicker.launch(arrayOf("image/*"))
        }
        view.findViewById<Button>(R.id.btnCreateProduct).setOnClickListener {
            showProductDialog(null)
        }
        view.findViewById<Button>(R.id.btnEditProduct).setOnClickListener {
            val product = getAdminProducts().getOrNull(spinnerProducts.selectedItemPosition)
            if (product == null) {
                Toast.makeText(requireContext(), R.string.no_products_available, Toast.LENGTH_SHORT).show()
            } else {
                showProductDialog(product)
            }
        }
        view.findViewById<Button>(R.id.btnLogout).setOnClickListener {
            (requireActivity() as MainActivity).logout()
        }

        bindProfile()
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).refreshChrome(getString(R.string.menu_profile))
        bindProfile()
    }

    private fun bindProfile() {
        val user = controller().getCurrentUser()
        ivProfile.loadStoreImage(user.photoUri, profile = true)
        tvName.text = user.name
        tvEmail.text = user.email
        tvRole.text = getString(R.string.role_format, user.role)
        adminPanel.showIf(user.role == "admin")
        if (user.role == "admin") {
            val titles = getAdminProducts().map { it.title }
            spinnerProducts.adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                if (titles.isEmpty()) listOf(getString(R.string.no_products_available)) else titles,
            )
        }
    }

    private fun getAdminProducts(): List<Product> = controller().getProducts()

    private fun controller() = (requireActivity() as MainActivity).storeController

    private fun showProductDialog(existing: Product?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_product_form, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etProductTitle)
        val etPlatform = dialogView.findViewById<EditText>(R.id.etProductPlatform)
        val etCategory = dialogView.findViewById<EditText>(R.id.etProductCategory)
        val etPrice = dialogView.findViewById<EditText>(R.id.etProductPrice)
        val etStock = dialogView.findViewById<EditText>(R.id.etProductStock)
        val etDescription = dialogView.findViewById<EditText>(R.id.etProductDescription)
        val ivPreview = dialogView.findViewById<ImageView>(R.id.ivProductPreview)
        val switchFeatured = dialogView.findViewById<Switch>(R.id.switchFeatured)
        val btnPick = dialogView.findViewById<Button>(R.id.btnPickProductImage)

        var selectedImageUri: String? = existing?.imageUri
        etTitle.setText(existing?.title.orEmpty())
        etPlatform.setText(existing?.platform.orEmpty())
        etCategory.setText(existing?.category.orEmpty())
        etPrice.setText(existing?.price?.toString().orEmpty())
        etStock.setText(existing?.stock?.toString().orEmpty())
        etDescription.setText(existing?.description.orEmpty())
        switchFeatured.isChecked = existing?.featured == true
        ivPreview.loadStoreImage(selectedImageUri)

        btnPick.setOnClickListener {
            onImageSelected = { uriString ->
                selectedImageUri = uriString
                ivPreview.loadStoreImage(uriString)
            }
            productImagePicker.launch(arrayOf("image/*"))
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (existing == null) R.string.create_product else R.string.edit_product)
            .setView(dialogView)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.save, null)
            .create()
            .also { dialog ->
                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val title = etTitle.text.toString().trim()
                        val platform = etPlatform.text.toString().trim()
                        val category = etCategory.text.toString().trim()
                        val description = etDescription.text.toString().trim()
                        val price = etPrice.text.toString().trim().toDoubleOrNull()
                        val stock = etStock.text.toString().trim().toIntOrNull()

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
                                featured = switchFeatured.isChecked,
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
