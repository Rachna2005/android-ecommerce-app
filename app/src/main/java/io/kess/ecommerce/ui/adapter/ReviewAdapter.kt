package io.kess.ecommerce.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import io.kess.ecommerce.R
import io.kess.ecommerce.model.Review
import java.text.SimpleDateFormat
import java.util.Locale

class ReviewAdapter :
    ListAdapter<Review, ReviewAdapter.ViewHolder>(DiffCallback()) {

    class DiffCallback : DiffUtil.ItemCallback<Review>() {

        override fun areItemsTheSame(
            oldItem: Review,
            newItem: Review
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: Review,
            newItem: Review
        ): Boolean {
            return oldItem == newItem
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val userName: TextView =
            view.findViewById(R.id.userName)

        val reviewText: TextView =
            view.findViewById(R.id.review)

        val date: TextView =
            view.findViewById(R.id.date)

        val starContainer: LinearLayout =
            view.findViewById(R.id.star)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.review_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        val item = getItem(position)

        holder.userName.text = "-${item.username}"
        holder.reviewText.text = item.review

        holder.date.text = formatDate(item.createdAt)

        updateStars(
            holder.starContainer,
            item.rating
        )
    }

    private fun updateStars(
        container: LinearLayout,
        rating: Int
    ) {

        for (i in 0 until container.childCount) {

            val star = container.getChildAt(i) as ImageView

            if (i < rating) {
                star.setImageResource(R.drawable.ic_star_fill)
            } else {
                star.setImageResource(R.drawable.ic_star)
            }
        }
    }

    private fun formatDate(
        timestamp: Timestamp?
    ): String {

        if (timestamp == null) return ""

        val formatter = SimpleDateFormat(
            "dd-MM-yyyy",
            Locale.getDefault()
        )

        return "Posted at ${formatter.format(timestamp.toDate())}"
    }
}