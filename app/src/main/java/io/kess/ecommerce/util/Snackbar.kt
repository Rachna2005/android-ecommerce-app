package io.kess.ecommerce.util

import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import com.google.android.material.snackbar.Snackbar

fun showSnackBar(
    view: View,
    message: String,
    backgroundColor: Int,
    textColor: Int,
    gravity: Int = Gravity.TOP
) {

    val snackbar = Snackbar.make(
        view,
        message,
        Snackbar.LENGTH_SHORT
    )

    snackbar.setBackgroundTint(backgroundColor)
    snackbar.setTextColor(textColor)

    val snackbarView = snackbar.view

    val params = snackbarView.layoutParams as FrameLayout.LayoutParams
    params.gravity = gravity
    params.setMargins(16, 100, 16, 0)

    snackbarView.layoutParams = params

    snackbar.show()
}