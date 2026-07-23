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
import io.kess.ecommerce.model.Category

class CategoryFilter(
    private val onClick: (Category?) -> Unit
) : ListAdapter<Category, CategoryFilter.ViewHolder>(DiffCallback()) {
    private var selectedId: String? = null
    fun setSelected(id: String?) {
        selectedId = id
        notifyDataSetChanged()
    }

    class DiffCallback : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(
            oldItem: Category,
            newItem: Category
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: Category,
            newItem: Category
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

        holder.button.text = item.name
        if (item.id == selectedId) {
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
            selectedId = if(selectedId == item.id){
                null
            }else{
                item.id
            }
            notifyDataSetChanged()
            onClick(
                if(selectedId == null){
                    null
                }else{
                    item
                }
            )
        }
    }
}