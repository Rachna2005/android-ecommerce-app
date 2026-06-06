package io.kess.ecommerce.ui

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.kess.ecommerce.R

class SuccessPaymentSheet : BottomSheetDialogFragment() {
    private var openViewOrderHistory = false
    var onGoHome: (() -> Unit)? = null
    var onGoOrderHistory: (() -> Unit)? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.success_bottom_sheet,container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val btn = view.findViewById<Button>(R.id.btn)
        btn.setOnClickListener {
            openViewOrderHistory = true
            dismiss()
            onGoOrderHistory?.invoke()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if(!openViewOrderHistory){
            onGoHome?.invoke()
        }
    }
}