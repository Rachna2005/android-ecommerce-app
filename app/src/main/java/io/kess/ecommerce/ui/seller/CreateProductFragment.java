package io.kess.ecommerce.ui.seller;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.kess.ecommerce.R;
import io.kess.ecommerce.databinding.FragmentCreateProductBinding;

public class CreateProductFragment extends Fragment {
    private FragmentCreateProductBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentCreateProductBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onDestroyView(){
        super.onDestroyView();
        binding = null;
    }
}