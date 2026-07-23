package io.kess.ecommerce.ui.adapter

import io.kess.ecommerce.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import io.kess.ecommerce.model.Category


class CategoryAdapter(private var onCategoryClick: (Category) -> Unit) :
    ListAdapter<Category, CategoryAdapter.ViewHolder>(DiffCallback()) {

    class DiffCallback : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem == newItem
        }
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val cateoryContainer = view.findViewById<LinearLayout>(R.id.container)
        val img = view.findViewById<ImageView>(R.id.image)
        val title = view.findViewById<TextView>(R.id.title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(io.kess.ecommerce.R.layout.item_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder, position: Int
    ) {
        val item = getItem(position)
        holder.title.text = item.name
        Glide.with(holder.itemView.context).load(item.image).into(holder.img)
        holder.cateoryContainer.setOnClickListener { onCategoryClick(item) }
    }


}