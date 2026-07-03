package io.kess.ecommerce.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.kess.ecommerce.R
import io.kess.ecommerce.model.Shop

class ShopFilter(
    private val onClick: (Shop) -> Unit
) : ListAdapter<Shop, ShopFilter.ViewHolder>(DiffCallback()) {

    private var selectedId: String? = null

    fun setSelected(id: String?) {
        selectedId = id
        notifyDataSetChanged()
    }

    class DiffCallback : DiffUtil.ItemCallback<Shop>() {
        override fun areItemsTheSame(oldItem: Shop, newItem: Shop) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Shop, newItem: Shop) =
            oldItem == newItem
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val radio: RadioButton = view.findViewById(R.id.radioShop)
        val name: TextView = view.findViewById(R.id.tvShopName)
        val row: LinearLayout = view.findViewById(R.id.shopRow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_shop, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.name.text = item.shopName

        holder.radio.isChecked = item.id == selectedId

        holder.row.setOnClickListener {
            selectItem(item)
        }
        holder.radio.setOnClickListener {
            selectItem(item)
        }
    }

    private fun selectItem(item: Shop) {
        selectedId = item.id
        notifyDataSetChanged()
        onClick(item)
    }
}