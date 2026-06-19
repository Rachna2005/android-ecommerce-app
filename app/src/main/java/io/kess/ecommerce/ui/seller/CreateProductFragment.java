package io.kess.ecommerce.ui.seller;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.kess.ecommerce.R;
import io.kess.ecommerce.databinding.FragmentCreateProductBinding;
import io.kess.ecommerce.model.Category;
import io.kess.ecommerce.model.Product;
import io.kess.ecommerce.model.ProductDetail;
import io.kess.ecommerce.model.ProductVariant;
import io.kess.ecommerce.model.Shop;
import io.kess.ecommerce.ui.adapter.ManageProductAdapter;
import io.kess.ecommerce.ui.adapter.VariantAdapter;
import io.kess.ecommerce.util.UiState;
import io.kess.ecommerce.view_model.CategoryViewModel;
import io.kess.ecommerce.view_model.ProductViewModel;
import io.kess.ecommerce.view_model.ShopViewModel;
import kotlin.Unit;


public class CreateProductFragment extends Fragment {
    private FragmentCreateProductBinding binding;
    private ProductViewModel productViewModel;
    private CategoryViewModel categoryVM;
    private ShopViewModel shopViewModel;
    private VariantAdapter variantAdapter;
    private Uri selectedImageUri;
    private List<Category> categoryList = new ArrayList<>();
    private String selectedCategoryId = null;
    private String selectedCategoryName = null;
    private boolean isEdit = false;
    private String productId;
    private String imageUrl;

