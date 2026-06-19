package io.kess.ecommerce.ui.bottomSheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.kess.ecommerce.R

class AddressBottomSheet() :
    BottomSheetDialogFragment() {
    private var address: String = ""
    private var phone: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        address = arguments?.getString("ADDRESS", "") ?: ""
        phone = arguments?.getString("PHONE", "") ?: ""
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val addressInput = view.findViewById<EditText>(R.id.inputAddress)
        val phoneInput = view.findViewById<EditText>(R.id.inputPhone)
        val btnAdd = view.findViewById<Button>(R.id.btnAdd)
        addressInput.setText(address)
        phoneInput.setText(phone)
        btnAdd.setOnClickListener {
            val bundle = Bundle().apply {
                putString("ADDRESS", addressInput.text.toString())
                putString("PHONE", phoneInput.text.toString())
            }
            parentFragmentManager.setFragmentResult(
                "ADDRESS_RESULT", bundle
            )
            dismiss()
        }
    }
}