package io.kess.ecommerce.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import io.kess.ecommerce.R
import io.kess.ecommerce.model.CartItem

class CartAdapter(
    private var loadingItems: Set<String>,
    private val onProductClick: (CartItem) -> Unit,
    private val onIncrease: (CartItem) -> Unit,
    private val onDecrease: (CartItem) -> Unit,
    private val onDelete: (CartItem) -> Unit
) : ListAdapter<CartItem, CartAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val container = view.findViewById<RelativeLayout>(R.id.container)
        val btnDelete = view.findViewById<ImageView>(R.id.btn_delete)
        val img = view.findViewById<ImageView>(R.id.image)

        val title = view.findViewById<TextView>(R.id.title)
        val variant = view.findViewById<TextView>(R.id.variant)

        val price = view.findViewById<TextView>(R.id.price)
        val quantity = view.findViewById<TextView>(R.id.quantity)
        val quantityNum = view.findViewById<TextView>(R.id.quantityNum)
        val totalPrice = view.findViewById<TextView>(R.id.totalPrice)

        val increase = view.findViewById<TextView>(R.id.increase)
        val decrease = view.findViewById<TextView>(R.id.decrease)

    }

    class DiffCallback : DiffUtil.ItemCallback<CartItem>() {

        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.product_item_cart, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val cartItem = getItem(position)

        Glide.with(holder.itemView.context)
            .load(cartItem.image)
            .into(holder.img)

        holder.title.text = cartItem.name

        holder.variant.text =
            "${cartItem.selectorColor}, ${cartItem.selectSize}"

        holder.price.text = "$${String.format("%.2f", cartItem.price)}"

        holder.quantityNum.text = cartItem.quantity.toString()
        holder.quantity.text = cartItem.quantity.toString()

        val total = cartItem.price * cartItem.quantity
        holder.totalPrice.text = "$${String.format("%.2f", total)}"
        holder.decrease.isEnabled = cartItem.quantity > 1
        holder.decrease.alpha = if (cartItem.quantity > 1) 1f else 0.5f

        val isLoading = loadingItems.contains(cartItem.id)
        holder.container.isEnabled = !isLoading
        holder.container.alpha = if(isLoading) 0.5f else 1f

        holder.container.setOnClickListener {
            onProductClick(cartItem)
        }

        holder.increase.setOnClickListener {
            onIncrease(cartItem)
        }

        holder.decrease.setOnClickListener {
            onDecrease(cartItem)
        }

        holder.btnDelete.setOnClickListener {
            onDelete(cartItem)
        }
    }
}