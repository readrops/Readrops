package com.readrops.app.utils

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import com.readrops.api.utils.HttpManager
import java.io.InputStream

@GlideModule
class ReadropsGlideModule : AppGlideModule() {

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        val factory = OkHttpUrlLoader.Factory(HttpManager.getInstance().okHttpClient)
        
        glide.registry.replace(GlideUrl::class.java, InputStream::class.java, factory)
    }
}