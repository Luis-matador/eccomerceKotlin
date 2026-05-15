package com.example.myapplication.util

import android.content.Context
import android.net.Uri
import android.view.View
import android.widget.ImageView
import com.example.myapplication.R
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.Locale

fun Double.toEuroString(): String =
    NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-ES")).format(this)

fun View.showIf(condition: Boolean) {
    visibility = if (condition) View.VISIBLE else View.GONE
}

fun ImageView.loadStoreImage(uriString: String?, profile: Boolean = false) {
    if (!uriString.isNullOrBlank()) {
        try {
            val uri = Uri.parse(uriString)
            // Si es una ruta de archivo local (empieza con /)
            if (uriString.startsWith("/")) {
                setImageURI(Uri.fromFile(java.io.File(uriString)))
                return
            }
            // Si es una URI de content provider
            setImageURI(uri)
            return
        } catch (_: Exception) {
        }
    }
    setImageResource(if (profile) R.drawable.ic_avatar_placeholder else R.drawable.ic_product_placeholder)
}

fun Context.copyImageToInternalStorage(uri: Uri): String? {
    return try {
        val inputStream = contentResolver.openInputStream(uri) ?: return null
        val fileName = "img_${System.currentTimeMillis()}.jpg"
        val outputFile = File(filesDir, "images")
        if (!outputFile.exists()) outputFile.mkdirs()
        val file = File(outputFile, fileName)
        
        FileOutputStream(file).use { output ->
            inputStream.copyTo(output)
        }
        inputStream.close()
        
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

