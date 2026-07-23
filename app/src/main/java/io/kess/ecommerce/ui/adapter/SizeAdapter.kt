package io.kess.ecommerce.ui.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import io.kess.ecommerce.R
import io.kess.ecommerce.model.ProductVariant

class SizeAdapter(
    private val onClick: (ProductVariant) -> Unit
) : ListAdapter<ProductVariant, SizeAdapter.ViewHolder>(DiffCallback()) {

    private var selectedColor: String? = null
    private var sizeId: String? = null
    private var allVariants: List<ProductVariant> = emptyList()
    fun setVariant(variants: List<ProductVariant>) {
        allVariants = variants
        submitList(variants.distinctBy { it.size })
    }

    fun setSelectedColor(color: String?) {
        selectedColor = color
        notifyDataSetChanged()
    }

    class DiffCallback : DiffUtil.ItemCallback<ProductVariant>() {
        override fun areItemsTheSame(
            oldItem: ProductVariant,
            newItem: ProductVariant
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: ProductVariant,
            newItem: ProductVariant
        ): Boolean = oldItem == newItem
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val button: MaterialButton = view.findViewById(R.id.btnVariant)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.varaint_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.button.text = item.size

        val isAvailable = selectedColor == null || allVariants.any {
            it.color == selectedColor && it.size == item.size
        }

        holder.button.isEnabled = isAvailable
        holder.button.alpha = if (isAvailable) 1f else 0.5f

        if (item.id == sizeId && isAvailable) {
            holder.button.strokeColor = ColorStateList.valueOf(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.primary
                )
            )
            holder.button.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context, R.color.primary
                )
            )
        } else {
            holder.button.strokeColor = ColorStateList.valueOf(
                ContextCompat.getColor(
                    holder.itemView.context, R.color.stroke_default
                )
            )
            holder.button.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.black)
            )
        }

        holder.button.setOnClickListener {
            sizeId = item.id
            notifyDataSetChanged()
            onClick(item)
        }
    }
}