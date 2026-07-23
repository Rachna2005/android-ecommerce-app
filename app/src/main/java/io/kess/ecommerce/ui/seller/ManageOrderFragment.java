package io.kess.ecommerce.ui.seller;

import android.app.AlertDialog;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import io.kess.ecommerce.databinding.FragmentManageOrderBinding;
import io.kess.ecommerce.model.Order;
import io.kess.ecommerce.model.Shop;
import io.kess.ecommerce.ui.adapter.OrderAdapter;
import io.kess.ecommerce.util.UiState;
import io.kess.ecommerce.view_model.OrderViewModel;
import io.kess.ecommerce.view_model.ShopViewModel;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class ManageOrderFragment extends Fragment {
    private FragmentManageOrderBinding binding;
    private OrderViewModel orderViewModel;
    private ShopViewModel shopViewModel;
    private OrderAdapter orderAdapter;
    private List<Order> orderList;
    private String shopId;

    public ManageOrderFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentManageOrderBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        initViewModel();
        setupClickListener();
        setupRecyclerView();
        observeData();
    }

    private void initViewModel() {
        orderViewModel = new ViewModelProvider((requireActivity())).get(OrderViewModel.class);
        shopViewModel = new ViewModelProvider((requireActivity())).get(ShopViewModel.class);

    }

    private void setupClickListener() {
        binding.filterContainer.setOnClickListener(v -> {
            orderStatusDialog(status -> {
                binding.filterStatus.setText((status));
                filterOrders(status);
            });
        });
    }

    private void filterOrders(String status){
        if(status.equals("ALL")){
            orderAdapter.submitList(new ArrayList<>(orderList));
            return;
        }
        List<Order> filtered = new ArrayList<>();
        for (Order order : orderList){
            if (status.equals(order.getStatus())){
                filtered.add(order);
            }
        }
        orderAdapter.submitList(filtered);
    }

    private void setupRecyclerView() {
        orderAdapter = new OrderAdapter(
                new Function1<Order, Unit>() {
                    @Override
                    public Unit invoke(Order order) {
                        openOrderDetail(order);
                        return Unit.INSTANCE;
                    }
                }
        );

        binding.recyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );
        binding.recyclerView.setAdapter(orderAdapter);
    }
    private void openOrderDetail(Order order) {
        OrderDetailFragment fragment = new OrderDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putString("ID", order.getId());
        fragment.setArguments(bundle);
        ((ShopActivity) requireActivity()).navigate(fragment);
    }

    private void orderStatusDialog(OnStatusSelectedListener listener) {
        String[] status = {"ALL", "PROCESSING", "CONFIRMED", "DELIVERED"};

        new AlertDialog.Builder(requireContext())
                .setTitle("Filter Status")
                .setItems(status, (dialog, which) -> {
                    String selectedStatus = status[which];
                    listener.onSelected(selectedStatus);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void observeData() {
        shopViewModel.getShopState().observe(getViewLifecycleOwner(), new Observer<UiState<Shop>>() {
            @Override
            public void onChanged(UiState<Shop> state) {
                if (state instanceof UiState.Loading) {
                } else if (state instanceof UiState.Success) {
                    Shop shop = ((UiState.Success<Shop>) state).getData();
                    shopId = shop.getId();
                    orderViewModel.getOrderByShop(shopId);
                } else if (state instanceof UiState.Error) {
                    String msg = ((UiState.Error) state).getMessage();
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                }
            }
        });

        orderViewModel.getOrders().observe(getViewLifecycleOwner(), new Observer<UiState<List<Order>>>() {
            @Override
            public void onChanged(UiState<List<Order>> state) {

                if (state instanceof UiState.Loading) {
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.layoutEmptyProduct.setVisibility(View.GONE);


                } else if (state instanceof UiState.Success) {

                    UiState.Success<List<Order>> success = (UiState.Success<List<Order>>) state;
                    List<Order> orders = success.getData();
                    orderList = orders;
                    orderAdapter.submitList(orders);
                    binding.progressBar.setVisibility(View.GONE);
                    if (orders == null || orders.isEmpty()) {
                        binding.layoutEmptyProduct.setVisibility(View.VISIBLE);

                    }

                } else if (state instanceof UiState.Error) {
                    String msg = ((UiState.Error) state).getMessage();
                    binding.progressBar.setVisibility(View.GONE);
                    binding.layoutEmptyProduct.setVisibility(View.GONE);

                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    public interface OnStatusSelectedListener {
        void onSelected(String status);
    }
}

