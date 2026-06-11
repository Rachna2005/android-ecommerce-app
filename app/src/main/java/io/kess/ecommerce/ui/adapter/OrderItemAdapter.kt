package io.kess.ecommerce.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import io.kess.ecommerce.R
import io.kess.ecommerce.model.CartItem
import io.kess.ecommerce.model.OrderItem

class OrderItemAdapter :
    ListAdapter<OrderItem, OrderItemAdapter.OrderViewHolder>(DiffCallback) {
    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.image)
        val title: TextView = itemView.findViewById(R.id.title)
        val variant: TextView = itemView.findViewById(R.id.variant)
        val price: TextView = itemView.findViewById(R.id.price)
        val quantity: TextView = itemView.findViewById(R.id.quantity)
        val totalPrice: TextView = itemView.findViewById(R.id.totalPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.order_item, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val item = getItem(position)

        val total = item.price * item.quantity

        holder.title.text = item.name
        holder.variant.text = "${item.selectorColor}, ${item.selectSize}"
        holder.price.text = item.price.toString()
        holder.quantity.text = item.quantity.toString()
        holder.totalPrice.text = "$${String.format("%.2f", total)}"

        Glide.with(holder.itemView.context)
            .load(item.image)
            .into(holder.image)
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<OrderItem>() {
            override fun areItemsTheSame(oldItem: OrderItem, newItem: OrderItem): Boolean {
                return oldItem.id == newItem.id
            }
            override fun areContentsTheSame(oldItem: OrderItem, newItem: OrderItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}