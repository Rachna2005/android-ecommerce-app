package io.kess.ecommerce.ui.seller;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import io.kess.ecommerce.R;
import io.kess.ecommerce.databinding.FragmentEditShopInfoBinding;
import io.kess.ecommerce.model.Shop;
import io.kess.ecommerce.util.UiState;
import io.kess.ecommerce.view_model.AuthViewModel;
import io.kess.ecommerce.view_model.CategoryViewModel;
import io.kess.ecommerce.view_model.ProductViewModel;
import io.kess.ecommerce.view_model.ShopViewModel;

public class EditShopInfoFragment extends Fragment {
private FragmentEditShopInfoBinding binding;
    private ShopViewModel shopViewModel;
    private Shop originalShop;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEditShopInfoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViewModel();
        setupClickListener();
        observeData();
    }

    private void initViewModel() {
        shopViewModel = new ViewModelProvider((requireActivity())).get(ShopViewModel.class);

    }
    private void observeData(){
        shopViewModel.getShopState().observe(
                getViewLifecycleOwner(),
                state -> {
                    if (state instanceof UiState.Loading) {

                    }
                    else if (state instanceof UiState.Success) {

                        Shop shop =
                                ((UiState.Success<Shop>) state)
                                        .getData();
                        originalShop = shop;
                        fillInfo(shop);
                    }
                    else if (state instanceof UiState.Error) {

                        Toast.makeText(
                                requireContext(),
                                ((UiState.Error) state).getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
        shopViewModel.getActionState().observe(
                getViewLifecycleOwner(),
                state -> {
                    if (state instanceof UiState.Loading) {
                        showLoading(true);
                    }
                    else if (state instanceof UiState.Success) {
                        showLoading(false);
//                        Shop shop =
//                                ((UiState.Success<String>) state)
//                                        .getData();
                        Toast.makeText(
                                requireContext(),
                                "Update Successful",
                                Toast.LENGTH_SHORT
                        ).show();
                        shopViewModel.clearState();
                        getParentFragmentManager().popBackStack();

                    }
                    else if (state instanceof UiState.Error) {
                        showLoading(false);
                        Toast.makeText(
                                requireContext(),
                                ((UiState.Error) state).getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }
    private void showLoading(Boolean show){
        if(show){
            binding.progressBar.setVisibility(View.VISIBLE);

            binding.btnContainer.setVisibility(View.GONE);
            binding.contentContainer.setVisibility(View.GONE);
        }else {
            binding.progressBar.setVisibility(View.GONE);

            binding.btnContainer.setVisibility(View.VISIBLE);
            binding.contentContainer.setVisibility(View.VISIBLE);
        }
    }

    private void setupClickListener(){
        binding.btnSaveProduct.setOnClickListener(v -> {
            updateShop();
        });
        binding.delete.setOnClickListener(v -> {

        });
        binding.btnBack.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });
    }

    private void updateShop() {

        if (originalShop == null) {
            return;
        }

        String shopName =
                binding.inputName.getText()
                        .toString()
                        .trim();

        String description =
                binding.inputShopDescription.getText()
                        .toString()
                        .trim();

        String phone =
                binding.inputPhone.getText()
                        .toString()
                        .trim();

        String address =
                binding.inputLocation.getText()
                        .toString()
                        .trim();

        // Validation
        if (shopName.isEmpty()) {
            binding.inputName.setError("Shop name is required");
            return;
        }

        if (description.isEmpty()) {
            binding.inputShopDescription.setError(
                    "Description is required"
            );
            return;
        }

        if (phone.isEmpty()) {
            binding.inputPhone.setError(
                    "Phone is required"
            );
            return;
        }

        if (address.isEmpty()) {
            binding.inputLocation.setError(
                    "Address is required"
            );
            return;
        }

        String updateName = null;
        String updateDescription = null;
        String updatePhone = null;
        String updateAddress = null;

        if (!shopName.equals(originalShop.getShopName())) {
            updateName = shopName;
        }

        if (!description.equals(originalShop.getDescription())) {
            updateDescription = description;
        }

        if (!phone.equals(originalShop.getPhone())) {
            updatePhone = phone;
        }

        if (!address.equals(originalShop.getAddress())) {
            updateAddress = address;
        }

        boolean hasChanges =
                updateName != null
                        || updateDescription != null
                        || updatePhone != null
                        || updateAddress != null;

        if (!hasChanges) {
            Toast.makeText(
                    requireContext(),
                    "No changes detected",
                    Toast.LENGTH_SHORT
            ).show();
//            getParentFragmentManager().popBackStack();
            return;
        }
        shopViewModel.updateShop(
                originalShop.getId(),
                updateName,
                updateDescription,
                updatePhone,
                updateAddress,
                null
        );
    }

    private void fillInfo(Shop shop){
        binding.inputName.setText(shop.getShopName());
        binding.inputShopDescription.setText(shop.getDescription());
        binding.inputPhone.setText(shop.getPhone());
        binding.inputLocation.setText(shop.getAddress());
        Glide.with(requireView())
                .load(shop.getLogoUrl())
                .into(binding.imgBanner);
    }

    public void onResume() {
        super.onResume();
        ((ShopActivity) getActivity()).showButtonNav(false);
    }

    public void onDestroyView(){
        super.onDestroyView();
        binding = null;
        ((ShopActivity) getActivity()).showButtonNav(true);
    }
}