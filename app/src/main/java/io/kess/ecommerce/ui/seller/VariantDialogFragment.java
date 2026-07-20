
package io.kess.ecommerce.ui.seller;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import io.kess.ecommerce.R;
import io.kess.ecommerce.model.ProductVariant;

public class VariantDialogFragment extends DialogFragment {

    private final ProductVariant variant;
    private final int position;
    private final VariantListener listener;

    public VariantDialogFragment(
            ProductVariant variant,
            int position,
            VariantListener listener
    ) {
        this.variant = variant;
        this.position = position;
        this.listener = listener;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.variant_dialog, null);

        EditText etColor = view.findViewById(R.id.et_color);
        EditText etSize = view.findViewById(R.id.et_size);
        EditText etStock = view.findViewById(R.id.et_stock);

        Button btnCreate = view.findViewById(R.id.btn_create);
        ImageView btnClose = view.findViewById(R.id.close);

        boolean isEdit = variant != null;

        if (isEdit) {
            etColor.setText(variant.getColor());
            etSize.setText(variant.getSize());
            etStock.setText(String.valueOf(variant.getStock()));

            btnCreate.setText("Update Variant");
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(view)
                .create();

        btnClose.setOnClickListener(v -> dismiss());

        btnCreate.setOnClickListener(v -> {

            String color = etColor.getText()
                    .toString()
                    .trim();

            String size = etSize.getText()
                    .toString()
                    .trim();
            String stockText = etStock.getText()
                    .toString()
                    .trim();
//            if (color.isEmpty()) {
//                etColor.setError("Color is required");
//                return;
//            }

//            if (size.isEmpty()) {
//                etSize.setError("Size is required");
//                return;
//            }

            if (stockText.isEmpty()) {
                etStock.setError("Stock is required");
                return;
            }

            int stock;

            try {
                stock = Integer.parseInt(stockText);
            } catch (NumberFormatException e) {
                etStock.setError("Invalid stock value");
                return;
            }

            ProductVariant newVariant = new ProductVariant(
                    isEdit ? variant.getId() : "",
                    color,
                    null,
                    true,
                    size,
                    stock
            );

            if (isEdit) {

                listener.onVariantUpdated(
                        newVariant,
                        position
                );

            } else {

                listener.onVariantCreated(
                        newVariant
                );
            }

            dismiss();
        });

        return dialog;
    }

    public interface VariantListener {
        void onVariantCreated(ProductVariant variant);

        void onVariantUpdated(
                ProductVariant variant,
                int position
        );
    }
}

