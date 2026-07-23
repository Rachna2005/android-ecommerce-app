package io.kess.ecommerce.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.cloudinary.android.MediaManager
import io.kess.ecommerce.R
import io.kess.ecommerce.databinding.FragmentCreateShopBinding
import io.kess.ecommerce.model.Shop
import io.kess.ecommerce.ui.seller.ShopActivity
import io.kess.ecommerce.util.UiState
import io.kess.ecommerce.util.showSnackBar
import io.kess.ecommerce.view_model.AuthViewModel
import io.kess.ecommerce.view_model.ShopViewModel

class CreateShopFragment : Fragment() {
    private var _binding: FragmentCreateShopBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AuthViewModel
    private lateinit var shopViewModel: ShopViewModel
    private var selectedImageUri: Uri? = null

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->

            uri?.let {
                selectedImageUri = it

                binding.shopLogo.setImageURI(it)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateShopBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        observeData()
        setupOnClickListener()
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(requireActivity())[AuthViewModel::class.java]
        shopViewModel = ViewModelProvider(requireActivity())[ShopViewModel::class.java]
    }

    private fun setupOnClickListener() {
        binding.backBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.shopLogo.setOnClickListener {
            pickImage.launch("image/*")
        }
        binding.btnCreateAccount.setOnClickListener {
            val shopName = binding.inputName.text.toString().trim()
            val description = binding.inputShopDescription.text.toString().trim()
            val phoneNumber = binding.inputPhone.text.toString().trim()
            val address = binding.inputLocation.text.toString().trim()

            if (
                shopName.isBlank() ||
                description.isBlank() ||
                phoneNumber.isBlank() ||
                address.isBlank()
            ) {
                showSnackBar(
                    requireView(),
                    "All fields need to be filled",
                    ContextCompat.getColor(requireContext(), R.color.yellow),
                    ContextCompat.getColor(requireContext(), R.color.black)
                )
                return@setOnClickListener
            }

            if (selectedImageUri == null) {
                showSnackBar(
                    requireView(),
                    "Please select shop logo",
                    ContextCompat.getColor(requireContext(), R.color.yellow),
                    ContextCompat.getColor(requireContext(), R.color.black)
                )
                return@setOnClickListener
            }
            uploadImage(
                selectedImageUri!!,
                onSuccess = { imageUrl ->
                    val sellerId = (viewModel.authState.value as? UiState.Success)?.data?.id
                    if (sellerId == null) {
                        showSnackBar(
                            requireView(),
                            "Unable to get user information",
                            ContextCompat.getColor(requireContext(), R.color.red),
                            ContextCompat.getColor(requireContext(), android.R.color.white)
                        )
                        return@uploadImage
                    }
                    val shop = Shop(
                        ownerId = sellerId,
                        shopName = shopName,
                        description = description,
                        phone = phoneNumber,
                        address = address,
                        logoUrl = imageUrl
                    )
                    shopViewModel.createShop(shop)
                },
                onError = { msg ->
                    showSnackBar(
                        requireView(), msg,
                        ContextCompat.getColor(requireContext(), R.color.red),
                        ContextCompat.getColor(requireContext(), android.R.color.white)
                    )
                }
            )

        }
    }

    private fun uploadImage(
        imageUri: Uri,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {

        MediaManager.get()
            .upload(imageUri)
            .unsigned("ecommerce_preset")
            .callback(object : com.cloudinary.android.callback.UploadCallback {

                override fun onStart(requestId: String?) {}

                override fun onProgress(
                    requestId: String?,
                    bytes: Long,
                    totalBytes: Long
                ) {
                }

                override fun onSuccess(
                    requestId: String?,
                    resultData: Map<*, *>
                ) {
                    val url = resultData["secure_url"]?.toString()
                    if (!url.isNullOrEmpty()) {
                        onSuccess(url)
                    } else {
                        onError("Upload failed: empty URL")
                    }
                }

                override fun onError(
                    requestId: String?,
                    error: com.cloudinary.android.callback.ErrorInfo?
                ) {
                    onError(error?.toString() ?: "Upload failed")
                }

                override fun onReschedule(
                    requestId: String?,
                    error: com.cloudinary.android.callback.ErrorInfo?
                ) {

                }
            })
            .dispatch()
    }

    private fun observeData() {
        shopViewModel.shopState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    showLoading(true)
                }
                is UiState.Success -> {
                    showLoading(false)
                    binding.root.isEnabled = false
                    showSnackBar(
                        requireView(),
                        "Shop created successfully",
                        ContextCompat.getColor(requireContext(), R.color.green),
                        ContextCompat.getColor(requireContext(), android.R.color.white)
                    ) {
                        startActivity(Intent(requireContext(), ShopActivity::class.java))
                        requireActivity().finish()
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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}