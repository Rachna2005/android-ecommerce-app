package io.kess.ecommerce.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.kess.ecommerce.R
import io.kess.ecommerce.model.CartItem
import io.kess.ecommerce.model.ShopCartGroup
import java.security.PrivateKey

class CartShopAdapter(
    private var loadingItems: Set<String>,
    private val onProductClick: (CartItem) -> Unit,
    private val onIncrease: (CartItem) -> Unit,
    private val onDecrease: (CartItem) -> Unit,
    private val onDelete: (CartItem) -> Unit,
    private val onCheckout: (ShopCartGroup) -> Unit
) : ListAdapter<ShopCartGroup, CartShopAdapter.ShopVH>(DiffCallback()) {
    fun updateLoading(newLoading: Set<String>) {
//        loadingItems = newLoading
//        notifyDataSetChanged()
        val oldLoading = loadingItems
        loadingItems = newLoading
        val changedItems = oldLoading + newLoading
        val changedShopPositions = mutableSetOf<Int>()
        changedItems.forEach { itemId ->
            val shopIndex = currentList.indexOfFirst { shop ->
                shop.items.any { it.id == itemId }
            }

            if (shopIndex != -1) {
                changedShopPositions.add(shopIndex)
            }
        }

        changedShopPositions.forEach { pos ->
            notifyItemChanged(pos)
        }
    }

    inner class ShopVH(view: View) : RecyclerView.ViewHolder(view) {

        val shopName: TextView = view.findViewById(R.id.shopName)
        val recyclerItems: RecyclerView = view.findViewById(R.id.recyclerView)
        val btnCheckout: TextView = view.findViewById(R.id.btnCheckout)
        val price: TextView = view.findViewById(R.id.totalPrice)
        fun bind(shop: ShopCartGroup) {
            shopName.text = "#${shop.shopName}"

            val itemAdapter = CartAdapter(
                onIncrease = onIncrease,
                onDecrease = onDecrease,
                onDelete = onDelete,
                onProductClick = onProductClick,
                loadingItems = loadingItems,
            )

            recyclerItems.adapter = itemAdapter
            itemAdapter.submitList(shop.items)
            btnCheckout.setOnClickListener {
                onCheckout(shop)
            }
            val totalPrice = shop.items.sumOf { item ->
                item.price * item.quantity
            }
            price.text = "$${String.format("%.2f", totalPrice)}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopVH {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart_shop, parent, false)

        return ShopVH(view)
    }

    override fun onBindViewHolder(holder: ShopVH, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<ShopCartGroup>() {

        override fun areItemsTheSame(
            oldItem: ShopCartGroup,
            newItem: ShopCartGroup
        ): Boolean {
            return oldItem.shopId == newItem.shopId
        }
        override fun areContentsTheSame(
            oldItem: ShopCartGroup,
            newItem: ShopCartGroup
        ): Boolean {
            return oldItem == newItem
        }
    }
}