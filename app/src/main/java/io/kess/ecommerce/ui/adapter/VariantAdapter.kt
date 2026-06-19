package io.kess.ecommerce.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.kess.ecommerce.R
import io.kess.ecommerce.model.ProductVariant

class VariantAdapter(
    private val onEditClick: (ProductVariant, Int) -> Unit,
    private val onDeleteClick: (ProductVariant) -> Unit
) : ListAdapter<ProductVariant, VariantAdapter.ViewHolder>(DiffCallback()) {

    class DiffCallback : DiffUtil.ItemCallback<ProductVariant>() {

        override fun areItemsTheSame(
            oldItem: ProductVariant,
            newItem: ProductVariant
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: ProductVariant,
            newItem: ProductVariant
        ): Boolean {
            return oldItem == newItem
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val color: TextView =
            view.findViewById(R.id.color)
        val size: TextView = view.findViewById(R.id.size)
        val stock: TextView =
            view.findViewById(R.id.stock)
        val btnEdit: ImageView =
            view.findViewById(R.id.btn_edit_variant1)
        val btnDelete: ImageView =
            view.findViewById(R.id.btn_delete_variant1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.variant_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val variant = getItem(position)

        holder.color.text = variant.color
        holder.stock.text = "Stock: ${variant.stock}"
        holder.size.text = variant.size

        holder.btnEdit.setOnClickListener {
            val pos = holder.bindingAdapterPosition

            if (pos != RecyclerView.NO_POSITION) {
                onEditClick.invoke(getItem(pos), pos)
            }
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(variant)
        }
    }
}