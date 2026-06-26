package io.kess.ecommerce.ui

import android.content.Intent
import android.media.Rating
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.compose.ui.graphics.ImageBitmap
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import io.kess.ecommerce.R
import io.kess.ecommerce.databinding.FragmentProductDetailBinding
import io.kess.ecommerce.databinding.FragmentReviewBinding
import io.kess.ecommerce.ui.activity.MainActivity
import io.kess.ecommerce.ui.seller.ShopActivity
import io.kess.ecommerce.util.UiState
import io.kess.ecommerce.util.showSnackBar
import io.kess.ecommerce.view_model.ReviewViewModel

class ReviewFragment : Fragment() {
    private var _binding: FragmentReviewBinding? = null
    private val binding get() = _binding!!
    private lateinit var reviewViewModel: ReviewViewModel
    private var productId: String? = null
    private var productName: String? = null
    private var image: String? = null
    private var rating: Int = 0
    private var review: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        productId = arguments?.getString("ID")
        productName = arguments?.getString("Name")
        image = arguments?.getString("Image")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        setupUi()
        setupRatingStars()
        setupClickListener()
        observeData()
    }

    private fun initViewModel() {
        reviewViewModel = ViewModelProvider(this)[ReviewViewModel::class.java]
    }


    private fun setupRatingStars() {
        val stars = listOf(
            binding.star1,
            binding.star2,
            binding.star3,
            binding.star4,
            binding.star5
        )
        stars.forEachIndexed { index, view ->
            view.setOnClickListener {
                rating = index + 1
                updateStar(stars)
            }
        }
    }

    private fun updateStar(stars: List<ImageView>) {
        stars.forEachIndexed { index, view ->
            if (index < rating) {
                view.setImageResource(R.drawable.ic_star_fill)
            } else {
                view.setImageResource(R.drawable.ic_star)
            }
        }
    }

    private fun setupClickListener() {
        binding.btnSubmitReview.setOnClickListener {
            review = binding.inputReview.text.toString().trim()
            val id = productId

            if (id.isNullOrEmpty()) {
//                Toast.makeText(requireContext(), "Product not found", Toast.LENGTH_SHORT).show()
                showSnackBar(requireView(), "Product not found",
                    backgroundColor = ContextCompat.getColor(requireContext(), R.color.red),
                    textColor = ContextCompat.getColor(requireContext(), R.color.white))
                return@setOnClickListener
            }

            if (rating == 0) {
//                Toast.makeText(requireContext(), "Please select rating", Toast.LENGTH_SHORT).show()
                showSnackBar(requireView(), "Please select rating",
                    backgroundColor = ContextCompat.getColor(requireContext(), R.color.primary),
                    textColor = ContextCompat.getColor(requireContext(), R.color.white))
                return@setOnClickListener
            }

            if (review.isBlank()) {
//                Toast.makeText(requireContext(), "Please write review", Toast.LENGTH_SHORT).show()
                showSnackBar(requireView(), "Please write review",
                    backgroundColor = ContextCompat.getColor(requireContext(), R.color.primary),
                    textColor = ContextCompat.getColor(requireContext(), R.color.white))
                return@setOnClickListener
            }

            reviewViewModel.addReview(
                productId = id,
                review = review,
                rating = rating
            )

        }
        binding.btnClose.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun observeData() {
//        reviewViewModel.message.observe(viewLifecycleOwner){message ->
//
//            parentFragmentManager.popBackStack()
//        }
        reviewViewModel.actionState.observe(viewLifecycleOwner){state ->
            when (state) {
                is UiState.Loading -> {
                    showLoading(true)
                }
                is UiState.Success -> {
                    showLoading(false)
                    showSnackBar(
                        requireView(),
                        "Review Add successfully",
                        ContextCompat.getColor(requireContext(), R.color.green),
                        ContextCompat.getColor(requireContext(), android.R.color.white)
                    ){
                        parentFragmentManager.popBackStack()
                    }
                }

                is UiState.Error -> {
                    showLoading(false)
                    showSnackBar(
                        requireView(),
                        state.message,
                        ContextCompat.getColor(requireContext(), R.color.red),
                        ContextCompat.getColor(requireContext(), android.R.color.white)
                    )
                }
                is UiState.Idle -> {}
            }
        }
    }
    private fun showLoading(show: Boolean) {
        binding.loadingOverlay.visibility = if (show) View.VISIBLE else View.GONE
        binding.root.isEnabled = !show
    }

    private fun setupUi() {
        binding.productName.text = productName
        Glide.with(requireContext()).load(image).into(binding.image)
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).showButtonNav(show = false)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        (activity as MainActivity).showButtonNav(show = true)
    }

}