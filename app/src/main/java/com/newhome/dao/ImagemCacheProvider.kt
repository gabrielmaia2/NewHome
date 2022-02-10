package com.newhome.dao

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import java.io.InputStream
import java.net.URL
import java.util.concurrent.Executors

class ImagemCacheProvider {
    companion object {
        private val bitmaps: HashMap<String, Bitmap> = HashMap()

        fun tryLoadBitmap(
            url: String,
            onSuccess: (bitmap: Bitmap) -> Unit,
            onFailure: (e: Exception) -> Unit
        ) {
            val executor = Executors.newSingleThreadExecutor()
            val handler = Handler(Looper.getMainLooper())
            executor.execute {
                try {
                    val inputStream = URL(url).content as InputStream
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    handler.post {
                        onSuccess(bitmap)
                    }
                } catch (e: Exception) {
                    handler.post {
                        onFailure(e)
                    }
                }
            }
        }

        fun tryLoadCachedBitmap(
            imageUrl: String,
            onSuccess: (bitmap: Bitmap) -> Unit,
            onFailure: (e: Exception) -> Unit
        ) {
            val cached = bitmaps[imageUrl]
            if (cached != null) {
                onSuccess(cached)
                return
            }

            tryLoadBitmap(imageUrl, { bitmap ->
                bitmaps[imageUrl] = bitmap
                onSuccess(bitmap)
            }, onFailure)
        }

        fun removeCachedBitmap(imageUrl: String) {
            bitmaps.remove(imageUrl)
        }
    }
}