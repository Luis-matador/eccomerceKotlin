package com.example.myapplication.util

import android.net.Uri
import android.view.View
import android.widget.ImageView
import com.example.myapplication.R
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
            setImageURI(Uri.parse(uriString))
            return
        } catch (_: Exception) {
        }
    }
    setImageResource(if (profile) R.drawable.ic_avatar_placeholder else R.drawable.ic_product_placeholder)
}

