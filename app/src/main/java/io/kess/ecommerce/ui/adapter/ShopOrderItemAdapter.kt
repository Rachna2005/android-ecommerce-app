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
import io.kess.ecommerce.model.cartToOrderItem
import java.security.PrivateKey

class ShopOrderItemAdapter(
) : ListAdapter<ShopCartGroup, ShopOrderItemAdapter.ShopVH>(DiffCallback()) {
    inner class ShopVH(view: View) : RecyclerView.ViewHolder(view) {

        val shopName: TextView = view.findViewById(R.id.shopName)
        val recyclerItems: RecyclerView = view.findViewById(R.id.recyclerView)
        val btnCheckout: TextView = view.findViewById(R.id.btnCheckout)
        val price: TextView = view.findViewById(R.id.totalPrice)
        fun bind(shop: ShopCartGroup) {
            shopName.text = "#${shop.shopName}"

            val orderAdapter = OrderItemAdapter()
            recyclerItems.adapter = orderAdapter
            orderAdapter.submitList(shop.items.map { cartToOrderItem(it) })
            val totalPrice = shop.items.sumOf { item ->
                item.price * item.quantity
            }
            btnCheckout.visibility = View.GONE
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