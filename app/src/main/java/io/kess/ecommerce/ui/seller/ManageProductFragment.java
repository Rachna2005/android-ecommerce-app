package io.kess.ecommerce.ui.seller;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;

import io.kess.ecommerce.databinding.FragmentManageProductBinding;
import io.kess.ecommerce.model.Product;
import io.kess.ecommerce.model.Shop;
import io.kess.ecommerce.ui.ProductDetailFragment;
import io.kess.ecommerce.ui.adapter.ManageProductAdapter;
import io.kess.ecommerce.util.UiState;
import io.kess.ecommerce.view_model.ProductViewModel;
import io.kess.ecommerce.view_model.ShopViewModel;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class ManageProductFragment extends Fragment {
    private FragmentManageProductBinding binding;
    private ProductViewModel productViewModel;
    private ManageProductAdapter productAdapter;
    private ShopViewModel shopViewModel;
    private String shopId;

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
        initViewModel();
        setupClickListener();
        setupRecyclerView();
        observeData();
    }

    private void initViewModel() {
        productViewModel = new ViewModelProvider((requireActivity())).get(ProductViewModel.class);
        shopViewModel = new ViewModelProvider((requireActivity())).get(ShopViewModel.class);
    }

    private void setupClickListener() {
        binding.createProduct.setOnClickListener(v -> {
                    ((ShopActivity) getActivity()).navigate(new CreateProductFragment());
                }
        );
        binding.addProduct.setOnClickListener(v -> {
            ((ShopActivity) getActivity()).navigate(new CreateProductFragment());
        });
    }

    private void setupRecyclerView() {
        productAdapter = new ManageProductAdapter(new Function1<Product, Unit>() {
            @Override
            public Unit invoke(Product product) {

                openEditProduct(product);

                return Unit.INSTANCE;
            }
        },
                new Function1<Product, Unit>() {
                    @Override
                    public Unit invoke(Product product) {

                        productViewModel.deleteProduct(product.getId());

                        return Unit.INSTANCE;
                    }
                },
                new Function1<Product, Unit>() {
                    @Override
                    public Unit invoke(Product product) {

//                        Bundle bundle = new Bundle();
//                        bundle.putString("PRODUCT_ID", product.getId());
//
//                        ProductDetailFragment fragment = new ProductDetailFragment();
//                        fragment.setArguments(bundle);
//
//                        ((ShopActivity) requireActivity()).navigate(fragment);
//
                        return Unit.INSTANCE;
                    }
                }
        );
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(productAdapter);
    }

    private void openEditProduct(Product product){
        CreateProductFragment fragment = new CreateProductFragment();
        Bundle bundle = new Bundle();
        bundle.putString("Product_Id", product.getId());
        fragment.setArguments(bundle);
        ((ShopActivity) requireActivity()).navigate(fragment);
    }


    private void observeData() {
        shopViewModel.getShopState().observe(
                getViewLifecycleOwner(),
                new Observer<UiState<Shop>>() {
                    @Override
                    public void onChanged(UiState<Shop> state) {

                        if (state instanceof UiState.Loading) {

                        } else if (state instanceof UiState.Success) {

                            Shop shop = ((UiState.Success<Shop>) state).getData();
                            shopId = shop.getId();
                            productViewModel.observeProductsByShop(shopId);
                        } else if (state instanceof UiState.Error) {
                            String msg = ((UiState.Error) state).getMessage();

                            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        productViewModel.getProducts().observe(
                getViewLifecycleOwner(),
                new Observer<UiState<List<Product>>>() {
                    @Override
                    public void onChanged(UiState<List<Product>> state) {

                        if (state instanceof UiState.Loading) {
                            binding.progressBar.setVisibility(View.VISIBLE);
                            binding.layoutEmptyProduct.setVisibility(View.GONE);
                            binding.search.setVisibility(View.VISIBLE);

                        } else if (state instanceof UiState.Success) {

                            UiState.Success<List<Product>> success =
                                    (UiState.Success<List<Product>>) state;
                            List<Product> products = success.getData();
                            productAdapter.submitList(products);
                            binding.progressBar.setVisibility(View.GONE);
                            if (products == null || products.isEmpty()) {
                                binding.layoutEmptyProduct.setVisibility(View.VISIBLE);
                                binding.search.setVisibility(View.GONE);
                            }

                        } else if (state instanceof UiState.Error) {
                            String msg = ((UiState.Error) state).getMessage();
                            binding.progressBar.setVisibility(View.GONE);
                            binding.layoutEmptyProduct.setVisibility(View.GONE);
                            binding.search.setVisibility(View.VISIBLE);
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}