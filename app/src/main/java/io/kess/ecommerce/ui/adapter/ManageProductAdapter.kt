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
import io.kess.ecommerce.model.Product


internal interface OnProductClick {
    fun onClick(product: Product?)
}
class ManageProductAdapter(
    private val onEditClick: (Product) -> Unit,
    private val onDeleteClick: (Product) -> Unit,
    private val onProductClick: (Product) -> Unit
) : ListAdapter<Product, ManageProductAdapter.ViewHolder>(DiffCallback()) {

    class DiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img = view.findViewById<ImageView>(R.id.iv_product)
        val name = view.findViewById<TextView>(R.id.tv_product_name)

        val price = view.findViewById<TextView>(R.id.tv_price)
        val stock = view.findViewById<TextView>(R.id.tv_stock)
        val btnEdit = view.findViewById<ImageView>(R.id.btn_edit)
        val btnDelete = view.findViewById<ImageView>(R.id.btn_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_manage_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = getItem(position)

        Glide.with(holder.itemView.context)
            .load(product.image)
            .into(holder.img)

        holder.name.text = product.name
        holder.price.text = "$${String.format("%.2f", product.price)}"

        holder.stock.text = "Stock: ${product.totalStock}"

        holder.itemView.setOnClickListener {
            onProductClick(product)
        }

        holder.btnEdit.setOnClickListener {
            onEditClick(product)
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(product)
        }
    }
}