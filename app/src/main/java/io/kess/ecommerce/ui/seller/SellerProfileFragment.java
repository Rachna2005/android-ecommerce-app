package io.kess.ecommerce.ui.seller;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import io.kess.ecommerce.R;
import io.kess.ecommerce.databinding.FragmentSellerProfileBinding;
import io.kess.ecommerce.model.Shop;
import io.kess.ecommerce.model.User;
import io.kess.ecommerce.ui.activity.LoginActivity;
import io.kess.ecommerce.util.UiState;
import io.kess.ecommerce.view_model.AuthViewModel;
import io.kess.ecommerce.view_model.ShopViewModel;

public class SellerProfileFragment extends Fragment {
    private FragmentSellerProfileBinding binding;
    private ShopViewModel shopViewModel;
    private AuthViewModel authViewModel;

    public SellerProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSellerProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view,
                              Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        initViewModel();
        observeData();
        setupClickListener();
    }
    private void initViewModel() {
        shopViewModel = new ViewModelProvider((requireActivity())).get(ShopViewModel.class);
        authViewModel = new ViewModelProvider((requireActivity())).get(AuthViewModel.class);
    }

    private void observeData(){
        authViewModel.getAuthState().observe(
                getViewLifecycleOwner(),
                state -> {
                    if (state instanceof UiState.Success) {
                        User user =
                                ((UiState.Success<User>) state)
                                        .getData();
                        binding.uName.setText(
                                user.getName()
                        );

                        binding.email.setText(
                                user.getEmail()
                        );
                    }
                }
        );

        shopViewModel.getShopState().observe(
                getViewLifecycleOwner(),
                state -> {

                    if (state instanceof UiState.Loading) {

                        // show loading if needed
                    }

                    else if (state instanceof UiState.Success) {

                        Shop shop =
                                ((UiState.Success<Shop>) state)
                                        .getData();

                        setupUi(shop);
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
    }

    private void setupUi(Shop shop){
        binding.shopName.setText(shop.getShopName());

        Glide.with(this)
                .load(shop.getLogoUrl())
                .into(binding.profile);
    }

    private void showDialog() {
        View view = getLayoutInflater().inflate(R.layout.logout_dialog, null);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(view)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialog.setCanceledOnTouchOutside(false);

        Button btnYes = view.findViewById(R.id.btnYes);
        Button btnNo = view.findViewById(R.id.btnNo);

        btnYes.setOnClickListener(v -> {
            dialog.dismiss();
            authViewModel.logout();

            Intent intent = new Intent(requireContext(), LoginActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });

        btnNo.setOnClickListener(v -> dialog.dismiss());

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.75),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private void setupClickListener(){
        binding.shopInfo.setOnClickListener(v -> {
            ((ShopActivity) getActivity()).navigate(new EditShopInfoFragment());
        });
        binding.btnLogout.setOnClickListener(v -> {
            showDialog();
        });
    }

    public void onDestroyView(){
        super.onDestroyView();
        binding = null;
    }
}