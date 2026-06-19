package io.kess.ecommerce.util

import android.app.Application
import com.cloudinary.android.MediaManager

class EcommerceApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val config = HashMap<String, String>()

        config["cloud_name"] = "dcao8vmuc"

        MediaManager.init(this, config)
    }
}