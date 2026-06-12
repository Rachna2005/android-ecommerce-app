package io.kess.ecommerce.ui.seller;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.kess.ecommerce.R;
import io.kess.ecommerce.databinding.FragmentManageProductBinding;
import io.kess.ecommerce.ui.MainActivity;

public class ManageProductFragment extends Fragment {
    private FragmentManageProductBinding binding;

    public ManageProductFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentManageProductBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view,
                             Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        setupClickListener();
    }


    private void setupClickListener() {
        binding.createProduct.setOnClickListener(v -> {
                    ((ShopActivity) getActivity()).navigate(new CreateProductFragment());
                }
        );
    }


    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}