package io.kess.ecommerce.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import io.kess.ecommerce.R
import io.kess.ecommerce.model.Order

import java.text.SimpleDateFormat
import androidx.recyclerview.widget.DiffUtil
import io.kess.ecommerce.ui.adapter.ProductAdapter.DiffCallback
import java.util.Locale

class OrderAdapter(
    private val onOrderClick: (Order) -> Unit,
//    private val onTrackingClick: (Order) -> Unit
) : ListAdapter<Order, OrderAdapter.OrderViewHolder>(DiffCallback()) {

    class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtOrderId: TextView = view.findViewById(R.id.txtOrderId)
        val txtStatus: TextView = view.findViewById(R.id.txtStatus)
        val txtDate: TextView = view.findViewById(R.id.txtDate)
        val txtTotal: TextView = view.findViewById(R.id.txtTotal)

        val imgProduct1: ImageView = view.findViewById(R.id.imgProduct1)
        val imgProduct2: ImageView = view.findViewById(R.id.imgProduct2)
        val imgProduct3: ImageView = view.findViewById(R.id.imgProduct3)
        val totalQuantity: TextView = view.findViewById(R.id.txtTotalQuantity)

//        val btnTracking: MaterialButton =
//            view.findViewById(R.id.btnTracking)
        val btnDetail: MaterialButton = view.findViewById(R.id.btnDetail)
    }

    class DiffCallback : DiffUtil.ItemCallback<Order>() {

        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OrderViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.order_history_item, parent, false)

        return OrderViewHolder(view)
    }


    override fun onBindViewHolder(
        holder: OrderViewHolder,
        position: Int
    ) {

        val order = getItem(position)

        holder.txtOrderId.text =
            "ORD-${order.id.takeLast(6)}"

        holder.txtStatus.text = order.status

        holder.txtTotal.text =
            "$${String.format("%.2f", order.totalPrice)}"
        if (order.totalQuantity == 1) {
            holder.totalQuantity.text = "${order.totalQuantity} item"
        } else {
            holder.totalQuantity.text = "${order.totalQuantity} items"
        }


        order.createdAt?.let {
            val date =
                SimpleDateFormat(
                    "dd MMM yyyy",
                    Locale.getDefault()
                ).format(it.toDate())

            holder.txtDate.text = date
        }

        val items = order.previewImages

        if (items.isNotEmpty()) {
            Glide.with(holder.itemView)
                .load(items[0])
                .into(holder.imgProduct1)
        }

        if (items.size > 1) {
            Glide.with(holder.itemView)
                .load(items[1])
                .into(holder.imgProduct2)
        }

        if (items.size > 2) {
            Glide.with(holder.itemView)
                .load(items[2])
                .into(holder.imgProduct3)
        }
//        holder.itemView.setOnClickListener {
//            onOrderClick(order)
//        }
        holder.btnDetail.setOnClickListener {
            onOrderClick(order)
        }

//        holder.itemView.findViewById<View>(R.id.btnDetail).setOnClickListener {
//            onTrackingClick(order)
//        }
    }
}