package io.kess.ecommerce.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import io.kess.ecommerce.R
import io.kess.ecommerce.model.Shop

class ShopAdapter(
    private val onShopClick: (Shop) -> Unit
) : ListAdapter<Shop, ShopAdapter.ViewHolder>(DiffCallback()) {

    class DiffCallback : DiffUtil.ItemCallback<Shop>() {
        override fun areItemsTheSame(
            oldItem: Shop,
            newItem: Shop
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: Shop,
            newItem: Shop
        ): Boolean {
            return oldItem == newItem
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.img)
        val title: TextView = view.findViewById(R.id.shopName)
        val address: TextView = view.findViewById(R.id.location)
        val container: CardView = view.findViewById(R.id.container)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.shop_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val shop = getItem(position)

        holder.title.text = shop.shopName
        holder.address.text = shop.address

        Glide.with(holder.itemView.context)
            .load(shop.logoUrl)
            .into(holder.image)

        holder.container.setOnClickListener {
            onShopClick(shop)
        }
    }
}