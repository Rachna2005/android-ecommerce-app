package io.kess.ecommerce.ui.adapter

import io.kess.ecommerce.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import io.kess.ecommerce.model.Category
import io.kess.ecommerce.model.Product


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
//        val count = view.findViewById<TextView>(R.id.count)
//        val container = view.findViewById<LinearLayout>(R.id.textContainer)
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
//        holder.count.text = "${item.productCount} Products"
        Glide.with(holder.itemView.context).load(item.image).into(holder.img)
//        val para = holder.container.layoutParams as LinearLayout.LayoutParams
        holder.cateoryContainer.setOnClickListener { onCategoryClick(item) }
//        if (item.alignRight) {
//            para.addRule(RelativeLayout.ALIGN_PARENT_END)
//            para.removeRule(RelativeLayout.ALIGN_PARENT_START)
//            holder.title.textAlignment = View.TEXT_ALIGNMENT_VIEW_END
//            holder.count.textAlignment = View.TEXT_ALIGNMENT_VIEW_END
//        } else {
//            para.addRule(RelativeLayout.ALIGN_PARENT_START)
//            para.removeRule(RelativeLayout.ALIGN_PARENT_END)
//            holder.title.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
//            holder.count.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
//        }
//        holder.container.layoutParams = para
    }


}