    private ActivityResultLauncher<String> pickImage;
    private final List<ProductVariant> variants =
            new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pickImage = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        binding.productImage.setImageURI(uri);
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentCreateProductBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViewModel();
        checkEditMode();
        setupClickListener();
        setupVariantRecyclerView();
        observeData();

    }

    private void initViewModel() {
        productViewModel = new ViewModelProvider((requireActivity())).get(ProductViewModel.class);
        shopViewModel = new ViewModelProvider((requireActivity())).get(ShopViewModel.class);
        categoryVM = new ViewModelProvider(this).get(CategoryViewModel.class);
    }

    private void checkEditMode() {

        Bundle args = getArguments();

        if (args == null) {
            return;
        }

        productId = args.getString("PRODUCT_ID");

        if (productId != null) {

            isEdit = true;

            productViewModel.getProductDetail(
                    productId
            );

            productViewModel.getVariants(
                    productId
            );

            binding.tvTitle.setText(
                    "Edit Product"
            );

            binding.btnSaveProduct.setText(
                    "Update Product"
            );
        }
    }

    private void setupVariantRecyclerView() {

        variantAdapter = new VariantAdapter(

                (variant, position) -> {

                    VariantDialogFragment dialog =
                            new VariantDialogFragment(
                                    variant,
                                    position,
                                    new VariantDialogFragment.VariantListener() {
                                        @Override
                                        public void onVariantCreated(ProductVariant variant) {
                                        }

                                        @Override
                                        public void onVariantUpdated(
                                                ProductVariant updatedVariant,
                                                int position
                                        ) {
                                            variants.set(position, updatedVariant);

                                            variantAdapter.submitList(
                                                    new ArrayList<>(variants)
                                            );
                                        }
                                    }
                            );

                    dialog.show(
                            getParentFragmentManager(),
                            "edit_variant"
                    );

                    return Unit.INSTANCE;
                },

                variant -> {

                    variants.remove(variant);

                    variantAdapter.submitList(
                            new ArrayList<>(variants)
                    );
                    return Unit.INSTANCE;
                }
        );

        binding.recyVariant.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        binding.recyVariant.setAdapter(
                variantAdapter
        );
    }

    private void setupClickListener() {
        binding.btnUploadImage.setOnClickListener(v -> {
            pickImage.launch("image/*");
        });
        binding.btnBack.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });
        binding.btnAddVariant.setOnClickListener(v -> {
            VariantDialogFragment dialog =
                    new VariantDialogFragment(
                            null,
                            -1,
                            new VariantDialogFragment.VariantListener() {
                                @Override
                                public void onVariantCreated(
                                        ProductVariant variant
                                ) {
                                    variants.add(variant);

                                    variantAdapter.submitList(
                                            new ArrayList<>(variants)
                                    );
                                }

                                @Override
                                public void onVariantUpdated(
                                        ProductVariant variant,
                                        int position
                                ) {

                                }
                            }
                    );
            dialog.show(
                    getParentFragmentManager(),
                    "variant_dialog"
            );
        });
        binding.btnSaveProduct.setOnClickListener(v -> {
//            createProduct();
            if (isEdit) {

                updateProduct();

            } else {

                createProduct();
            }
        });
        binding.btnDiscard.setOnClickListener(v -> {

        });
        binding.dropdownCategory.setOnClickListener(v -> {
            showCategoryDialog();
        });

    }

    private void showCategoryDialog() {

        if (categoryList.isEmpty()) {
            Toast.makeText(requireContext(), "No categories found", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] names = new String[categoryList.size()];

        for (int i = 0; i < categoryList.size(); i++) {
            names[i] = categoryList.get(i).getName();
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Select Category")
                .setItems(names, (dialog, which) -> {
                    Category selected = categoryList.get(which);
                    selectedCategoryId = selected.getId();
                    selectedCategoryName = selected.getName();
                    binding.tvSelectedCategory.setText(selectedCategoryName);
                })
                .show();
    }

    private void showLoading(boolean show) {
        binding.loadingOverlay.setVisibility(
                show ? View.VISIBLE : View.GONE
        );

        binding.getRoot().setEnabled(!show);
    }

    private void observeData() {
//        productViewModel.getActionState().removeObserver(getViewLifecycleOwner());
        productViewModel.getActionState().observe(getViewLifecycleOwner(), state -> {
            if (state instanceof UiState.Loading) {
                showLoading(true);
            } else if (state instanceof UiState.Success) {
                String productId = ((UiState.Success<String>) state).getData();
                uploadVariants(productId);
            } else if (state instanceof UiState.Error) {
                showLoading(false);

            }
        });
        categoryVM.getCategories().observe(getViewLifecycleOwner(), categories -> {
            categoryList.clear();
            categoryList.addAll(categories);
        });

        productViewModel.getProductDetail()
                .observe(
                        getViewLifecycleOwner(),
                        state -> {

                            if (state instanceof UiState.Success) {

                                ProductDetail detail =
                                        ((UiState.Success<ProductDetail>) state)
                                                .getData();

                                fillProductInfo(
                                        detail.getProduct()
                                );
                            }
                        });
    }

    private void fillProductInfo(
            Product product
    ) {

        binding.etProductName.setText(
                product.getName()
        );

        binding.etDescription.setText(
                product.getDescription()
        );

        binding.etBasePrice.setText(
                String.valueOf(
                        product.getPrice()
                )
        );

        if (product.getDiscountPercentage() != null) {

            binding.etDiscount.setText(
                    String.valueOf(
                            product.getDiscountPercentage()
                    )
            );
        }

        selectedCategoryId =
                product.getCategoryId();

        imageUrl =
                product.getImage();

        Glide.with(this)
                .load(product.getImage())
                .into(binding.productImage);
    }

    private void uploadVariants(String productId) {

        if (variants == null || variants.isEmpty()) {
            showLoading(false);
            return;
        }

        final int total = variants.size();
        final int[] successCount = {0};
        final boolean[] hasError = {false};

        for (ProductVariant v : variants) {

            productViewModel.addVariant(productId, v);
        }

        productViewModel.getVariantActionState().observe(getViewLifecycleOwner(), state -> {

            if (state instanceof UiState.Success) {

                successCount[0]++;

                if (successCount[0] == total && !hasError[0]) {

                    showLoading(false);

                    Toast.makeText(requireContext(),
                            "Product created successfully",
                            Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack();
                }
            } else if (state instanceof UiState.Error) {

                hasError[0] = true;
                showLoading(false);

                Toast.makeText(requireContext(),
                        ((UiState.Error) state).getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createProduct() {
        String name =
                binding.etProductName.getText().toString().trim();

        String description =
                binding.etDescription.getText().toString().trim();

        String priceText =
                binding.etBasePrice.getText().toString().trim();
        String discount = binding.etDiscount.getText().toString().trim();
        if (name.isEmpty()) {
            binding.etProductName.setError("Product name required");
            return;
        }
        if (selectedImageUri == null) {
            Toast.makeText(requireContext(), "Select image first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (description.isEmpty()) {
            binding.etDescription.setError("Description required");
            return;
        }

        if (priceText.isEmpty()) {
            binding.etDiscount.setError("Price required");
            return;
        }
        if (selectedCategoryId == null) {
            Toast.makeText(requireContext(), "Please select category", Toast.LENGTH_SHORT).show();
            return;
        }
        uploadImage(
                selectedImageUri,
                new OnUploadSuccess() {
                    @Override
                    public void onSuccess(String imageUrl) {
                        double price =
                                Double.parseDouble(priceText);
                        double discountValue = 0.0;

                        if (!discount.isEmpty()) {
                            try {
                                discountValue = Double.parseDouble(discount);
                            } catch (NumberFormatException e) {
                                discountValue = 0.0;
                            }
                        }
                        Shop shop = shopViewModel.getCurrentShop();
                        String shopId = null;
                        if (shop != null) {
                            shopId = shop.getId();
                        }
                        Product product = new Product(
                                "",
                                shopId,
                                imageUrl,
                                name,
                                selectedCategoryId,
                                price,
                                discountValue,
                                description,
                                "ACTIVE",
                                0,
                                null
                        );
                        productViewModel.addProduct(product);
                    }
                },
                new OnUploadError() {
                    @Override
                    public void onError(String message) {

                        Toast.makeText(
                                requireContext(),
                                message,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void updateProduct() {

        double price =
                Double.parseDouble(
                        binding.etBasePrice
                                .getText()
                                .toString()
                );

        Double discount = null;

        String discountText =
                binding.etDiscount
                        .getText()
                        .toString()
                        .trim();

        if (!discountText.isEmpty()) {
            discount =
                    Double.parseDouble(
                            discountText
                    );
        }
        Shop shop = shopViewModel.getCurrentShop();
        String shopId = null;
        if (shop != null) {
            shopId = shop.getId();
        }

        Product product =
                new Product(
                        productId,
                        shopId,
                        imageUrl,
                        binding.etProductName
                                .getText()
                                .toString()
                                .trim(),
                        selectedCategoryId,
                        price,
                        discount,
                        binding.etDescription
                                .getText()
                                .toString()
                                .trim(),
                        "ACTIVE",
                        0,
                        null
                );

        productViewModel.updateProduct(
                productId,
                product
        );
    }

    private void uploadImage(
            Uri imageUri,
            OnUploadSuccess onSuccess,
            OnUploadError onError
    ) {
        MediaManager.get()
                .upload(imageUri)
                .unsigned("ecommerce_preset")
                .callback(
                        new UploadCallback() {
                            @Override
                            public void onStart(String requestId) {
                            }

                            @Override
                            public void onProgress(
                                    String requestId,
                                    long bytes,
                                    long totalBytes
                            ) {

                            }

                            @Override
                            public void onSuccess(
                                    String requestId,
                                    Map resultData
                            ) {

                                Object secureUrl =
                                        resultData.get("secure_url");

                                String imageUrl =
                                        secureUrl != null
                                                ? secureUrl.toString()
                                                : null;

                                if (imageUrl != null
                                        && !imageUrl.isEmpty()) {

                                    onSuccess.onSuccess(imageUrl);

                                } else {

                                    onError.onError(
                                            "Upload failed: empty URL"
                                    );
                                }
                            }

                            @Override
                            public void onError(
                                    String requestId,
                                    ErrorInfo error
                            ) {

                                onError.onError(
                                        error != null
                                                ? error.toString()
                                                : "Upload failed"
                                );
                            }

                            @Override
                            public void onReschedule(
                                    String requestId,
                                    ErrorInfo error
                            ) {

                            }
                        }
                )
                .dispatch();
    }

    public interface OnUploadSuccess {
        void onSuccess(String imageUrl);
    }

    public interface OnUploadError {
        void onError(String message);
    }

    public void onResume() {
        super.onResume();
        ((ShopActivity) getActivity()).showButtonNav(false);
    }

    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        ((ShopActivity) getActivity()).showButtonNav(true);
    }
}