package io.kess.ecommerce.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import io.kess.ecommerce.R
import io.kess.ecommerce.model.Category
import io.kess.ecommerce.model.Product
import android.graphics.Paint
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil

import androidx.recyclerview.widget.ListAdapter

class ProductAdapter(
    private var favoriteIds: Set<String>,
    private var loadingFavorite: Set<String>,
    private val onFavoriteClick: (Product) -> Unit,
    private val onProductClick: (Product) -> Unit
) : PagingDataAdapter<Product, ProductAdapter.ViewHolder>(DiffCallback()) {

    fun updateFavorites(newFavorites: Set<String>) {
        favoriteIds = newFavorites
        notifyDataSetChanged()
    }

    fun updateLoadingFavorite(id: Set<String>){
        loadingFavorite = id
        notifyDataSetChanged()
    }

    class DiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val container = view.findViewById<LinearLayout>(R.id.container)
        val btnFavorite = view.findViewById<ImageView>(R.id.btnWishlist)
        val img = view.findViewById<ImageView>(R.id.imgProduct)
        val name = view.findViewById<TextView>(R.id.txtName)
        val originalPrice = view.findViewById<TextView>(R.id.txtOldPrice)
        val discountPrice = view.findViewById<TextView>(R.id.txtPrice)
        val discount = view.findViewById<TextView>(R.id.discountBadge)
        val favoriteLoading = view.findViewById<ProgressBar>(R.id.favoriteLoading)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ViewHolder(view)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = getItem(position) ?: return

        val discount = product.price * ((product.discountPercentage ?: 0.0) / 100)
        val priceAfterDiscount = product.price - discount
        val hasDiscount = (product.discountPercentage ?: 0.0) > 0

        Glide.with(holder.itemView.context).load(product.image).into(holder.img)
        holder.name.text = product.name
        if (favoriteIds.contains(product.id)) {
            holder.btnFavorite.setImageResource(R.drawable.ic_heart_fill)
        } else {
            holder.btnFavorite.setImageResource(R.drawable.ic_heart)
        }
        val isLoading = loadingFavorite.contains(product.id)
        holder.favoriteLoading.visibility = if(isLoading) View.VISIBLE else View.GONE
        holder.btnFavorite.visibility =
            if (isLoading) View.INVISIBLE else View.VISIBLE

        holder.btnFavorite.isEnabled = !isLoading

        holder.btnFavorite.setOnClickListener {
            onFavoriteClick(product)
        }
        holder.container.setOnClickListener {
            onProductClick(product)
        }
//                holder.price.text = "$${String.format("%.2f", product.price)}"
        if (hasDiscount) {
            holder.originalPrice.text = "$${String.format("%.2f", product.price)}"
            holder.originalPrice.paintFlags =
                holder.originalPrice.paintFlags or
                        Paint.STRIKE_THRU_TEXT_FLAG
            holder.originalPrice.visibility = View.VISIBLE
            holder.discountPrice.text = "$${String.format("%.2f", priceAfterDiscount)}"

            holder.discount.text = "-${product.discountPercentage?.toInt()}%"
        } else {
            holder.originalPrice.visibility = View.GONE
            holder.discountPrice.text = "$${String.format("%.2f", product.price)}"
            holder.originalPrice.paintFlags = holder.originalPrice.paintFlags and
                    Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.discount.visibility = View.GONE
        }
    }
}

