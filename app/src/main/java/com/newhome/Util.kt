package com.newhome

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.Patterns
import androidx.appcompat.content.res.AppCompatResources
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.concurrent.Executors

class Util {
    companion object {
        fun tryLoadDrawable(context: Context, url: String): Drawable {
            if (!Patterns.WEB_URL.matcher(url).matches())
                return AppCompatResources.getDrawable(context, R.drawable.image_default)!!

            return try {
                val inputStream = URL(url).content as InputStream
                Drawable.createFromStream(inputStream, "src name")
            } catch (e: IOException) {
                AppCompatResources.getDrawable(context, R.drawable.image_default)!!
            }
        }

        fun tryLoadDrawableAsync(context: Context, url: String, onLoad: (drawable: Drawable) -> Unit) {
            val executor = Executors.newSingleThreadExecutor()
            val handler = Handler(Looper.getMainLooper())
            executor.execute {
                val drawable = tryLoadDrawable(context, url)
                handler.post {
                    onLoad(drawable)
                }
            }
        }
    }
}
