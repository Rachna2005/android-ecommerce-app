package io.kess.ecommerce

import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity

import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import io.kess.ecommerce.databinding.ActivityMainBinding
import io.kess.ecommerce.databinding.FragmentDisplayProductBinding
import io.kess.ecommerce.databinding.TestBinding
//import io.kess.ecommerce.ui.FragmentTest
import io.kess.ecommerce.ui.ProductListFragment
import io.kess.ecommerce.ui.ProductShopFragment
import io.kess.ecommerce.ui.SearchFragment
import io.kess.ecommerce.ui.ShopDetailFragment
import io.kess.ecommerce.ui.ShopInfoFragment

//data class Product(
//    val id: String,
//    val title: String,
//    val price: Double
//)

class TestActivity : AppCompatActivity() {
    private lateinit var binding: TestBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = TestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, SearchFragment())
            .commit()
    }

    fun replaceFragment(fragment: Fragment) {

        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }

    private fun showFilterBottomSheet() {
        val view = layoutInflater.inflate(R.layout.filter_bottom_sheet, null)

        val sheet = com.google.android.material.bottomsheet.BottomSheetDialog(this)
        sheet.setContentView(view)

        val slider =
            view.findViewById<com.google.android.material.slider.RangeSlider>(R.id.sliderPrice)

        slider.valueFrom = 0f
        slider.valueTo = 2000f
        slider.values = listOf(0f, 2000f)

        val tvPriceRange = view.findViewById<TextView>(R.id.tvPriceRange)

        slider.addOnChangeListener { s, _, _ ->
            val min = s.values[0].toInt()
            val max = s.values[1].toInt()
            tvPriceRange.text = "$min - $max"
        }

        sheet.show()
    }
}
